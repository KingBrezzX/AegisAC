package id.kingbrezz.aegisac.player;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerData {

    private final UUID uniqueId;

    private volatile String name;

    private volatile Location lastLocation;
    private volatile Location lastSafeLocation;

    private volatile long joinTime;
    private volatile long lastMovementTime;
    private volatile long lastAttackTime;
    private volatile long lastViolationTime;

    private volatile int ping;

    private volatile boolean exempt;
    private volatile boolean temporarilyExempt;

    private final Map<String, Double> violations =
            new ConcurrentHashMap<>();

    private final Map<String, Long> lastCheckTimes =
            new ConcurrentHashMap<>();

    private final Map<String, Integer> setbackCounts =
            new ConcurrentHashMap<>();

    public PlayerData(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.joinTime = System.currentTimeMillis();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public Location getLastLocation() {
        return cloneLocation(lastLocation);
    }

    public void setLastLocation(Location location) {
        this.lastLocation = cloneLocation(location);
    }

    public Location getLastSafeLocation() {
        return cloneLocation(lastSafeLocation);
    }

    public void setLastSafeLocation(Location location) {
        this.lastSafeLocation = cloneLocation(location);
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void resetJoinTime() {
        joinTime = System.currentTimeMillis();
    }

    public long getLastMovementTime() {
        return lastMovementTime;
    }

    public void markMovement() {
        lastMovementTime = System.currentTimeMillis();
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void markAttack() {
        lastAttackTime = System.currentTimeMillis();
    }

    public long getLastViolationTime() {
        return lastViolationTime;
    }

    public void markViolation() {
        lastViolationTime = System.currentTimeMillis();
    }

    public int getPing() {
        return ping;
    }

    public void setPing(int ping) {
        this.ping = Math.max(0, ping);
    }

    public boolean isExempt() {
        return exempt;
    }

    public void setExempt(boolean exempt) {
        this.exempt = exempt;
    }

    public boolean isTemporarilyExempt() {
        return temporarilyExempt;
    }

    public void setTemporarilyExempt(boolean temporarilyExempt) {
        this.temporarilyExempt = temporarilyExempt;
    }

    public boolean isWithinJoinGracePeriod(int seconds) {
        if (seconds <= 0) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - joinTime;
        return elapsed < seconds * 1000L;
    }

    public double getViolation(String check) {
        if (check == null || check.isBlank()) {
            return 0.0;
        }

        return violations.getOrDefault(check, 0.0);
    }

    public double addViolation(
            String check,
            double amount
    ) {
        if (check == null || check.isBlank()) {
            return 0.0;
        }

        if (!Double.isFinite(amount) || amount <= 0.0) {
            return getViolation(check);
        }

        double result = violations.merge(
                check,
                amount,
                Double::sum
        );

        lastViolationTime = System.currentTimeMillis();

        return result;
    }

    public double reduceViolation(
            String check,
            double amount
    ) {
        if (check == null || check.isBlank()) {
            return 0.0;
        }

        if (!Double.isFinite(amount) || amount <= 0.0) {
            return getViolation(check);
        }

        return violations.compute(
                check,
                (key, current) -> {
                    if (current == null) {
                        return 0.0;
                    }

                    return Math.max(
                            0.0,
                            current - amount
                    );
                }
        );
    }

    public void setViolation(
            String check,
            double value
    ) {
        if (check == null || check.isBlank()) {
            return;
        }

        if (!Double.isFinite(value)) {
            return;
        }

        violations.put(
                check,
                Math.max(0.0, value)
        );
    }

    public void resetViolation(String check) {
        if (check == null || check.isBlank()) {
            return;
        }

        violations.remove(check);
    }

    public void resetViolations() {
        violations.clear();
    }

    public Map<String, Double> getViolations() {
        return Map.copyOf(violations);
    }

    public long getLastCheckTime(String check) {
        if (check == null || check.isBlank()) {
            return 0L;
        }

        return lastCheckTimes.getOrDefault(
                check,
                0L
        );
    }

    public void markCheck(String check) {
        if (check == null || check.isBlank()) {
            return;
        }

        lastCheckTimes.put(
                check,
                System.currentTimeMillis()
        );
    }

    public int getSetbackCount(String check) {
        if (check == null || check.isBlank()) {
            return 0;
        }

        return setbackCounts.getOrDefault(
                check,
                0
        );
    }

    public int incrementSetbackCount(String check) {
        if (check == null || check.isBlank()) {
            return 0;
        }

        return setbackCounts.merge(
                check,
                1,
                Integer::sum
        );
    }

    public void resetSetbackCounts() {
        setbackCounts.clear();
    }

    public void resetSetbackCount(String check) {
        if (check == null || check.isBlank()) {
            return;
        }

        setbackCounts.remove(check);
    }

    public void clearRuntimeState() {
        violations.clear();
        lastCheckTimes.clear();
        setbackCounts.clear();

        lastLocation = null;
        lastSafeLocation = null;

        lastMovementTime = 0L;
        lastAttackTime = 0L;
        lastViolationTime = 0L;
    }

    private Location cloneLocation(Location location) {
        return location == null
                ? null
                : location.clone();
    }
          }
