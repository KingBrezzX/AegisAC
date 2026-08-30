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

/**
 * Base implementation shared by all AegisAC checks.
 *
 * The class deliberately keeps detection state inside the check instead
 * of mixing detection, punishment and event registration together.
 */
public abstract class AbstractCheck implements Check {

    protected final AegisAC plugin;

    private final String name;
    private final String displayName;
    private final CheckType type;

    private boolean enabled;

    protected AbstractCheck(
            AegisAC plugin,
            String name,
            CheckType type
    ) {
        this(
                plugin,
                name,
                name,
                type
        );
    }

    protected AbstractCheck(
            AegisAC plugin,
            String name,
            String displayName,
            CheckType type
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.name = Objects.requireNonNull(name, "name");
        this.displayName = Objects.requireNonNull(
                displayName,
                "displayName"
        );
        this.type = Objects.requireNonNull(type, "type");

        this.enabled = loadEnabledState();
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getDisplayName() {
        return displayName;
    }

    @Override
    public final CheckType getType() {
        return type;
    }

    @Override
    public boolean isEnabled() {
        return enabled && plugin.isEnabled();
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Reloads the enabled state from configuration.
     */
    public void reload() {
        this.enabled = loadEnabledState();
        onReload();
    }

    protected void onReload() {
    }

    /*
     * ------------------------------------------------------------
     * Event capability defaults
     * ------------------------------------------------------------
     */

    @Override
    public boolean isMovementCheck() {
        return type == CheckType.MOVEMENT;
    }

    @Override
    public boolean isRotationCheck() {
        return false;
    }

    @Override
    public boolean isCombatCheck() {
        return type == CheckType.COMBAT;
    }

    @Override
    public boolean isBlockCheck() {
        return type == CheckType.BLOCK;
    }

    /*
     * ------------------------------------------------------------
     * Lifecycle hooks
     * ------------------------------------------------------------
     */

    @Override
    public void onJoin(Player player) {
        if (player != null) {
            handleJoin(player);
        }
    }

    @Override
    public void onQuit(Player player) {
        if (player != null) {
            handleQuit(player);
        }
    }

    @Override
    public void onDeath(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
        if (player != null) {
            handleDeath(player, data);
        }
    }

    @Override
    public void onWorldChange(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
        if (player != null) {
            handleWorldChange(player, data);
        }
    }

    @Override
    public void onTeleport(
            Player player,
            Location from,
            Location to,
            PlayerTeleportEvent.TeleportCause cause,
            PlayerDataManager.PlayerData data
    ) {
        if (player != null) {
            handleTeleport(
                    player,
                    from,
                    to,
                    cause,
                    data
            );
        }
    }

    /*
     * ------------------------------------------------------------
     * Detection hooks
     * ------------------------------------------------------------
     */

    @Override
    public boolean onMove(
            DetectionEngine.MovementContext context
    ) {
        if (context == null || !isEnabled()) {
            return false;
        }

        return handleMove(context);
    }

    @Override
    public void onRotation(
            Player player,
            Location from,
            Location to,
            PlayerDataManager.PlayerData data
    ) {
        if (isEnabled()) {
            handleRotation(
                    player,
                    from,
                    to,
                    data
            );
        }
    }

    @Override
    public void onAttack(
            Player player,
            Entity target,
            EntityDamageByEntityEvent event,
            PlayerDataManager.PlayerData data
    ) {
        if (isEnabled()) {
            handleAttack(
                    player,
                    target,
                    event,
                    data
            );
        }
    }

    @Override
    public void onBlockPlace(
            Player player,
            BlockPlaceEvent event,
            PlayerDataManager.PlayerData data
    ) {
        if (isEnabled()) {
            handleBlockPlace(
                    player,
                    event,
                    data
            );
        }
    }

    @Override
    public void onBlockBreak(
            Player player,
            BlockBreakEvent event,
            PlayerDataManager.PlayerData data
    ) {
        if (isEnabled()) {
            handleBlockBreak(
                    player,
                    event,
                    data
            );
        }
    }

    /*
     * ------------------------------------------------------------
     * Override points for concrete checks
     * ------------------------------------------------------------
     */

    protected void handleJoin(Player player) {
    }

    protected void handleQuit(Player player) {
    }

    protected void handleDeath(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
    }

    protected void handleWorldChange(
            Player player,
            PlayerDataManager.PlayerData data
    ) {
    }

    protected void handleTeleport(
            Player player,
            Location from,
            Location to,
            PlayerTeleportEvent.TeleportCause cause,
            PlayerDataManager.PlayerData data
    ) {
    }

    protected void handleRotation(
            Player player,
            Location from,
            Location to,
            PlayerDataManager.PlayerData data
    ) {
    }

    /**
     * Concrete movement checks return true only when their detection
     * logic has actually reached a violation condition.
     */
    protected boolean handleMove(
            DetectionEngine.MovementContext context
    ) {
        return false;
    }

    protected void handleAttack(
            Player player,
            Entity target,
            EntityDamageByEntityEvent event,
            PlayerDataManager.PlayerData data
    ) {
    }

    protected void handleBlockPlace(
            Player player,
            BlockPlaceEvent event,
            PlayerDataManager.PlayerData data
    ) {
    }

    protected void handleBlockBreak(
            Player player,
            BlockBreakEvent event,
            PlayerDataManager.PlayerData data
    ) {
    }

    /*
     * ------------------------------------------------------------
     * Configuration
     * ------------------------------------------------------------
     */

    private boolean loadEnabledState() {
        /*
         * Keep the base class safe if a custom ConfigManager is temporarily
         * unavailable during plugin bootstrap.
         *
         * Concrete checks can override reload()/isEnabled() when they need
         * more detailed configuration.
         */
        try {
            if (plugin.getConfigManager() == null) {
                return true;
            }

            String path = "checks." + name + ".enabled";

            return plugin.getConfigManager()
                    .getBoolean(path, true);

        } catch (Throwable ignored) {
            return true;
        }
    }

    protected final boolean getBoolean(
            String path,
            boolean def
    ) {
        try {
            return plugin.getConfigManager()
                    .getBoolean(path, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

    protected final int getInt(
            String path,
            int def
    ) {
        try {
            return plugin.getConfigManager()
                    .getInt(path, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

    protected final long getLong(
            String path,
            long def
    ) {
        try {
            return plugin.getConfigManager()
                    .getLong(path, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

    protected final double getDouble(
            String path,
            double def
    ) {
        try {
            return plugin.getConfigManager()
                    .getDouble(path, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

    protected final String getString(
            String path,
            String def
    ) {
        try {
            return plugin.getConfigManager()
                    .getString(path, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

    /**
     * Returns the configuration prefix for this check.
     */
    protected final String configPath() {
        return "checks." + name;
    }
    }
