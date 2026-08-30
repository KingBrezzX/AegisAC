package id.kingbrezz.aegisac.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Runtime detection data for a single player.
 *
 * This class intentionally contains only lightweight state.
 * It does not perform punishments or Bukkit event handling.
 */
public final class PlayerData {

    private final UUID uuid;

    private Location lastLocation;
    private Location lastValidLocation;

    private long lastMoveTime;
    private long lastRotationTime;
    private long lastAttackTime;

    private double horizontalDistance;
    private double verticalDistance;

    private float lastYaw;
    private float lastPitch;

    private int ticksSinceTeleport;
    private int ticksSinceJoin;

    private boolean onGround;
    private boolean lastOnGround;

    private boolean recentlyTeleported;
    private boolean exempt;

    public PlayerData(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException(
                    "uuid cannot be null"
            );
        }

        this.uuid = uuid;
        this.lastMoveTime = System.currentTimeMillis();
        this.lastRotationTime = this.lastMoveTime;
        this.lastAttackTime = 0L;

        this.ticksSinceTeleport = 100;
        this.ticksSinceJoin = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    /*
     * ------------------------------------------------------------
     * MOVEMENT
     * ------------------------------------------------------------
     */

    public void updateMovement(Player player) {
        if (player == null) {
            return;
        }

        Location location = player.getLocation();

        if (!isFinite(location)) {
            return;
        }

        if (lastLocation != null) {
            lastOnGround = onGround;
        }

        onGround = player.isOnGround();

        lastLocation = location.clone();

        if (lastValidLocation == null) {
            lastValidLocation = location.clone();
        }

        lastYaw = location.getYaw();
        lastPitch = location.getPitch();

        lastMoveTime = System.currentTimeMillis();

        ticksSinceJoin++;
        ticksSinceTeleport++;
    }

    public void recordMovement(
            Location from,
            Location to
    ) {
        if (from == null || to == null) {
            return;
        }

        if (!isFinite(from) || !isFinite(to)) {
            return;
        }

        if (from.getWorld() == null
                || to.getWorld() == null
                || !from.getWorld().equals(to.getWorld())) {
            return;
        }

        horizontalDistance = Math.sqrt(
                Math.pow(
                        to.getX() - from.getX(),
                        2.0
                )
                +
                Math.pow(
                        to.getZ() - from.getZ(),
                        2.0
                )
        );

        verticalDistance =
                to.getY() - from.getY();

        lastLocation = to.clone();

        lastYaw = to.getYaw();
        lastPitch = to.getPitch();

        lastMoveTime = System.currentTimeMillis();

        ticksSinceTeleport++;

        recentlyTeleported =
                ticksSinceTeleport < 3;
    }

    public void recordRotation(
            Location from,
            Location to
    ) {
        if (from == null || to == null) {
            return;
        }

        if (!isFinite(from) || !isFinite(to)) {
            return;
        }

        lastYaw = to.getYaw();
        lastPitch = to.getPitch();

        lastRotationTime =
                System.currentTimeMillis();
    }

    public void resetMovement(Location location) {
        if (location == null || !isFinite(location)) {
            return;
        }

        lastLocation = location.clone();
        lastValidLocation = location.clone();

        horizontalDistance = 0.0D;
        verticalDistance = 0.0D;

        lastYaw = location.getYaw();
        lastPitch = location.getPitch();

        lastMoveTime = System.currentTimeMillis();

        ticksSinceTeleport = 0;
        recentlyTeleported = true;
    }

    /**
     * Clears temporary detection state while keeping the UUID.
     */
    public void resetDetectionState() {
        horizontalDistance = 0.0D;
        verticalDistance = 0.0D;

        ticksSinceTeleport = 0;
        ticksSinceJoin = 0;

        recentlyTeleported = false;

        lastAttackTime = 0L;
        lastRotationTime = System.currentTimeMillis();
        lastMoveTime = System.currentTimeMillis();

        if (lastLocation != null) {
            lastValidLocation = lastLocation.clone();
        }
    }

    /*
     * ------------------------------------------------------------
     * ATTACK
     * ------------------------------------------------------------
     */

    public void recordAttack() {
        lastAttackTime =
                System.currentTimeMillis();
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public long getMillisSinceAttack() {
        if (lastAttackTime <= 0L) {
            return Long.MAX_VALUE;
        }

        return Math.max(
                0L,
                System.currentTimeMillis()
                        - lastAttackTime
        );
    }

    /*
     * ------------------------------------------------------------
     * LOCATION
     * ------------------------------------------------------------
     */

    public Location getLastLocation() {
        return cloneOrNull(lastLocation);
    }

    public Location getLastValidLocation() {
        return cloneOrNull(lastValidLocation);
    }

    public void setLastValidLocation(Location location) {
        if (location == null || !isFinite(location)) {
            return;
        }

        lastValidLocation = location.clone();
    }

    /*
     * ------------------------------------------------------------
     * DISTANCE
     * ------------------------------------------------------------
     */

    public double getHorizontalDistance() {
        return horizontalDistance;
    }

    public double getVerticalDistance() {
        return verticalDistance;
    }

    public double getMovementDistance() {
        return Math.sqrt(
                horizontalDistance
                        * horizontalDistance
                        +
                        verticalDistance
                        * verticalDistance
        );
    }

    /*
     * ------------------------------------------------------------
     * ROTATION
     * ------------------------------------------------------------
     */

    public float getLastYaw() {
        return lastYaw;
    }

    public float getLastPitch() {
        return lastPitch;
    }

    public long getLastRotationTime() {
        return lastRotationTime;
    }

    public long getMillisSinceRotation() {
        return Math.max(
                0L,
                System.currentTimeMillis()
                        - lastRotationTime
        );
    }

    /*
     * ------------------------------------------------------------
     * GROUND
     * ------------------------------------------------------------
     */

    public boolean isOnGround() {
        return onGround;
    }

    public boolean wasOnGround() {
        return lastOnGround;
    }

    public void setOnGround(boolean onGround) {
        this.lastOnGround = this.onGround;
        this.onGround = onGround;
    }

    /*
     * ------------------------------------------------------------
     * TELEPORT / JOIN
     * ------------------------------------------------------------
     */

    public int getTicksSinceTeleport() {
        return ticksSinceTeleport;
    }

    public int getTicksSinceJoin() {
        return ticksSinceJoin;
    }

    public boolean isRecentlyTeleported() {
        return recentlyTeleported;
    }

    public void setRecentlyTeleported(
            boolean recentlyTeleported
    ) {
        this.recentlyTeleported =
                recentlyTeleported;
    }

    /*
     * ------------------------------------------------------------
     * EXEMPTION
     * ------------------------------------------------------------
     */

    public boolean isExempt() {
        return exempt;
    }

    public void setExempt(boolean exempt) {
        this.exempt = exempt;
    }

    /*
     * ------------------------------------------------------------
     * TIME
     * ------------------------------------------------------------
     */

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public long getMillisSinceMove() {
        return Math.max(
                0L,
                System.currentTimeMillis()
                        - lastMoveTime
        );
    }

    /*
     * ------------------------------------------------------------
     * INTERNAL
     * ------------------------------------------------------------
     */

    private static Location cloneOrNull(
            Location location
    ) {
        return location == null
                ? null
                : location.clone();
    }

    private static boolean isFinite(
            Location location
    ) {
        return location.getWorld() != null
                && Double.isFinite(location.getX())
                && Double.isFinite(location.getY())
                && Double.isFinite(location.getZ())
                && Float.isFinite(location.getYaw())
                && Float.isFinite(location.getPitch());
    }
            }
