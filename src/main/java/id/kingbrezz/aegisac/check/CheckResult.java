package id.kingbrezz.aegisac.check;

import java.util.Objects;

public final class CheckResult {

    private final String checkName;
    private final boolean failed;
    private final double confidence;
    private final double violationAmount;
    private final boolean setback;
    private final String action;
    private final String detail;

    private CheckResult(
            String checkName,
            boolean failed,
            double confidence,
            double violationAmount,
            boolean setback,
            String action,
            String detail
    ) {
        this.checkName = Objects.requireNonNull(
                checkName,
                "checkName"
        );

        if (checkName.isBlank()) {
            throw new IllegalArgumentException(
                    "checkName cannot be blank"
            );
        }

        this.failed = failed;
        this.confidence = clamp(confidence, 0.0, 1.0);
        this.violationAmount = Math.max(
                0.0,
                violationAmount
        );
        this.setback = setback;
        this.action = action == null ? "" : action;
        this.detail = detail == null ? "" : detail;
    }

    public static CheckResult pass(String checkName) {
        return new CheckResult(
                checkName,
                false,
                0.0,
                0.0,
                false,
                "",
                ""
        );
    }

    public static CheckResult fail(
            String checkName,
            double confidence,
            double violationAmount
    ) {
        return new CheckResult(
                checkName,
                true,
                confidence,
                violationAmount,
                false,
                "alert",
                ""
        );
    }

    public static CheckResult fail(
            String checkName,
            double confidence,
            double violationAmount,
            boolean setback
    ) {
        return new CheckResult(
                checkName,
                true,
                confidence,
                violationAmount,
                setback,
                setback ? "setback" : "alert",
                ""
        );
    }

    public CheckResult withAction(String action) {
        return new CheckResult(
                checkName,
                failed,
                confidence,
                violationAmount,
                setback,
                action,
                detail
        );
    }

    public CheckResult withDetail(String detail) {
        return new CheckResult(
                checkName,
                failed,
                confidence,
                violationAmount,
                setback,
                action,
                detail
        );
    }

    public CheckResult withSetback(boolean setback) {
        return new CheckResult(
                checkName,
                failed,
                confidence,
                violationAmount,
                setback,
                setback ? "setback" : action,
                detail
        );
    }

    public String getCheckName() {
        return checkName;
    }

    public boolean isFailed() {
        return failed;
    }

    public double getConfidence() {
        return confidence;
    }

    public double getViolationAmount() {
        return violationAmount;
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

    public boolean isHighConfidence(double threshold) {
        if (!Double.isFinite(threshold)) {
            return false;
        }

        return confidence >= clamp(
                threshold,
                0.0,
                1.0
        );
    }

    private static double clamp(
            double value,
            double minimum,
            double maximum
    ) {
        if (!Double.isFinite(value)) {
            return minimum;
        }

        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }

    @Override
    public String toString() {
        return "CheckResult{" +
                "checkName='" + checkName + '\'' +
                ", failed=" + failed +
                ", confidence=" + confidence +
                ", violationAmount=" + violationAmount +
                ", setback=" + setback +
                ", action='" + action + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
      }
