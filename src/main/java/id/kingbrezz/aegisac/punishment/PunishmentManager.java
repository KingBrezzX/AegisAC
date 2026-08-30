package id.kingbrezz.aegisac.punishment;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerData;
import id.kingbrezz.aegisac.violation.ViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class PunishmentManager {

    private final AegisAC plugin;

    public PunishmentManager(AegisAC plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin"
        );
    }

    public boolean handle(ViolationEvent event) {
        if (event == null) {
            return false;
        }

        if (!shouldPunish(event)) {
            return false;
        }

        Player player = event.getPlayer();

        if (!player.isOnline()) {
            return false;
        }

        String command = buildPunishmentCommand(
                player,
                event
        );

        if (command.isBlank()) {
            return false;
        }

        boolean executed = Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                command
        );

        if (executed) {
            resetViolations(event.getData());
        }

        return executed;
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

        Player player = event.getPlayer();

        if (!player.isOnline()) {
            return false;
        }

        if (plugin.getConfigManager()
                .shouldProtectOperators()
                && player.hasPermission(
                "aegisac.bypass"
        )) {
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

    private String buildPunishmentCommand(
            Player player,
            ViolationEvent event
    ) {
        String command = plugin.getConfig()
                .getString(
                        "punishment.command",
                        "ban {player} Suspicious activity detected by AegisAC"
                );

        if (command == null) {
            return "";
        }

        command = command.trim();

        if (command.isEmpty()) {
            return "";
        }

        return command
                .replace(
                        "{player}",
                        player.getName()
                )
                .replace(
                        "{check}",
                        event.getCheckName()
                )
                .replace(
                        "{vl}",
                        format(
                                event.getViolationLevel()
                        )
                )
                .replace(
                        "{confidence}",
                        format(
                                event.getConfidence() * 100.0
                        )
                );
    }

    private void resetViolations(
            PlayerData data
    ) {
        if (data == null) {
            return;
        }

        data.resetViolations();
    }

    private String format(double value) {
        return String.format(
                java.util.Locale.US,
                "%.2f",
                value
        );
    }

    public AegisAC getPlugin() {
        return plugin;
    }
                  }
