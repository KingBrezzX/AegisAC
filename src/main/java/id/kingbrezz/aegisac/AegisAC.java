package id.kingbrezz.aegisac;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class AegisAC extends JavaPlugin {

    private static AegisAC instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("messages.yml", false);

        getLogger().info("AegisAC v" + getDescription().getVersion() + " is starting...");
        getLogger().info("Author: KingBrezz");
        getLogger().info("Platform: Paper");
        getLogger().info("Target: Minecraft Java Edition");

        registerCommands();

        getLogger().info("AegisAC has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("AegisAC has been disabled.");

        if (instance == this) {
            instance = null;
        }
    }

    private void registerCommands() {
        PluginCommand command = getCommand("aegisac");

        if (command == null) {
            getLogger().log(
                    Level.SEVERE,
                    "Unable to register /aegisac. The command is missing from plugin.yml."
            );
            return;
        }

        /*
         * The command executor will be attached when the command
         * module is added. Keeping registration isolated here
         * prevents command setup from being scattered across the
         * plugin lifecycle.
         */
    }

    public static AegisAC getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AegisAC is not enabled.");
        }

        return instance;
    }
}
