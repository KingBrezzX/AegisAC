package id.kingbrezz.aegisac.listener;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.check.DetectionEngine;
import id.kingbrezz.aegisac.player.PlayerData;
import id.kingbrezz.aegisac.player.PlayerDataManager;
import id.kingbrezz.aegisac.setback.SetbackManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin"
        );

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

        PlayerData data =
                playerDataManager.get(player);

        data.resetJoinTime();
        data.setLastLocation(
                player.getLocation()
        );

        setbackManager.reset(player);
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        setbackManager.remove(player);

        playerDataManager.remove(player);
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!hasPositionChanged(event)) {
            return;
        }

        PlayerData data =
                playerDataManager.get(player);

        /*
         * The safe location is updated before processing
         * the current movement so a valid position can be
         * used by the setback system.
         */
        setbackManager.updateSafeLocation(
                player,
                data
        );

        detectionEngine.process(player);
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker =
                resolveAttacker(event.getDamager());

        if (attacker == null) {
            return;
        }

        detectionEngine.processCombat(attacker);
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onInteract(PlayerInteractEvent event) {
        detectionEngine.processPlayer(
                event.getPlayer()
        );
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onInventoryClick(
            InventoryClickEvent event
    ) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        detectionEngine.processPlayer(player);
    }

    private boolean hasPositionChanged(
            PlayerMoveEvent event
    ) {
        if (event.getFrom() == null
                || event.getTo() == null) {
            return false;
        }

        if (!event.getFrom().getWorld()
                .equals(event.getTo().getWorld())) {
            return true;
        }

        return event.getFrom().getX()
                != event.getTo().getX()
                || event.getFrom().getY()
                != event.getTo().getY()
                || event.getFrom().getZ()
                != event.getTo().getZ();
    }

    private Player resolveAttacker(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }

        if (entity instanceof Projectile projectile) {
            Object shooter = projectile.getShooter();

            if (shooter instanceof Player player) {
                return player;
            }
        }

        return null;
    }

    public AegisAC getPlugin() {
        return plugin;
    }
          }
