package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerData;
import id.kingbrezz.aegisac.player.PlayerDataManager;
import org.bukkit.entity.Player;

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
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin"
        );

        this.checkManager = Objects.requireNonNull(
                checkManager,
                "checkManager"
        );

        this.playerDataManager = Objects.requireNonNull(
                playerDataManager,
                "playerDataManager"
        );
    }

    /**
     * Processes movement-related checks for a player.
     */
    public void process(Player player) {
        if (!canProcess(player)) {
            return;
        }

        PlayerData data = playerDataManager.get(player);

        updatePing(player, data);

        DetectionContext context =
                DetectionContext.create(player, data);

        data.setLastLocation(
                context.getCurrentLocation()
        );

        data.markMovement();

        processChecks(
                player,
                data,
                Check.CheckCategory.MOVEMENT
        );
    }

    /**
     * Processes combat-related checks for a player.
     */
    public void processCombat(Player player) {
        if (!canProcess(player)) {
            return;
        }

        PlayerData data = playerDataManager.get(player);

        updatePing(player, data);

        data.markAttack();

        processChecks(
                player,
                data,
                Check.CheckCategory.COMBAT
        );
    }

    /**
     * Processes player/interaction checks.
     */
    public void processPlayer(Player player) {
        if (!canProcess(player)) {
            return;
        }

        PlayerData data = playerDataManager.get(player);

        updatePing(player, data);

        processChecks(
                player,
                data,
                Check.CheckCategory.PLAYER
        );
    }

    /**
     * Processes every enabled category.
     *
     * This method is intended for controlled server-side
     * processing, not for arbitrary high-frequency calls.
     */
    public void processAll(Player player) {
        if (!canProcess(player)) {
            return;
        }

        PlayerData data = playerDataManager.get(player);

        updatePing(player, data);

        processChecks(
                player,
                data,
                null
        );
    }

    private void processChecks(
            Player player,
            PlayerData data,
            Check.CheckCategory category
    ) {
        if (!plugin.getConfigManager().isEnabled()) {
            return;
        }

        if (shouldProtectFromPerformance()) {
            return;
        }

        if (shouldProtectFromPing(data)) {
            return;
        }

        if (category == null) {
            checkManager.handleAll(
                    player,
                    data
            );
            return;
        }

        int processed = 0;

        int maximum = plugin.getConfigManager()
                .getMaximumChecksPerCycle();

        for (Check check : checkManager.getChecks(category)) {

            if (processed >= maximum) {
                break;
            }

            if (!check.isEnabled()) {
                continue;
            }

            checkManager.handle(
                    check,
                    player,
                    data
            );

            processed++;
        }
    }

    private boolean canProcess(Player player) {
        if (player == null) {
            return false;
        }

        if (!player.isOnline()) {
            return false;
        }

        if (!plugin.getConfigManager().isEnabled()) {
            return false;
        }

        PlayerData data =
                playerDataManager.find(
                        player.getUniqueId()
                );

        if (data == null) {
            return true;
        }

        if (data.isExempt()
                || data.isTemporarilyExempt()) {
            return false;
        }

        if (plugin.getConfigManager().isSpectatorIgnored()
                && player.getGameMode().name().equals("SPECTATOR")) {
            return false;
        }

        if (plugin.getConfigManager().isPermissionFlightIgnored()
                && player.hasPermission("aegisac.bypass")) {
            return false;
        }

        return true;
    }

    private boolean shouldProtectFromPerformance() {
        if (!plugin.getConfigManager()
                .isPerformanceProtectionEnabled()) {
            return false;
        }

        double minimumTps =
                plugin.getConfigManager().getMinimumTps();

        double tps = getServerTps();

        return tps < minimumTps;
    }

    private boolean shouldProtectFromPing(
            PlayerData data
    ) {
        if (!plugin.getConfigManager()
                .isPingProtectionEnabled()) {
            return false;
        }

        int maximumPing =
                plugin.getConfigManager().getMaximumPing();

        return data.getPing() > maximumPing;
    }

    private void updatePing(
            Player player,
            PlayerData data
    ) {
        int ping = player.getPing();

        data.setPing(ping);
    }

    private double getServerTps() {
        double[] tps = plugin.getServer().getTPS();

        if (tps.length == 0) {
            return 20.0;
        }

        double current = tps[0];

        if (!Double.isFinite(current)) {
            return 20.0;
        }

        return Math.max(
                0.0,
                Math.min(20.0, current)
        );
    }

    public AegisAC getPlugin() {
        return plugin;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
          }
