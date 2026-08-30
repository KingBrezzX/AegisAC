package id.kingbrezz.aegisac.violation;

import id.kingbrezz.aegisac.check.CheckResult;
import id.kingbrezz.aegisac.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class ViolationEvent {

    private final Player player;
    private final PlayerData data;
    private final String checkName;
    private final double violationLevel;
    private final double violationAmount;
    private final double confidence;
    private final boolean setback;
    private final String action;
    private final String detail;
    private final long timestamp;

    public ViolationEvent(
            Player player,
            PlayerData data,
            CheckResult result,
            double violationLevel
    ) {
        this.player = Objects.requireNonNull(
                player,
                "player"
        );

        this.data = Objects.requireNonNull(
                data,
                "data"
        );

        Objects.requireNonNull(
                result,
                "result"
        );

        if (!result.isFailed()) {
            throw new IllegalArgumentException(
                    "ViolationEvent requires a failed CheckResult."
            );
        }

        if (!Double.isFinite(violationLevel)
                || violationLevel < 0.0) {
            throw new IllegalArgumentException(
                    "violationLevel must be finite and non-negative."
            );
        }

        this.checkName = result.getCheckName();
        this.violationLevel = violationLevel;
        this.violationAmount =
                result.getViolationAmount();
        this.confidence = result.getConfidence();
        this.setback = result.shouldSetback();
        this.action = result.getAction();
        this.detail = result.getDetail();
        this.timestamp = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerData getData() {
        return data;
    }

    public String getCheckName() {
        return checkName;
    }

    public double getViolationLevel() {
        return violationLevel;
    }

    public double getViolationAmount() {
        return violationAmount;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean shouldSetback() {
        return setback;
    }

    public String getAction() {
        return action;
    }

    public String getDetail() {
        return detail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPlayerName() {
        return player.getName();
    }

    public int getPing() {
        return data.getPing();
    }

    public boolean isHighConfidence(double threshold) {
        if (!Double.isFinite(threshold)) {
            return false;
        }

        return confidence >= Math.max(
                0.0,
                Math.min(1.0, threshold)
        );
    }

    @Override
    public String toString() {
        return "ViolationEvent{" +
                "player=" + player.getName() +
                ", checkName='" + checkName + '\'' +
                ", violationLevel=" + violationLevel +
                ", violationAmount=" + violationAmount +
                ", confidence=" + confidence +
                ", setback=" + setback +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
    }
