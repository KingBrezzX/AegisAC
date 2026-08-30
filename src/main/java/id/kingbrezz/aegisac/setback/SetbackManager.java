package id.kingbrezz.aegisac.setback;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SetbackManager {

    private final AegisAC plugin;

    private final Map<UUID, Long> lastSetback =
            new ConcurrentHashMap<>();

    private final Map<UUID, Integer> setbackCounts =
            new ConcurrentHashMap<>();

    public SetbackManager(AegisAC plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin"
        );
    }

    public boolean setback(
            Player player,
            PlayerData data,
            String checkName
    ) {
        if (player == null || data == null) {
            return false;
        }

        if (!player.isOnline()) {
            return false;
        }

        if (!plugin.getConfigManager().isSetbackEnabled()) {
            return false;
        }

        if (data.isExempt() || data.isTemporarilyExempt()) {
            return false;
        }

        if (checkName == null || checkName.isBlank()) {
            return false;
        }

        if (!canSetback(player)) {
            return false;
        }

        Location safe = data.getLastSafeLocation();

        if (safe == null
                || safe.getWorld() == null) {
            safe = data.getLastLocation();
        }

        if (safe == null
                || safe.getWorld() == null) {
            return false;
        }

        if (!safe.getWorld().equals(
                player.getWorld()
        )) {
            return false;
        }

        Location target = safe.clone();

        target.setYaw(player.getLocation().getYaw());
        target.setPitch(player.getLocation().getPitch());

        boolean success = player.teleport(target);

        if (!success) {
            return false;
        }

        long now = System.currentTimeMillis();

        lastSetback.put(
                player.getUniqueId(),
                now
        );

        setbackCounts.merge(
                player.getUniqueId(),
                1,
                Integer::sum
        );

        data.incrementSetbackCount(checkName);

        return true;
    }

    public void updateSafeLocation(
            Player player,
            PlayerData data
    ) {
        if (player == null || data == null) {
            return;
        }

        if (!player.isOnline()) {
            return;
        }

        Location location = player.getLocation();

        if (location.getWorld() == null) {
            return;
        }

        if (!isSafeLocation(player)) {
            return;
        }

        data.setLastSafeLocation(location);
    }

    private boolean isSafeLocation(Player player) {
        if (player.isDead()) {
            return false;
        }

        if (!player.isOnGround()) {
            return false;
        }

        if (player.isInsideVehicle()) {
            return false;
        }

        return !player.isGliding();
    }

    private boolean canSetback(Player player) {
        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();

        long last = lastSetback.getOrDefault(
                uuid,
                0L
        );

        long interval =
                plugin.getConfigManager()
                        .getSetbackIntervalMillis();

        if (last > 0L
                && now - last < interval) {
            return false;
        }

        int maximum =
                plugin.getConfigManager()
                        .getMaximumSetbacksPerInterval();

        int count = setbackCounts.getOrDefault(
                uuid,
                0
        );

        if (count >= maximum) {
            return false;
        }

        return true;
    }

    public long getLastSetbackTime(Player player) {
        if (player == null) {
            return 0L;
        }

        return lastSetback.getOrDefault(
                player.getUniqueId(),
                0L
        );
    }

    public int getSetbackCount(Player player) {
        if (player == null) {
            return 0;
        }

        return setbackCounts.getOrDefault(
                player.getUniqueId(),
                0
        );
    }

    public void reset(Player player) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();

        lastSetback.remove(uuid);
        setbackCounts.remove(uuid);
    }

    public void resetAll() {
        lastSetback.clear();
        setbackCounts.clear();
    }

    public void remove(Player player) {
        reset(player);
    }

    public AegisAC getPlugin() {
        return plugin;
    }
            }
