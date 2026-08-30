package id.kingbrezz.aegisac.violation;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class ViolationManager {

    private final AegisAC plugin;

    public ViolationManager(AegisAC plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public double fail(
            Player player,
            PlayerData data,
            String checkName,
            double amount
    ) {
        if (player == null || data == null) {
            return 0.0;
        }

        if (checkName == null || checkName.isBlank()) {
            return 0.0;
        }

        if (!Double.isFinite(amount) || amount <= 0.0) {
            return data.getViolation(checkName);
        }

        if (!plugin.getConfigManager().isCheckEnabled(checkName)) {
            return data.getViolation(checkName);
        }

        double violation = data.addViolation(
                checkName,
                amount
        );

        int maximum = plugin.getConfigManager()
                .getMaxViolationLevel();

        if (violation > maximum) {
            violation = maximum;
            data.setViolation(
                    checkName,
                    maximum
            );
        }

        data.markCheck(checkName);

        return violation;
    }

    public double reward(
            PlayerData data,
            String checkName,
            double amount
    ) {
        if (data == null
                || checkName == null
                || checkName.isBlank()) {
            return 0.0;
        }

        if (!Double.isFinite(amount) || amount <= 0.0) {
            return data.getViolation(checkName);
        }

        return data.reduceViolation(
                checkName,
                amount
        );
    }

    public double getViolation(
            PlayerData data,
            String checkName
    ) {
        if (data == null
                || checkName == null
                || checkName.isBlank()) {
            return 0.0;
        }

        return data.getViolation(checkName);
    }

    public boolean hasReachedThreshold(
            PlayerData data,
            String checkName
    ) {
        if (data == null
                || checkName == null
                || checkName.isBlank()) {
            return false;
        }

        double violation = data.getViolation(checkName);

        int threshold = plugin.getConfigManager()
                .getPunishmentThreshold();

        return violation >= threshold;
    }

    public boolean hasReachedCheckThreshold(
            PlayerData data,
            String checkName
    ) {
        if (data == null
                || checkName == null
                || checkName.isBlank()) {
            return false;
        }

        double violation = data.getViolation(checkName);

        int threshold = plugin.getConfigManager()
                .getCheckViolation(checkName, 1);

        return violation >= threshold;
    }

    public void decay(PlayerData data) {
        if (data == null) {
            return;
        }

        double amount = plugin.getConfigManager()
                .getViolationDecay();

        if (amount <= 0.0) {
            return;
        }

        data.getViolations().keySet().forEach(
                check -> data.reduceViolation(
                        check,
                        amount
                )
        );
    }

    public void reset(
            PlayerData data,
            String checkName
    ) {
        if (data == null
                || checkName == null
                || checkName.isBlank()) {
            return;
        }

        data.resetViolation(checkName);
    }

    public void resetAll(PlayerData data) {
        if (data == null) {
            return;
        }

        data.resetViolations();
    }

    public AegisAC getPlugin() {
        return plugin;
    }
        }
