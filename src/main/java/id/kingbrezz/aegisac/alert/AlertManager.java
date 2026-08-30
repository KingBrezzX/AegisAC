package id.kingbrezz.aegisac.alert;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.manager.MessageManager;
import id.kingbrezz.aegisac.violation.ViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class AlertManager {

    private final AegisAC plugin;
    private final MessageManager messageManager;

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

    public void handle(ViolationEvent event) {
        if (event == null) {
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

    private void sendStaffAlert(
            ViolationEvent event
    ) {
        if (!plugin.getConfigManager()
                .areStaffAlertsEnabled()) {
            return;
        }

        String message = buildMessage(event);

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (!staff.hasPermission(
                    "aegisac.alerts"
            )) {
                continue;
            }

            staff.sendMessage(message);
        }
    }

    private void sendConsoleAlert(
            ViolationEvent event
    ) {
        if (!plugin.getConfigManager()
                .isConsoleAlertsEnabled()) {
            return;
        }

        Bukkit.getConsoleSender().sendMessage(
                buildMessage(event)
        );
    }

    private String buildMessage(
            ViolationEvent event
    ) {
        String message =
                messageManager.get(
                        "alerts.violation"
                );

        return message
                .replace(
                        "{player}",
                        event.getPlayerName()
                )
                .replace(
                        "{check}",
                        event.getCheckName()
                )
                .replace(
                        "{vl}",
                        format(event.getViolationLevel())
                )
                .replace(
                        "{confidence}",
                        format(
                                event.getConfidence() * 100.0
                        )
                )
                .replace(
                        "{ping}",
                        String.valueOf(event.getPing())
                )
                .replace(
                        "{action}",
                        event.getAction()
                )
                .replace(
                        "{detail}",
                        event.getDetail()
                );
    }

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

        return System.currentTimeMillis() - last
                < cooldown;
    }

    private void markAlert(
            ViolationEvent event
    ) {
        lastAlerts.put(
                createKey(event),
                System.currentTimeMillis()
        );
    }

    private String createKey(
            ViolationEvent event
    ) {
        return event.getPlayer()
                .getUniqueId()
                .toString()
                + ':'
                + event.getCheckName();
    }

    private String format(double value) {
        return String.format(
                java.util.Locale.US,
                "%.2f",
                value
        );
    }

    public void clear(Player player) {
        if (player == null) {
            return;
        }

        String prefix = player.getUniqueId()
                .toString()
                + ':';

        lastAlerts.keySet().removeIf(
                key -> key.startsWith(prefix)
        );
    }

    public void clearAll() {
        lastAlerts.clear();
    }

    public int getTrackedAlerts() {
        return lastAlerts.size();
    }

    public AegisAC getPlugin() {
        return plugin;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
          }
