package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public final class DetectionEngine {

    private final AegisAC plugin;
    private final CheckManager checkManager;
    private final PlayerDataManager playerDataManager;

    public DetectionEngine(
            AegisAC plugin,
            CheckManager checkManager,
            PlayerDataManager playerDataManager
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.checkManager = Objects.requireNonNull(
                checkManager,
                "checkManager"
        );
        this.playerDataManager = Objects.requireNonNull(
                playerDataManager,
                "playerDataManager"
        );
    }

    public void handleJoin(Player player) {
        if (player == null) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        data.resetDetectionState();
        data.updateMovement(player);

        checkManager.handleJoin(player);
    }

    /**
     * Processes a player movement.
     *
     * @return true when at least one check produced a violation
     */
    public boolean handleMove(
            Player player,
            Location from,
            Location to
    ) {
        if (player == null || from == null || to == null) {
            return false;
        }

        if (!isFinite(from) || !isFinite(to)) {
            return false;
        }

        if (!sameWorld(from, to)) {
            return false;
        }

        if (!player.isOnline() || player.isDead()) {
            return false;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        data.recordMovement(from, to);

        MovementContext context = new MovementContext(
                player,
                from,
                to,
                data
        );

        return checkManager.handleMove(context);
    }

    public void handleRotation(
            Player player,
            Location from,
            Location to
    ) {
        if (player == null || from == null || to == null) {
            return;
        }

        if (!isFinite(from) || !isFinite(to)) {
            return;
        }

        if (!player.isOnline() || player.isDead()) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        data.recordRotation(from, to);

        checkManager.handleRotation(
                player,
                from,
                to,
                data
        );
    }

    public void handleTeleport(
            Player player,
            Location from,
            Location to,
            PlayerTeleportEvent.TeleportCause cause
    ) {
        if (player == null || to == null) {
            return;
        }

        if (!isFinite(to)) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        data.resetMovement(to);

        checkManager.handleTeleport(
                player,
                from,
                to,
                cause,
                data
        );
    }

    public void handleWorldChange(Player player) {
        if (player == null) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        Location location = player.getLocation();

        if (!isFinite(location)) {
            return;
        }

        data.resetMovement(location);

        checkManager.handleWorldChange(
                player,
                data
        );
    }

    public void handleAttack(
            Player player,
            Entity target,
            EntityDamageByEntityEvent event
    ) {
        if (player == null || target == null || event == null) {
            return;
        }

        if (!player.isOnline() || player.isDead()) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        checkManager.handleAttack(
                player,
                target,
                event,
                data
        );
    }

    public void handleBlockPlace(
            Player player,
            BlockPlaceEvent event
    ) {
        if (player == null || event == null) {
            return;
        }

        if (!player.isOnline() || player.isDead()) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        checkManager.handleBlockPlace(
                player,
                event,
                data
        );
    }

    public void handleBlockBreak(
            Player player,
            BlockBreakEvent event
    ) {
        if (player == null || event == null) {
            return;
        }

        if (!player.isOnline() || player.isDead()) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        checkManager.handleBlockBreak(
                player,
                event,
                data
        );
    }

    public void handleDeath(Player player) {
        if (player == null) {
            return;
        }

        PlayerDataManager.PlayerData data =
                playerDataManager.get(player);

        data.resetDetectionState();

        checkManager.handleDeath(
                player,
                data
        );
    }

    public void handleQuit(Player player) {
        if (player == null) {
            return;
        }

        checkManager.handleQuit(player);
    }

    private boolean sameWorld(
            Location first,
            Location second
    ) {
        if (first.getWorld() == null
                || second.getWorld() == null) {
            return false;
        }

        return first.getWorld().equals(second.getWorld());
    }

    private boolean isFinite(Location location) {
        return Double.isFinite(location.getX())
                && Double.isFinite(location.getY())
                && Double.isFinite(location.getZ())
                && Float.isFinite(location.getYaw())
                && Float.isFinite(location.getPitch());
    }

    public record MovementContext(
            Player player,
            Location from,
            Location to,
            PlayerDataManager.PlayerData data
    ) {
    }
            }
