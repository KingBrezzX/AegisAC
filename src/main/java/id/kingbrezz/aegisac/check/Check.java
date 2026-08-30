package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.player.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Base contract for every AegisAC detection check.
 *
 * Checks should remain lightweight and must never perform
 * expensive synchronous operations on the server thread.
 */
public interface Check {

    /**
     * Technical name of the check.
     */
    String getName();

    /**
     * Whether this check is currently enabled.
     */
    boolean isEnabled();

    /**
     * Enables or disables the check.
     */
    void setEnabled(boolean enabled);

    /**
     * Whether this check receives movement callbacks.
     */
    default boolean isMovementCheck() {
        return false;
    }

    /**
     * Whether this check receives rotation callbacks.
     */
    default boolean isRotationCheck() {
        return false;
    }

    /**
     * Whether this check receives combat callbacks.
     */
    default boolean isCombatCheck() {
        return false;
    }

    /**
     * Whether this check receives block callbacks.
     */
    default boolean isBlockCheck() {
        return false;
    }

    /**
     * Called when a player joins.
     */
    default void onJoin(Player player) {
    }

    /**
     * Called for movement checks.
     *
     * @return true when a violation was produced
     */
    default boolean onMove(
            DetectionEngine.MovementContext context
    ) {
        return false;
    }

    /**
     * Called for rotation checks.
     */
    default void onRotation(
            Player player,
            Location from,
            Location to,
            PlayerDataManager.PlayerData data
    ) {
    }

    /**
     * Called after a teleport.
     */
    default void onTeleport(
            Player player,
            Location from,
            Location to,
            PlayerTeleportEvent.TeleportCause cause,
            PlayerDataManager.PlayerData data
    ) {
    }

    /**
     * Called after a world change.
     */
    default void onWorldChange(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
    }

    /**
     * Called for entity attacks.
     */
    default void onAttack(
            Player player,
            Entity target,
            EntityDamageByEntityEvent event,
            PlayerDataManager.PlayerData data
    ) {
    }

    /**
     * Called when a block is placed.
     */
    default void onBlockPlace(
            Player player,
            BlockPlaceEvent event,
            PlayerDataManager.PlayerData data
    ) {
    }

    /**
     * Called when a block is broken.
     */
    default void onBlockBreak(
            Player player,
            BlockBreakEvent event,
            PlayerDataManager.PlayerData data
    ) {
    }

    /**
     * Called when a player dies.
     */
    default void onDeath(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
    }

    /**
     * Called when a player quits.
     *
     * Checks should use this to clear temporary per-player state.
     */
    default void onQuit(Player player) {
    }
        }
