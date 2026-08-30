package id.kingbrezz.aegisac.violation;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.check.CheckResult;
import id.kingbrezz.aegisac.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class ViolationProcessor {

    private final AegisAC plugin;
    private final ViolationManager violationManager;

    public ViolationProcessor(
            AegisAC plugin,
            ViolationManager violationManager
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin"
        );

        this.violationManager = Objects.requireNonNull(
                violationManager,
                "violationManager"
        );
    }

    /**
     * Processes the result of a detection.
     *
     * @return the generated violation event,
     *         or null when the result should be ignored
     */
    public ViolationEvent process(
            Player player,
            PlayerData data,
            CheckResult result
    ) {
        if (player == null || data == null || result == null) {
            return null;
        }

        if (!result.isFailed()) {
            return null;
        }

        if (!player.isOnline()) {
            return null;
        }

        if (!plugin.getConfigManager().isEnabled()) {
            return null;
        }

        if (data.isExempt() || data.isTemporarilyExempt()) {
            return null;
        }

        double confidence = result.getConfidence();

        double minimumConfidence =
                plugin.getConfigManager()
                        .getMinimumConfidence();

        if (confidence < minimumConfidence) {
            return null;
        }

        double amount = result.getViolationAmount();

        if (!Double.isFinite(amount) || amount <= 0.0) {
            return null;
        }

        double violationLevel =
                violationManager.fail(
                        player,
                        data,
                        result.getCheckName(),
                        amount
                );

        ViolationEvent event =
                new ViolationEvent(
                        player,
                        data,
                        result,
                        violationLevel
                );

        dispatch(event);

        return event;
    }

    private void dispatch(ViolationEvent event) {
        if (event == null) {
            return;
        }

        /*
         * Alert, setback and punishment managers are connected
         * here once their implementations are available.
         *
         * Keeping the dispatch point centralized prevents
         * individual checks from directly performing actions.
         */

        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info(
                    "Violation: "
                            + event.getPlayerName()
                            + " failed "
                            + event.getCheckName()
                            + " VL="
                            + format(event.getViolationLevel())
                            + " confidence="
                            + format(
                            event.getConfidence() * 100.0
                    )
                            + "%"
            );
        }
    }

    private String format(double value) {
        return String.format(
                java.util.Locale.US,
                "%.2f",
                value
        );
    }

    public boolean shouldPunish(
            ViolationEvent event
    ) {
        if (event == null) {
            return false;
        }

        if (!plugin.getConfigManager()
                .isPunishmentEnabled()) {
            return false;
        }

        if (plugin.getConfigManager()
                .shouldProtectOperators()
                && event.getPlayer()
                .hasPermission("aegisac.bypass")) {
            return false;
        }

        int threshold =
                plugin.getConfigManager()
                        .getPunishmentThreshold();

        if (event.getViolationLevel() < threshold) {
            return false;
        }

        if (plugin.getConfigManager()
                .isPunishmentConfidenceRequired()) {

            double minimum =
                    plugin.getConfigManager()
                            .getPunishmentMinimumConfidence();

            if (event.getConfidence() < minimum) {
                return false;
            }
        }

        return true;
    }

    public boolean shouldSetback(
            ViolationEvent event
    ) {
        if (event == null || !event.shouldSetback()) {
            return false;
        }

        return plugin.getConfigManager()
                .isSetbackEnabled();
    }

    public ViolationManager getViolationManager() {
        return violationManager;
    }

    public AegisAC getPlugin() {
        return plugin;
    }
          }
