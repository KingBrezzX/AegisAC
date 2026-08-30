package id.kingbrezz.aegisac.alert;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.manager.MessageManager;
import id.kingbrezz.aegisac.violation.ViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AlertManager {

    private final AegisAC plugin;
    private final MessageManager messageManager;

    /*
     * player UUID + check name -> last alert timestamp
     */
    private final Map<String, Long> lastAlerts =
            new ConcurrentHashMap<>();

    public AlertManager(
            AegisAC plugin,
            MessageManager messageManager
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin"
        );

        this.messageManager = Objects.requireNonNull(
                messageManager,
                "messageManager"
        );
    }

    /**
     * Handles a violation event.
     */
    public void handle(ViolationEvent event) {
        if (event == null) {
            return;
        }

        if (!plugin.isEnabled()) {
            return;
        }

        if (plugin.getConfigManager() == null) {
            return;
        }

        if (!plugin.getConfigManager()
                .areAlertsEnabled()) {
            return;
        }

        if (isDuplicate(event)) {
            return;
        }

        markAlert(event);

        sendStaffAlert(event);
        sendConsoleAlert(event);
    }

    /**
     * Sends the alert to online staff.
     */
    private void sendStaffAlert(
            ViolationEvent event
    ) {
        if (!plugin.getConfigManager()
                .areStaffAlertsEnabled()) {
            return;
        }

        String message = buildMessage(event);

        if (message.isEmpty()) {
            return;
        }

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (!staff.isOnline()) {
                continue;
            }

            if (!staff.hasPermission(
                    "aegisac.alerts"
            )) {
                continue;
            }

            staff.sendMessage(message);
        }
    }

    /**
     * Sends the alert to console.
     */
    private void sendConsoleAlert(
            ViolationEvent event
    ) {
        if (!plugin.getConfigManager()
                .isConsoleAlertsEnabled()) {
            return;
        }

        String message = buildMessage(event);

        if (message.isEmpty()) {
            return;
        }

        Bukkit.getConsoleSender().sendMessage(
                message
        );
    }

    /**
     * Builds the configured violation message.
     */
    private String buildMessage(
            ViolationEvent event
    ) {
        String message =
                messageManager.get(
                        "alerts.violation"
                );

        if (message == null
                || message.isEmpty()) {
            message =
                    "&c[AegisAC] &f{player} &7failed "
                            + "&f{check} &7VL: &f{vl}";
        }

        return message
                .replace(
                        "{player}",
                        safe(event.getPlayerName())
                )
                .replace(
                        "{check}",
                        safe(event.getCheckName())
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
                                event.getConfidence()
                                        * 100.0D
                        )
                )
                .replace(
                        "{ping}",
                        String.valueOf(
                                event.getPing()
                        )
                )
                .replace(
                        "{action}",
                        safe(event.getAction())
                )
                .replace(
                        "{detail}",
                        safe(event.getDetail())
                );
    }

    /**
     * Checks the configured alert cooldown.
     */
    private boolean isDuplicate(
            ViolationEvent event
    ) {
        if (!plugin.getConfigManager()
                .isAlertAntiSpamEnabled()) {
            return false;
        }

        String key = createKey(event);

        Long last = lastAlerts.get(key);

        if (last == null) {
            return false;
        }

        long cooldown =
                plugin.getConfigManager()
                        .getAlertCooldownMillis();

        if (cooldown <= 0L) {
            return false;
        }

        long elapsed =
                System.currentTimeMillis() - last;

        return elapsed >= 0L
                && elapsed < cooldown;
    }

    /**
     * Records the timestamp of the last alert.
     */
    private void markAlert(
            ViolationEvent event
    ) {
        lastAlerts.put(
                createKey(event),
                System.currentTimeMillis()
        );
    }

    /**
     * Creates a stable anti-spam key.
     */
    private String createKey(
            ViolationEvent event
    ) {
        if (event.getPlayer() == null) {
            return "unknown:"
                    + safe(event.getCheckName());
        }

        UUID uuid =
                event.getPlayer()
                        .getUniqueId();

        return uuid
                + ":"
                + safe(event.getCheckName())
                        .toLowerCase(Locale.ROOT);
    }

    /**
     * Clears alert cooldowns for one player.
     */
    public void clear(Player player) {
        if (player == null) {
            return;
        }

        String prefix =
                player.getUniqueId()
                        .toString()
                        + ":";

        Iterator<String> iterator =
                lastAlerts.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();

            if (key.startsWith(prefix)) {
                iterator.remove();
            }
        }
    }

    /**
     * Clears all alert cooldowns.
     */
    public void clearAll() {
        lastAlerts.clear();
    }

    /**
     * Number of active anti-spam entries.
     */
    public int getTrackedAlerts() {
        return lastAlerts.size();
    }

    public AegisAC getPlugin() {
        return plugin;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    private String format(double value) {
        if (!Double.isFinite(value)) {
            return "0.00";
        }

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }

    private String safe(String value) {
        return value == null
                ? ""
                : value;
    }
    }
