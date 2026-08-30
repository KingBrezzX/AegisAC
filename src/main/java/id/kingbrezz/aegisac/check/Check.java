package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerData;
import org.bukkit.entity.Player;

public interface Check {

    /**
     * Returns the unique configuration name of this check.
     */
    String getName();

    /**
     * Returns the human-readable name of this check.
     */
    String getDisplayName();

    /**
     * Returns the category this check belongs to.
     */
    CheckCategory getCategory();

    /**
     * Returns whether this check is currently enabled.
     */
    boolean isEnabled();

    /**
     * Processes the check for a player.
     *
     * @param player the player being checked
     * @param data   runtime data belonging to the player
     */
    void handle(Player player, PlayerData data);

    /**
     * Returns the plugin instance.
     */
    AegisAC getPlugin();

    /**
     * Check categories supported by AegisAC.
     */
    enum CheckCategory {
        MOVEMENT,
        COMBAT,
        PLAYER
    }
}
