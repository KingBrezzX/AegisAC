package id.kingbrezz.aegisac.listener;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.check.DetectionEngine;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public final class AegisListener implements Listener {

    private final AegisAC plugin;
    private final DetectionEngine detectionEngine;

    public AegisListener(AegisAC plugin) {
        this.plugin = plugin;
        this.detectionEngine = new DetectionEngine(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        detectionEngine.handleJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        detectionEngine.handleQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            return;
        }

        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        Player player = event.getPlayer();
        detectionEngine.handleMove(event);
    }
}
