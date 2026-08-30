package id.kingbrezz.aegisac.listener;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.check.DetectionEngine;

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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class AegisListener implements Listener {

    private final AegisAC plugin;
    private final DetectionEngine detectionEngine;

    public AegisListener(AegisAC plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException(
                    "plugin cannot be null"
            );
        }

        this.plugin = plugin;
        this.detectionEngine =
                plugin.getDetectionEngine();
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onJoin(PlayerJoinEvent event) {
        if (event == null) {
            return;
        }

        detectionEngine.handleJoin(
                event.getPlayer()
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onQuit(PlayerQuitEvent event) {
        if (event == null) {
            return;
        }

        detectionEngine.handleQuit(
                event.getPlayer()
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onMove(PlayerMoveEvent event) {
        if (event == null
                || event.getTo() == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.isOnline()
                || player.isDead()) {
            return;
        }

        if (event.getFrom().getWorld() == null
                || event.getTo().getWorld() == null) {
            return;
        }

        /*
         * A world change is handled by PlayerChangedWorldEvent.
         */
        if (!event.getFrom().getWorld()
                .equals(event.getTo().getWorld())) {
            return;
        }

        boolean positionChanged =
                event.getFrom().getX()
                        != event.getTo().getX()
                || event.getFrom().getY()
                        != event.getTo().getY()
                || event.getFrom().getZ()
                        != event.getTo().getZ();

        boolean rotationChanged =
                event.getFrom().getYaw()
                        != event.getTo().getYaw()
                || event.getFrom().getPitch()
                        != event.getTo().getPitch();

        /*
         * Ignore completely identical movement events.
         */
        if (!positionChanged && !rotationChanged) {
            return;
        }

        /*
         * Movement and rotation are intentionally separated.
         */
        if (positionChanged) {
            detectionEngine.handleMove(
                    player,
                    event.getFrom(),
                    event.getTo()
            );
        }

        if (rotationChanged) {
            detectionEngine.handleRotation(
                    player,
                    event.getFrom(),
                    event.getTo()
            );
        }
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onTeleport(
            PlayerTeleportEvent event
    ) {
        if (event == null
                || event.getTo() == null) {
            return;
        }

        detectionEngine.handleTeleport(
                event.getPlayer(),
                event.getFrom(),
                event.getTo(),
                event.getCause()
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onWorldChange(
            PlayerChangedWorldEvent event
    ) {
        if (event == null) {
            return;
        }

        detectionEngine.handleWorldChange(
                event.getPlayer()
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onAttack(
            EntityDamageByEntityEvent event
    ) {
        if (event == null) {
            return;
        }

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

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
    public void onBlockPlace(
            BlockPlaceEvent event
    ) {
        if (event == null) {
            return;
        }

        detectionEngine.handleBlockPlace(
                event.getPlayer(),
                event
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onBlockBreak(
            BlockBreakEvent event
    ) {
        if (event == null) {
            return;
        }

        detectionEngine.handleBlockBreak(
                event.getPlayer(),
                event
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onDeath(
            PlayerDeathEvent event
    ) {
        if (event == null) {
            return;
        }

        detectionEngine.handleDeath(
                event.getEntity()
        );
    }
                        }
