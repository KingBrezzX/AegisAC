package id.kingbrezz.aegisac.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataManager {

    private final Map<UUID, PlayerData> players =
            new ConcurrentHashMap<>();

    /**
     * Gets existing data or creates a new data object.
     */
    public PlayerData get(Player player) {
        if (player == null) {
            throw new IllegalArgumentException(
                    "player cannot be null"
            );
        }

        return get(player.getUniqueId());
    }

    /**
     * Gets existing data or creates a new data object.
     */
    public PlayerData get(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException(
                    "uuid cannot be null"
            );
        }

        return players.computeIfAbsent(
                uuid,
                PlayerData::new
        );
    }

    /**
     * Checks whether the player is currently tracked.
     */
    public boolean contains(UUID uuid) {
        return uuid != null
                && players.containsKey(uuid);
    }

    /**
     * Removes player data.
     */
    public void remove(Player player) {
        if (player != null) {
            remove(player.getUniqueId());
        }
    }

    /**
     * Removes player data.
     */
    public void remove(UUID uuid) {
        if (uuid != null) {
            players.remove(uuid);
        }
    }

    /**
     * Number of tracked players.
     */
    public int size() {
        return players.size();
    }

    /**
     * Clears all runtime detection data.
     */
    public void shutdown() {
        players.clear();
    }

    /**
     * Runtime data belonging to one player.
     */
    public static final class PlayerData {

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

        private PlayerData(UUID uuid) {
            this.uuid = uuid;

            long now = System.currentTimeMillis();

            this.lastMoveTime = now;
            this.lastRotationTime = now;

            this.ticksSinceTeleport = 100;
            this.ticksSinceJoin = 0;
        }

        public UUID getUuid() {
            return uuid;
        }

        /*
         * --------------------------------------------------------
         * MOVEMENT
         * --------------------------------------------------------
         */

        public void updateMovement(Player player) {
            if (player == null) {
                return;
            }

            Location location = player.getLocation();

            if (!isFinite(location)) {
                return;
            }

            lastOnGround = onGround;
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

            double deltaX =
                    to.getX() - from.getX();

            double deltaY =
                    to.getY() - from.getY();

            double deltaZ =
                    to.getZ() - from.getZ();

            horizontalDistance = Math.sqrt(
                    deltaX * deltaX
                            + deltaZ * deltaZ
            );

            verticalDistance = deltaY;

            lastOnGround = onGround;

            lastLocation = to.clone();

            lastYaw = to.getYaw();
            lastPitch = to.getPitch();

            lastMoveTime =
                    System.currentTimeMillis();

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

        public void resetMovement(
                Location location
        ) {
            if (location == null
                    || !isFinite(location)) {
                return;
            }

            lastLocation = location.clone();
            lastValidLocation = location.clone();

            horizontalDistance = 0.0D;
            verticalDistance = 0.0D;

            lastYaw = location.getYaw();
            lastPitch = location.getPitch();

            lastMoveTime =
                    System.currentTimeMillis();

            ticksSinceTeleport = 0;
            recentlyTeleported = true;
        }

        public void resetDetectionState() {
            horizontalDistance = 0.0D;
            verticalDistance = 0.0D;

            ticksSinceTeleport = 0;
            ticksSinceJoin = 0;

            recentlyTeleported = false;

            lastAttackTime = 0L;

            long now =
                    System.currentTimeMillis();

            lastMoveTime = now;
            lastRotationTime = now;

            if (lastLocation != null) {
                lastValidLocation =
                        lastLocation.clone();
            }
        }

        /*
         * --------------------------------------------------------
         * COMBAT
         * --------------------------------------------------------
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
         * --------------------------------------------------------
         * LOCATION
         * --------------------------------------------------------
         */

        public Location getLastLocation() {
            return cloneLocation(lastLocation);
        }

        public Location getLastValidLocation() {
            return cloneLocation(
                    lastValidLocation
            );
        }

        public void setLastValidLocation(
                Location location
        ) {
            if (location == null
                    || !isFinite(location)) {
                return;
            }

            lastValidLocation =
                    location.clone();
        }

        /*
         * --------------------------------------------------------
         * DISTANCE
         * --------------------------------------------------------
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
         * --------------------------------------------------------
         * ROTATION
         * --------------------------------------------------------
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
         * --------------------------------------------------------
         * GROUND
         * --------------------------------------------------------
         */

        public boolean isOnGround() {
            return onGround;
        }

        public boolean wasOnGround() {
            return lastOnGround;
        }

        public void setOnGround(
                boolean onGround
        ) {
            this.lastOnGround = this.onGround;
            this.onGround = onGround;
        }

        /*
         * --------------------------------------------------------
         * TELEPORT / JOIN
         * --------------------------------------------------------
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
         * --------------------------------------------------------
         * EXEMPTION
         * --------------------------------------------------------
         */

        public boolean isExempt() {
            return exempt;
        }

        public void setExempt(
                boolean exempt
        ) {
            this.exempt = exempt;
        }

        /*
         * --------------------------------------------------------
         * TIME
         * --------------------------------------------------------
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
         * --------------------------------------------------------
         * INTERNAL
         * --------------------------------------------------------
         */

        private static Location cloneLocation(
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
                    && Double.isFinite(
                    location.getX()
            )
                    && Double.isFinite(
                    location.getY()
            )
                    && Double.isFinite(
                    location.getZ()
            )
                    && Float.isFinite(
                    location.getYaw()
            )
                    && Float.isFinite(
                    location.getPitch()
            );
        }
    }
            }
