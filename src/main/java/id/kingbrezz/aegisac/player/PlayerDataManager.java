package id.kingbrezz.aegisac.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataManager {

    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();

    public PlayerDataManager() {
    }

    public PlayerData get(Player player) {
        return get(player.getUniqueId());
    }

    public PlayerData get(UUID uuid) {
        return players.computeIfAbsent(uuid, ignored -> new PlayerData());
    }

    public boolean contains(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    public void remove(Player player) {
        if (player != null) {
            players.remove(player.getUniqueId());
        }
    }

    public void remove(UUID uuid) {
        if (uuid != null) {
            players.remove(uuid);
        }
    }

    public void clear() {
        players.clear();
    }

    public int size() {
        return players.size();
    }

    public Map<UUID, PlayerData> getPlayers() {
        return Map.copyOf(players);
    }

    public static final class PlayerData {

        private Location lastLocation;
        private Location lastValidLocation;

        private long lastMovementTime;
        private long lastAttackTime;
        private long lastPlaceTime;
        private long lastBreakTime;

        private double lastDeltaX;
        private double lastDeltaY;
        private double lastDeltaZ;

        private double lastHorizontalDistance;
        private double lastDistance;

        private float lastYaw;
        private float lastPitch;

        private boolean lastOnGround;
        private boolean lastSprinting;
        private boolean lastSwimming;
        private boolean lastGliding;
        private boolean lastFlying;

        private int movementTicks;
        private int airTicks;
        private int groundTicks;

        private long joinTime;

        private double ping;
        private double tps = 20.0;

        private final Map<String, CheckData> checks =
                new ConcurrentHashMap<>();

        public PlayerData() {
            this.joinTime = System.currentTimeMillis();
            this.lastMovementTime = this.joinTime;
        }

        public void updateMovement(Player player) {
            Location current = player.getLocation();

            if (lastLocation != null) {
                lastDeltaX = current.getX() - lastLocation.getX();
                lastDeltaY = current.getY() - lastLocation.getY();
                lastDeltaZ = current.getZ() - lastLocation.getZ();

                lastHorizontalDistance = Math.sqrt(
                        (lastDeltaX * lastDeltaX)
                                + (lastDeltaZ * lastDeltaZ)
                );

                lastDistance = Math.sqrt(
                        (lastDeltaX * lastDeltaX)
                                + (lastDeltaY * lastDeltaY)
                                + (lastDeltaZ * lastDeltaZ)
                );
            }

            lastYaw = current.getYaw();
            lastPitch = current.getPitch();

            lastOnGround = player.isOnGround();
            lastSprinting = player.isSprinting();
            lastSwimming = player.isSwimming();
            lastGliding = player.isGliding();
            lastFlying = player.isFlying();

            movementTicks++;

            if (lastOnGround) {
                groundTicks++;
                airTicks = 0;
            } else {
                airTicks++;
                groundTicks = 0;
            }

            lastLocation = current.clone();
            lastMovementTime = System.currentTimeMillis();

            updatePing(player);
        }

        public void markValidLocation(Location location) {
            if (location == null) {
                return;
            }

            lastValidLocation = location.clone();
        }

        public Location getLastLocation() {
            return cloneLocation(lastLocation);
        }

        public Location getLastValidLocation() {
            return cloneLocation(lastValidLocation);
        }

        public boolean hasValidLocation() {
            return lastValidLocation != null;
        }

        public long getLastMovementTime() {
            return lastMovementTime;
        }

        public long getLastAttackTime() {
            return lastAttackTime;
        }

        public void markAttack() {
            lastAttackTime = System.currentTimeMillis();
        }

        public long getLastPlaceTime() {
            return lastPlaceTime;
        }

        public void markPlace() {
            lastPlaceTime = System.currentTimeMillis();
        }

        public long getLastBreakTime() {
            return lastBreakTime;
        }

        public void markBreak() {
            lastBreakTime = System.currentTimeMillis();
        }

        public double getLastDeltaX() {
            return lastDeltaX;
        }

        public double getLastDeltaY() {
            return lastDeltaY;
        }

        public double getLastDeltaZ() {
            return lastDeltaZ;
        }

        public double getLastHorizontalDistance() {
            return lastHorizontalDistance;
        }

        public double getLastDistance() {
            return lastDistance;
        }

        public float getLastYaw() {
            return lastYaw;
        }

        public float getLastPitch() {
            return lastPitch;
        }

        public boolean wasOnGround() {
            return lastOnGround;
        }

        public boolean wasSprinting() {
            return lastSprinting;
        }

        public boolean wasSwimming() {
            return lastSwimming;
        }

        public boolean wasGliding() {
            return lastGliding;
        }

        public boolean wasFlying() {
            return lastFlying;
        }

        public int getMovementTicks() {
            return movementTicks;
        }

        public int getAirTicks() {
            return airTicks;
        }

        public int getGroundTicks() {
            return groundTicks;
        }

        public long getJoinTime() {
            return joinTime;
        }

        public long getPlayTime() {
            return System.currentTimeMillis() - joinTime;
        }

        public double getPing() {
            return ping;
        }

        public void setPing(double ping) {
            this.ping = Math.max(0.0, ping);
        }

        private void updatePing(Player player) {
            try {
                setPing(player.getPing());
            } catch (Throwable ignored) {
                /*
                 * Keep the last known ping if the server implementation
                 * does not expose it.
                 */
            }
        }

        public double getTps() {
            return tps;
        }

        public void setTps(double tps) {
            if (Double.isFinite(tps)) {
                this.tps = Math.max(1.0, Math.min(20.0, tps));
            }
        }

        public CheckData getCheck(String checkName) {
            return checks.computeIfAbsent(
                    normalize(checkName),
                    ignored -> new CheckData()
            );
        }

        public void resetCheck(String checkName) {
            checks.remove(normalize(checkName));
        }

        public Map<String, CheckData> getChecks() {
            return Map.copyOf(checks);
        }

        private static String normalize(String name) {
            return name
                    .toLowerCase()
                    .replace('-', '_')
                    .replace(' ', '_');
        }

        private static Location cloneLocation(Location location) {
            return location == null ? null : location.clone();
        }
    }

    public static final class CheckData {

        private double buffer;
        private double violations;

        private long lastFlagTime;
        private long lastAlertTime;

        private int flags;
        private int consecutiveFlags;

        public double getBuffer() {
            return buffer;
        }

        public void setBuffer(double buffer) {
            this.buffer = Math.max(0.0, buffer);
        }

        public void increaseBuffer(double amount, double maximum) {
            if (!Double.isFinite(amount)) {
                return;
            }

            double safeMaximum = Math.max(0.0, maximum);

            buffer = Math.min(
                    safeMaximum,
                    Math.max(0.0, buffer + amount)
            );
        }

        public void decreaseBuffer(double amount) {
            if (!Double.isFinite(amount)) {
                return;
            }

            buffer = Math.max(0.0, buffer - Math.max(0.0, amount));
        }

        public double getViolations() {
            return violations;
        }

        public void addViolation(double amount) {
            if (!Double.isFinite(amount)) {
                return;
            }

            violations = Math.max(
                    0.0,
                    violations + Math.max(0.0, amount)
            );

            flags++;
            consecutiveFlags++;
            lastFlagTime = System.currentTimeMillis();
        }

        public void removeViolation(double amount) {
            if (!Double.isFinite(amount)) {
                return;
            }

            violations = Math.max(
                    0.0,
                    violations - Math.max(0.0, amount)
            );
        }

        public void decayViolations(double amount) {
            removeViolation(amount);

            if (violations <= 0.0) {
                consecutiveFlags = 0;
            }
        }

        public long getLastFlagTime() {
            return lastFlagTime;
        }

        public long getLastAlertTime() {
            return lastAlertTime;
        }

        public void markAlert() {
            lastAlertTime = System.currentTimeMillis();
        }

        public int getFlags() {
            return flags;
        }

        public int getConsecutiveFlags() {
            return consecutiveFlags;
        }

        public void resetConsecutiveFlags() {
            consecutiveFlags = 0;
        }

        public void reset() {
            buffer = 0.0;
            violations = 0.0;
            lastFlagTime = 0L;
            lastAlertTime = 0L;
            flags = 0;
            consecutiveFlags = 0;
        }
    }
        }
