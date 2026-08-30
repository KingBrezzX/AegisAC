package id.kingbrezz.aegisac.listener;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.check.DetectionEngine;
import id.kingbrezz.aegisac.player.PlayerDataManager;
import id.kingbrezz.aegisac.setback.SetbackManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public final class AegisListener implements Listener {

    private final AegisAC plugin;
    private final DetectionEngine detectionEngine;
    private final PlayerDataManager playerDataManager;
    private final SetbackManager setbackManager;

    public AegisListener(
            AegisAC plugin,
            DetectionEngine detectionEngine,
            PlayerDataManager playerDataManager,
            SetbackManager setbackManager
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.detectionEngine = Objects.requireNonNull(
                detectionEngine,
                "detectionEngine"
        );
        this.playerDataManager = Objects.requireNonNull(
                playerDataManager,
                "playerDataManager"
        );
        this.setbackManager = Objects.requireNonNull(
                setbackManager,
                "setbackManager"
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        Location location = player.getLocation();

        data.markValidLocation(location);

        /*
         * Seed the movement state without generating a false detection
         * immediately after joining.
         */
        data.updateMovement(player);
        data.markValidLocation(location);

        detectionEngine.handleJoin(player);
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        /*
         * Ignore pure head rotation. There is no movement to analyse.
         */
        if (samePosition(from, to)) {
            detectionEngine.handleRotation(player, from, to);
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        /*
         * DetectionEngine receives the movement BEFORE the current
         * position becomes the new safe position.
         */
        boolean suspicious = detectionEngine.handleMove(
                player,
                from,
                to
        );

        data.updateMovement(player);

        /*
         * Only a movement accepted by the detection engine becomes
         * the new setback location.
         */
        if (!suspicious && !isUnsafeLocation(to)) {
            data.markValidLocation(to);
        }
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        Location destination = event.getTo();

        if (destination == null) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        /*
         * Teleports are legitimate state changes. Do not let the next
         * movement check compare against an old world/location.
         */
        data.markValidLocation(destination);
        data.updateMovement(player);

        detectionEngine.handleTeleport(
                player,
                event.getFrom(),
                destination,
                event.getCause()
        );

        /*
         * A teleport must never be treated as an automatic violation.
         * It resets the movement baseline.
         */
        setbackManager.clearPending(player);
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        data.markValidLocation(player.getLocation());
        data.updateMovement(player);

        detectionEngine.handleWorldChange(player);
        setbackManager.clearPending(player);
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        data.markAttack();

        detectionEngine.handleAttack(
                player,
                event.getEntity(),
                event
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        data.markPlace();

        detectionEngine.handleBlockPlace(
                player,
                event
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        data.markBreak();

        detectionEngine.handleBlockBreak(
                player,
                event
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        detectionEngine.handleDeath(player);

        setbackManager.clearPending(player);
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onKick(PlayerKickEvent event) {
        cleanup(event.getPlayer());
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onQuit(PlayerQuitEvent event) {
        cleanup(event.getPlayer());
    }

    private void cleanup(Player player) {
        if (player == null) {
            return;
        }

        detectionEngine.handleQuit(player);
        setbackManager.clearPending(player);
        playerDataManager.remove(player);
    }

    private boolean samePosition(Location first, Location second) {
        if (first == null || second == null) {
            return true;
        }

        return first.getX() == second.getX()
                && first.getY() == second.getY()
                && first.getZ() == second.getZ();
    }

    private boolean isUnsafeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return true;
        }

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return !Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(z);
    }
    }
