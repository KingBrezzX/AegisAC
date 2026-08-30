package id.kingbrezz.aegisac.violation;

import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Immutable event describing a detected violation.
 */
public final class ViolationEvent {

    private final Player player;
    private final String playerName;
    private final String checkName;

    private final double violationLevel;
    private final double confidence;

    private final int ping;

    private final String action;
    private final String detail;

    public ViolationEvent(
            Player player,
            String checkName,
            double violationLevel,
            double confidence,
            int ping,
            String action,
            String detail
    ) {
        this.player = Objects.requireNonNull(
                player,
                "player"
        );

        this.playerName = player.getName();

        this.checkName = normalize(
                checkName,
                "unknown"
        );

        this.violationLevel =
                sanitize(violationLevel);

        this.confidence =
                clamp(
                        sanitize(confidence),
                        0.0D,
                        1.0D
                );

        this.ping = Math.max(
                0,
                ping
        );

        this.action = normalize(
                action,
                "none"
        );

        this.detail = normalize(
                detail,
                ""
        );
    }

    /**
     * Convenience constructor for checks that do not
     * need to provide ping/action information.
     */
    public ViolationEvent(
            Player player,
            String checkName,
            double violationLevel,
            double confidence,
            String detail
    ) {
        this(
                player,
                checkName,
                violationLevel,
                confidence,
                getPing(player),
                "none",
                detail
        );
    }

    public Player getPlayer() {
        return player;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getCheckName() {
        return checkName;
    }

    public double getViolationLevel() {
        return violationLevel;
    }

    public double getConfidence() {
        return confidence;
    }

    public int getPing() {
        return ping;
    }

    public String getAction() {
        return action;
    }

    public String getDetail() {
        return detail;
    }

    private static int getPing(Player player) {
        if (player == null) {
            return 0;
        }

        /*
         * Paper/Bukkit Player#getPing() is available on modern
         * server APIs targeted by AegisAC.
         */
        try {
            return Math.max(
                    0,
                    player.getPing()
            );
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private static double sanitize(double value) {
        return Double.isFinite(value)
                ? value
                : 0.0D;
    }

    private static double clamp(
            double value,
            double min,
            double max
    ) {
        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }

    private static String normalize(
            String value,
            String fallback
    ) {
        if (value == null
                || value.isBlank()) {
            return fallback;
        }

        return value;
    }
            }
