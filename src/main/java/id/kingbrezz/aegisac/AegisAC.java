package id.kingbrezz.aegisac;

import id.kingbrezz.aegisac.alert.AlertManager;
import id.kingbrezz.aegisac.check.CheckManager;
import id.kingbrezz.aegisac.check.DetectionEngine;
import id.kingbrezz.aegisac.command.AegisCommand;
import id.kingbrezz.aegisac.listener.AegisListener;
import id.kingbrezz.aegisac.manager.ConfigManager;
import id.kingbrezz.aegisac.manager.MessageManager;
import id.kingbrezz.aegisac.player.PlayerDataManager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class AegisAC extends JavaPlugin {

    private static AegisAC instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    private AlertManager alertManager;
    private DetectionEngine detectionEngine;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info(
                "Starting AegisAC v"
                        + getDescription().getVersion()
                        + "..."
        );

        saveDefaultConfig();
        saveResource("messages.yml", false);

        /*
         * ---------------------------------------------------------
         * MANAGERS
         * ---------------------------------------------------------
         */

        configManager = new ConfigManager(this);

        messageManager = new MessageManager(this);

        playerDataManager = new PlayerDataManager();

        checkManager = new CheckManager(this);

        alertManager = new AlertManager(
                this,
                messageManager
        );

        /*
         * ---------------------------------------------------------
         * DETECTION ENGINE
         * ---------------------------------------------------------
         *
         * There is only ONE engine instance for the entire plugin.
         */

        detectionEngine = new DetectionEngine(
                this,
                checkManager,
                playerDataManager
        );

        /*
         * ---------------------------------------------------------
         * LISTENERS
         * ---------------------------------------------------------
         */

        registerListeners();

        /*
         * ---------------------------------------------------------
         * COMMANDS
         * ---------------------------------------------------------
         */

        registerCommands();

        getLogger().info(
                "AegisAC enabled successfully."
        );
    }

    @Override
    public void onDisable() {
        getLogger().info(
                "Disabling AegisAC..."
        );

        if (playerDataManager != null) {
            try {
                playerDataManager.shutdown();
            } catch (Throwable throwable) {
                getLogger().log(
                        Level.WARNING,
                        "Failed to shutdown PlayerDataManager.",
                        throwable
                );
            }
        }

        detectionEngine = null;
        alertManager = null;
        checkManager = null;
        playerDataManager = null;
        messageManager = null;
        configManager = null;

        if (instance == this) {
            instance = null;
        }

        getLogger().info(
                "AegisAC disabled."
        );
    }

    private void registerListeners() {
        PluginManager pluginManager =
                getServer().getPluginManager();

        pluginManager.registerEvents(
                new AegisListener(this),
                this
        );

        getLogger().info(
                "AegisAC listeners registered."
        );
    }

    private void registerCommands() {
        PluginCommand command =
                getCommand("aegisac");

        if (command == null) {
            getLogger().log(
                    Level.SEVERE,
                    "Command 'aegisac' is missing from plugin.yml."
            );
            return;
        }

        AegisCommand aegisCommand =
                new AegisCommand(
                        this,
                        messageManager,
                        checkManager,
                        playerDataManager
                );

        command.setExecutor(aegisCommand);
        command.setTabCompleter(aegisCommand);

        getLogger().info(
                "AegisAC command registered."
        );
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public DetectionEngine getDetectionEngine() {
        return detectionEngine;
    }

    public static AegisAC getInstance() {
        return Objects.requireNonNull(
                instance,
                "AegisAC is not enabled."
        );
    }
    }
