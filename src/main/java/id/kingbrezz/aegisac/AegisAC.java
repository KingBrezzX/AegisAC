package id.kingbrezz.aegisac;

import id.kingbrezz.aegisac.alert.AlertManager;
import id.kingbrezz.aegisac.check.CheckManager;
import id.kingbrezz.aegisac.command.AegisCommand;
import id.kingbrezz.aegisac.listener.AegisListener;
import id.kingbrezz.aegisac.manager.ConfigManager;
import id.kingbrezz.aegisac.manager.MessageManager;
import id.kingbrezz.aegisac.player.PlayerDataManager;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class AegisAC extends JavaPlugin {

    private static AegisAC instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    private AlertManager alertManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);

        // PlayerDataManager menggunakan constructor tanpa argument.
        playerDataManager = new PlayerDataManager();

        checkManager = new CheckManager(this);

        // AlertManager membutuhkan plugin + MessageManager.
        alertManager = new AlertManager(
                this,
                messageManager
        );

        registerListeners();
        registerCommands();

        getLogger().info("AegisAC enabled successfully.");
    }

    @Override
    public void onDisable() {
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

        if (instance == this) {
            instance = null;
        }

        getLogger().info("AegisAC disabled.");
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(
                new AegisListener(this),
                this
        );
    }

    private void registerCommands() {
        PluginCommand command = getCommand("aegisac");

        if (command == null) {
            getLogger().severe(
                    "Command 'aegisac' is missing from plugin.yml."
            );
            return;
        }

        AegisCommand aegisCommand = new AegisCommand(
                this,
                messageManager,
                checkManager,
                playerDataManager
        );

        command.setExecutor(aegisCommand);
        command.setTabCompleter(aegisCommand);
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

    public static AegisAC getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "AegisAC is not enabled."
            );
        }

        return instance;
    }
            }
