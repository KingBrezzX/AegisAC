package id.kingbrezz.aegisac;

import id.kingbrezz.aegisac.alert.AlertManager;
import id.kingbrezz.aegisac.check.CheckManager;
import id.kingbrezz.aegisac.command.AegisCommand;
import id.kingbrezz.aegisac.manager.ConfigManager;
import id.kingbrezz.aegisac.manager.MessageManager;
import id.kingbrezz.aegisac.player.PlayerDataManager;
import org.bukkit.command.PluginCommand;
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
        saveResource("messages.yml", false);

        getLogger().info(
                "AegisAC v"
                        + getDescription().getVersion()
                        + " is starting..."
        );

        getLogger().info("Author: KingBrezz");
        getLogger().info("Platform: Paper");
        getLogger().info("Target: Minecraft Java Edition");

        /*
         * Core managers
         */
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        playerDataManager = new PlayerDataManager(this);
        checkManager = new CheckManager(this);

        /*
         * Alert system
         */
        alertManager = new AlertManager(
                this
        );

        /*
         * Commands
         */
        registerCommands();

        getLogger().info(
                "AegisAC has been enabled."
        );
    }

    @Override
    public void onDisable() {
        getLogger().info(
                "AegisAC has been disabled."
        );

        if (instance == this) {
            instance = null;
        }
    }

    private void registerCommands() {
        PluginCommand command =
                getCommand("aegisac");

        if (command == null) {
            getLogger().log(
                    Level.SEVERE,
                    "Unable to register /aegisac. "
                            + "The command is missing "
                            + "from plugin.yml."
            );
            return;
        }

        AegisCommand executor =
                new AegisCommand(
                        this,
                        messageManager,
                        checkManager,
                        playerDataManager
                );

        command.setExecutor(executor);
        command.setTabCompleter(executor);
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
