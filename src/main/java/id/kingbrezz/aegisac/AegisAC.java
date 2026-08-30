package id.kingbrezz.aegisac;

import id.kingbrezz.aegisac.alert.AlertManager;
import id.kingbrezz.aegisac.check.CheckManager;
import id.kingbrezz.aegisac.check.DetectionEngine;
import id.kingbrezz.aegisac.command.AegisCommand;
import id.kingbrezz.aegisac.listener.AegisListener;
import id.kingbrezz.aegisac.manager.ConfigManager;
import id.kingbrezz.aegisac.manager.MessageManager;
import id.kingbrezz.aegisac.player.PlayerDataManager;
import id.kingbrezz.aegisac.punishment.PunishmentManager;
import id.kingbrezz.aegisac.setback.SetbackManager;
import id.kingbrezz.aegisac.violation.ViolationManager;
import id.kingbrezz.aegisac.violation.ViolationProcessor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AegisAC extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private PlayerDataManager playerDataManager;

    private CheckManager checkManager;
    private DetectionEngine detectionEngine;

    private AlertManager alertManager;
    private SetbackManager setbackManager;
    private PunishmentManager punishmentManager;

    private ViolationManager violationManager;
    private ViolationProcessor violationProcessor;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        getLogger().info("Starting AegisAC...");

        /*
         * Core configuration
         */
        configManager = new ConfigManager(this);
        configManager.load();

        messageManager = new MessageManager(this);
        messageManager.load();

        /*
         * Player state
         *
         * PlayerDataManager currently uses a no-argument constructor.
         * Keep this compatible with the existing implementation.
         */
        playerDataManager = new PlayerDataManager();

        /*
         * Core violation system
         */
        violationManager = new ViolationManager(this);
        violationProcessor = new ViolationProcessor(this, violationManager);

        /*
         * Detection / checks
         */
        checkManager = new CheckManager(this);
        detectionEngine = new DetectionEngine(this, checkManager, violationProcessor);

        /*
         * Gameplay response systems
         */
        setbackManager = new SetbackManager(this);
        punishmentManager = new PunishmentManager(this);
        alertManager = new AlertManager(this, messageManager);

        /*
         * Register checks.
         *
         * Concrete checks will be added to CheckManager in the
         * following files/steps. Keeping registration in one place
         * makes future additions predictable.
         */
        registerChecks();

        /*
         * Register Bukkit listeners.
         */
        getServer().getPluginManager().registerEvents(
                new AegisListener(
                        this,
                        detectionEngine,
                        playerDataManager,
                        setbackManager
                ),
                this
        );

        /*
         * Register command.
         */
        registerCommands();

        long elapsed = System.currentTimeMillis() - start;

        getLogger().info("AegisAC enabled successfully in " + elapsed + "ms.");
        getLogger().info("Detection engine: ENABLED");
        getLogger().info("Event listener: ENABLED");
        getLogger().info("Checks registered: " + checkManager.getChecks().size());
    }

    private void registerChecks() {
        /*
         * Concrete checks are intentionally registered from here.
         *
         * We will populate this method as each production check is
         * introduced. This prevents checks from being instantiated
         * before the core managers are ready.
         */
        checkManager.registerChecks();
    }

    private void registerCommands() {
        PluginCommand command = getCommand("aegisac");

        if (command == null) {
            getLogger().severe("Command 'aegisac' is missing from plugin.yml.");
            return;
        }

        AegisCommand aegisCommand = new AegisCommand(this);

        command.setExecutor(aegisCommand);
        command.setTabCompleter(aegisCommand);
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling AegisAC...");

        if (playerDataManager != null) {
            playerDataManager.clear();
        }

        getLogger().info("AegisAC disabled.");
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

    public DetectionEngine getDetectionEngine() {
        return detectionEngine;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public SetbackManager getSetbackManager() {
        return setbackManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public ViolationManager getViolationManager() {
        return violationManager;
    }

    public ViolationProcessor getViolationProcessor() {
        return violationProcessor;
    }
            }
