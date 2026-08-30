package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.Objects;

public abstract class AbstractCheck implements Check {

    private final AegisAC plugin;
    private final String name;
    private final String displayName;
    private final CheckCategory category;

    protected AbstractCheck(
            AegisAC plugin,
            String name,
            String displayName,
            CheckCategory category
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.name = requireText(name, "name");
        this.displayName = requireText(displayName, "displayName");
        this.category = Objects.requireNonNull(category, "category");
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getDisplayName() {
        return displayName;
    }

    @Override
    public final CheckCategory getCategory() {
        return category;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfigManager().isCheckEnabled(name);
    }

    @Override
    public final AegisAC getPlugin() {
        return plugin;
    }

    /**
     * Returns the configured violation amount for this check.
     */
    protected final int getViolationAmount(int fallback) {
        return plugin.getConfigManager().getCheckViolation(
                name,
                fallback
        );
    }

    /**
     * Returns the configured minimum confidence for this check.
     */
    protected final double getConfidence(double fallback) {
        return plugin.getConfigManager().getCheckConfidence(
                name,
                fallback
        );
    }

    /**
     * Returns whether this check should request a setback.
     */
    protected final boolean shouldSetback() {
        return plugin.getConfigManager().shouldCheckSetback(name);
    }

    /**
     * Basic eligibility check shared by individual checks.
     */
    protected boolean canCheck(Player player, PlayerData data) {
        if (player == null || data == null) {
            return false;
        }

        if (!player.isOnline()) {
            return false;
        }

        if (!isEnabled()) {
            return false;
        }

        if (data.isExempt() || data.isTemporarilyExempt()) {
            return false;
        }

        if (data.isWithinJoinGracePeriod(
                plugin.getConfigManager().getJoinGracePeriodSeconds()
        )) {
            return false;
        }

        return true;
    }

    /**
     * Validates that a numeric detection value is usable.
     */
    protected boolean isValidDetectionValue(double value) {
        return Double.isFinite(value);
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " cannot be null or blank"
            );
        }

        return value;
    }
      }
