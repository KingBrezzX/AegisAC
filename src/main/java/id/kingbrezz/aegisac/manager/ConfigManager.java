package id.kingbrezz.aegisac.manager;

import id.kingbrezz.aegisac.AegisAC;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public final class ConfigManager {

    private final AegisAC plugin;

    public ConfigManager(AegisAC plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void load() {
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public boolean isEnabled() {
        return getConfig().getBoolean("settings.enabled", true);
    }

    public boolean isDebug() {
        return getConfig().getBoolean("settings.debug", false);
    }

    public boolean isVerbose() {
        return getConfig().getBoolean("settings.verbose", false);
    }

    public String getBypassPermission() {
        return getConfig().getString(
                "settings.bypass-permission",
                "aegisac.bypass"
        );
    }

    public int getDefaultPunishmentVl() {
        return Math.max(
                1,
                getConfig().getInt(
                        "detection.default-punishment-vl",
                        20
                )
        );
    }

    public int getMaxViolationLevel() {
        return Math.max(
                1,
                getConfig().getInt(
                        "detection.max-violation-level",
                        100
                )
        );
    }

    public double getViolationDecay() {
        return Math.max(
                0.0,
                getConfig().getDouble(
                        "detection.violation-decay",
                        1.0
                )
        );
    }

    public long getViolationDecayIntervalMillis() {
        long seconds = Math.max(
                1,
                getConfig().getLong(
                        "detection.violation-decay-interval",
                        5
                )
        );

        return seconds * 1000L;
    }

    public double getMinimumConfidence() {
        return clamp(
                getConfig().getDouble(
                        "detection.minimum-confidence",
                        0.70
                ),
                0.0,
                1.0
        );
    }

    public int getJoinGracePeriodSeconds() {
        return Math.max(
                0,
                getConfig().getInt(
                        "detection.join-grace-period",
                        3
                )
        );
    }

    public boolean isPerformanceProtectionEnabled() {
        return getConfig().getBoolean(
                "detection.performance-protection.enabled",
                true
        );
    }

    public double getMinimumTps() {
        return Math.max(
                1.0,
                getConfig().getDouble(
                        "detection.performance-protection.minimum-tps",
                        18.0
                )
        );
    }

    public boolean isPingProtectionEnabled() {
        return getConfig().getBoolean(
                "detection.ping-protection.enabled",
                true
        );
    }

    public int getMaximumPing() {
        return Math.max(
                0,
                getConfig().getInt(
                        "detection.ping-protection.maximum-ping",
                        400
                )
        );
    }

    public boolean isSetbackEnabled() {
        return getConfig().getBoolean(
                "setback.enabled",
                true
        );
    }

    public int getMaximumSetbacksPerInterval() {
        return Math.max(
                1,
                getConfig().getInt(
                        "setback.max-per-interval",
                        3
                )
        );
    }

    public long getSetbackIntervalMillis() {
        long seconds = Math.max(
                1,
                getConfig().getLong(
                        "setback.interval-seconds",
                        10
                )
        );

        return seconds * 1000L;
    }

    public boolean shouldRestoreSafePosition() {
        return getConfig().getBoolean(
                "setback.restore-safe-position",
                true
        );
    }

    public boolean isPunishmentEnabled() {
        return getConfig().getBoolean(
                "punishment.enabled",
                true
        );
    }

    public int getPunishmentThreshold() {
        return Math.max(
                1,
                getConfig().getInt(
                        "punishment.threshold",
                        getDefaultPunishmentVl()
                )
        );
    }

    public boolean shouldProtectOperators() {
        return getConfig().getBoolean(
                "punishment.protect-operators",
                true
        );
    }

    public boolean isPunishmentConfidenceRequired() {
        return getConfig().getBoolean(
                "punishment.require-confidence",
                true
        );
    }

    public double getPunishmentMinimumConfidence() {
        return clamp(
                getConfig().getDouble(
                        "punishment.minimum-confidence",
                        0.95
                ),
                0.0,
                1.0
        );
    }

    public boolean areAlertsEnabled() {
        return getConfig().getBoolean(
                "alerts.enabled",
                true
        );
    }

    public boolean areStaffAlertsEnabled() {
        return getConfig().getBoolean(
                "alerts.staff",
                true
        );
    }

    public boolean isConsoleAlertsEnabled() {
        return getConfig().getBoolean(
                "alerts.console",
                true
        );
    }

    public boolean isAlertAntiSpamEnabled() {
        return getConfig().getBoolean(
                "alerts.anti-spam.enabled",
                true
        );
    }

    public long getAlertCooldownMillis() {
        return Math.max(
                0L,
                getConfig().getLong(
                        "alerts.anti-spam.cooldown-milliseconds",
                        1500L
                )
        );
    }

    public boolean isDiscordEnabled() {
        return getConfig().getBoolean(
                "discord.enabled",
                false
        );
    }

    public String getDiscordWebhookUrl() {
        return getConfig().getString(
                "discord.webhook-url",
                ""
        ).trim();
    }

    public int getDiscordMinimumViolationLevel() {
        return Math.max(
                1,
                getConfig().getInt(
                        "discord.minimum-violation-level",
                        10
                )
        );
    }

    public boolean isDiscordAntiSpamEnabled() {
        return getConfig().getBoolean(
                "discord.anti-spam.enabled",
                true
        );
    }

    public long getDiscordCooldownMillis() {
        long seconds = Math.max(
                0L,
                getConfig().getLong(
                        "discord.anti-spam.cooldown-seconds",
                        10L
                )
        );

        return seconds * 1000L;
    }

    public String getDiscordUsername() {
        return getConfig().getString(
                "discord.username",
                "AegisAC"
        );
    }

    public boolean isDiscordEmbedEnabled() {
        return getConfig().getBoolean(
                "discord.embed.enabled",
                true
        );
    }

    public boolean isHistoryEnabled() {
        return getConfig().getBoolean(
                "logging.history",
                true
        );
    }

    public int getMaximumHistoryPerPlayer() {
        return Math.max(
                1,
                getConfig().getInt(
                        "logging.max-history-per-player",
                        50
                )
        );
    }

    public boolean isFileLoggingEnabled() {
        return getConfig().getBoolean(
                "logging.file",
                true
        );
    }

    public boolean isAsyncProcessingEnabled() {
        return getConfig().getBoolean(
                "performance.async-processing",
                true
        );
    }

    public int getMaximumChecksPerCycle() {
        return Math.max(
                1,
                getConfig().getInt(
                        "performance.max-checks-per-cycle",
                        32
                )
        );
    }

    public boolean isSkipEmptyServerEnabled() {
        return getConfig().getBoolean(
                "performance.skip-empty-server",
                true
        );
    }

    public boolean isAdaptiveProcessingEnabled() {
        return getConfig().getBoolean(
                "performance.adaptive-processing.enabled",
                true
        );
    }

    public double getAdaptiveMinimumTps() {
        return Math.max(
                1.0,
                getConfig().getDouble(
                        "performance.adaptive-processing.minimum-tps",
                        17.0
                )
        );
    }

    public boolean isCheckEnabled(String path) {
        return getConfig().getBoolean(
                "checks." + path + ".enabled",
                false
        );
    }

    public int getCheckViolation(String path, int fallback) {
        return Math.max(
                1,
                getConfig().getInt(
                        "checks." + path + ".vl",
                        fallback
                )
        );
    }

    public double getCheckConfidence(String path, double fallback) {
        return clamp(
                getConfig().getDouble(
                        "checks." + path + ".confidence",
                        fallback
                ),
                0.0,
                1.0
        );
    }

    public boolean shouldCheckSetback(String path) {
        return getConfig().getBoolean(
                "checks." + path + ".setback",
                false
        );
    }

    public boolean isSpectatorIgnored() {
        return getConfig().getBoolean(
                "compatibility.ignore.spectator",
                true
        );
    }

    public boolean isVanishedIgnored() {
        return getConfig().getBoolean(
                "compatibility.ignore.vanished",
                true
        );
    }

    public boolean isPermissionFlightIgnored() {
        return getConfig().getBoolean(
                "compatibility.ignore.flying-with-permission",
                true
        );
    }

    public boolean isTemporaryExemptionEnabled() {
        return getConfig().getBoolean(
                "compatibility.temporary-exemption.enabled",
                true
        );
    }

    public long getTemporaryExemptionDurationMillis() {
        long seconds = Math.max(
                0L,
                getConfig().getLong(
                        "compatibility.temporary-exemption.duration-seconds",
                        5L
                )
        );

        return seconds * 1000L;
    }

    private double clamp(
            double value,
            double minimum,
            double maximum
    ) {
        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }
          }
