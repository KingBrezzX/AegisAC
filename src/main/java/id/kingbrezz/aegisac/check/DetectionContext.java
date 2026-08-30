package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class DetectionContext {

    private final Player player;
    private final PlayerData data;

    private final Location currentLocation;
    private final Location lastLocation;
    private final Location lastSafeLocation;

    private final long timestamp;
    private final long deltaTime;

    private final double deltaX;
    private final double deltaY;
    private final double deltaZ;
    private final double horizontalDistance;
    private final double distance;

    private final float yaw;
    private final float pitch;

    private final boolean onGround;
    private final boolean insideVehicle;
    private final boolean swimming;
    private final boolean gliding;
    private final boolean flying;
    private final boolean sprinting;

    private final int ping;

    private DetectionContext(
            Player player,
            PlayerData data,
            Location currentLocation,
            Location lastLocation,
            Location lastSafeLocation,
            long timestamp,
            long deltaTime,
            double deltaX,
            double deltaY,
            double deltaZ,
            double horizontalDistance,
            double distance,
            float yaw,
            float pitch,
            boolean onGround,
            boolean insideVehicle,
            boolean swimming,
            boolean gliding,
            boolean flying,
            boolean sprinting,
            int ping
    ) {
        this.player = Objects.requireNonNull(player, "player");
        this.data = Objects.requireNonNull(data, "data");

        this.currentLocation = cloneLocation(currentLocation);
        this.lastLocation = cloneLocation(lastLocation);
        this.lastSafeLocation = cloneLocation(lastSafeLocation);

        this.timestamp = timestamp;
        this.deltaTime = Math.max(0L, deltaTime);

        this.deltaX = sanitize(deltaX);
        this.deltaY = sanitize(deltaY);
        this.deltaZ = sanitize(deltaZ);

        this.horizontalDistance = sanitize(horizontalDistance);
        this.distance = sanitize(distance);

        this.yaw = yaw;
        this.pitch = pitch;

        this.onGround = onGround;
        this.insideVehicle = insideVehicle;
        this.swimming = swimming;
        this.gliding = gliding;
        this.flying = flying;
        this.sprinting = sprinting;

        this.ping = Math.max(0, ping);
    }

    public static DetectionContext create(
            Player player,
            PlayerData data
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(data, "data");

        Location current = player.getLocation();
        Location previous = data.getLastLocation();
        Location safe = data.getLastSafeLocation();

        long now = System.currentTimeMillis();

        long deltaTime = data.getLastMovementTime() <= 0L
                ? 0L
                : now - data.getLastMovementTime();

        double deltaX = 0.0;
        double deltaY = 0.0;
        double deltaZ = 0.0;

        double horizontalDistance = 0.0;
        double distance = 0.0;

        if (previous != null
                && previous.getWorld() != null
                && current.getWorld() != null
                && previous.getWorld().equals(current.getWorld())) {

            deltaX = current.getX() - previous.getX();
            deltaY = current.getY() - previous.getY();
            deltaZ = current.getZ() - previous.getZ();

            horizontalDistance = Math.sqrt(
                    (deltaX * deltaX)
                            + (deltaZ * deltaZ)
            );

            distance = Math.sqrt(
                    (deltaX * deltaX)
                            + (deltaY * deltaY)
                            + (deltaZ * deltaZ)
            );
        }

        return new DetectionContext(
                player,
                data,
                current,
                previous,
                safe,
                now,
                deltaTime,
                deltaX,
                deltaY,
                deltaZ,
                horizontalDistance,
                distance,
                current.getYaw(),
                current.getPitch(),
                player.isOnGround(),
                player.isInsideVehicle(),
                player.isSwimming(),
                player.isGliding(),
                player.isFlying(),
                player.isSprinting(),
                data.getPing()
        );
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerData getData() {
        return data;
    }

    public Location getCurrentLocation() {
        return cloneLocation(currentLocation);
    }

    public Location getLastLocation() {
        return cloneLocation(lastLocation);
    }

    public Location getLastSafeLocation() {
        return cloneLocation(lastSafeLocation);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDeltaTime() {
        return deltaTime;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public double getDeltaZ() {
        return deltaZ;
    }

    public double getHorizontalDistance() {
        return horizontalDistance;
    }

    public double getDistance() {
        return distance;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isInsideVehicle() {
        return insideVehicle;
    }

    public boolean isSwimming() {
        return swimming;
    }

    public boolean isGliding() {
        return gliding;
    }

    public boolean isFlying() {
        return flying;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public int getPing() {
        return ping;
    }

    public boolean hasPreviousLocation() {
        return lastLocation != null;
    }

    public boolean movedHorizontally() {
        return horizontalDistance > 0.0;
    }

    public boolean movedVertically() {
        return Math.abs(deltaY) > 0.0;
    }

    private static double sanitize(double value) {
        return Double.isFinite(value)
                ? value
                : 0.0;
    }

    private static Location cloneLocation(Location location) {
        return location == null
                ? null
                : location.clone();
    }
      }
