package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.player.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public interface Check {

    /**
     * Unique technical name of this check.
     */
    String getName();

    /**
     * Human-readable name displayed in alerts.
     */
    default String getDisplayName() {
        return getName();
    }

    /**
     * Check category.
     */
    default CheckType getType() {
        return CheckType.OTHER;
    }

    /**
     * Whether this check is currently enabled.
     */
    default boolean isEnabled() {
        return true;
    }

    /*
     * ------------------------------------------------------------
     * Capability flags
     * ------------------------------------------------------------
     */

    default boolean isMovementCheck() {
        return false;
    }

    default boolean isRotationCheck() {
        return false;
    }

    default boolean isCombatCheck() {
        return false;
    }

    default boolean isBlockCheck() {
        return false;
    }

    /*
     * ------------------------------------------------------------
     * Player lifecycle
     * ------------------------------------------------------------
     */

    default void onJoin(Player player) {
    }

    default void onQuit(Player player) {
    }

    default void onDeath(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
    }

    /*
     * ------------------------------------------------------------
     * Movement
     * ------------------------------------------------------------
     */

    /**
     * @return true if this movement produced a violation.
     */
    default boolean onMove(
            DetectionEngine.MovementContext context
    ) {
        return false;
    }

    default void onRotation(
            Player player,
            Location from,
            Location to,
            PlayerDataManager.PlayerData data
    ) {
    }

    default void onTeleport(
            Player player,
            Location from,
            Location to,
            PlayerTeleportEvent.TeleportCause cause,
            PlayerDataManager.PlayerData data
    ) {
    }

    default void onWorldChange(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
    }

    /*
     * ------------------------------------------------------------
     * Combat
     * ------------------------------------------------------------
     */

    default void onAttack(
            Player player,
            Entity target,
            EntityDamageByEntityEvent event,
            PlayerDataManager.PlayerData data
    ) {
    }

    /*
     * ------------------------------------------------------------
     * Block interaction
     * ------------------------------------------------------------
     */

    default void onBlockPlace(
            Player player,
            BlockPlaceEvent event,
            PlayerDataManager.PlayerData data
    ) {
    }

    default void onBlockBreak(
            Player player,
            BlockBreakEvent event,
            PlayerDataManager.PlayerData data
    ) {
    }

    /*
     * ------------------------------------------------------------
     * Check type
     * ------------------------------------------------------------
     */

    enum CheckType {
        MOVEMENT,
        COMBAT,
        PLAYER,
        WORLD,
        PACKET,
        BLOCK,
        OTHER
    }
        }
