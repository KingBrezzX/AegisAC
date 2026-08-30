package id.kingbrezz.aegisac.check;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.player.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CheckManager {

    private final AegisAC plugin;

    private final Map<String, Check> checks =
            new LinkedHashMap<>();

    public CheckManager(AegisAC plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * Registers a check.
     *
     * @return true when the check was registered
     */
    public boolean register(Check check) {
        Objects.requireNonNull(check, "check");

        String name = normalize(check.getName());

        if (name == null) {
            throw new IllegalArgumentException(
                    "Check name cannot be null or blank."
            );
        }

        if (checks.containsKey(name)) {
            plugin.getLogger().warning(
                    "Duplicate check registration ignored: "
                            + check.getName()
            );
            return false;
        }

        checks.put(name, check);

        return true;
    }

    /**
     * Removes a registered check.
     */
    public boolean unregister(String name) {
        String normalized = normalize(name);

        if (normalized == null) {
            return false;
        }

        return checks.remove(normalized) != null;
    }

    /**
     * Finds a check by its configuration name.
     */
    public Check get(String name) {
        String normalized = normalize(name);

        if (normalized == null) {
            return null;
        }

        return checks.get(normalized);
    }

    /**
     * Returns all registered checks.
     */
    public Collection<Check> getChecks() {
        return Collections.unmodifiableCollection(
                new ArrayList<>(checks.values())
        );
    }

    /**
     * Returns checks belonging to a category.
     */
    public List<Check> getChecks(Check.CheckCategory category) {
        Objects.requireNonNull(category, "category");

        List<Check> result = new ArrayList<>();

        for (Check check : checks.values()) {
            if (check.getCategory() == category) {
                result.add(check);
            }
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Returns only enabled checks.
     */
    public List<Check> getEnabledChecks() {
        List<Check> result = new ArrayList<>();

        for (Check check : checks.values()) {
            if (check.isEnabled()) {
                result.add(check);
            }
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Executes a single check.
     *
     * Detection exceptions are isolated so one broken check
     * cannot bring down the entire anti-cheat.
     */
    public void handle(
            Check check,
            Player player,
            PlayerData data
    ) {
        if (check == null || player == null || data == null) {
            return;
        }

        if (!check.isEnabled()) {
            return;
        }

        try {
            check.handle(player, data);
        } catch (RuntimeException exception) {
            plugin.getLogger().warning(
                    "Check '" + check.getName()
                            + "' failed for "
                            + player.getName()
                            + ": "
                            + exception.getMessage()
            );

            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning(
                        "Check exception details: "
                                + exception
                );
            }
        }
    }

    /**
     * Executes all enabled checks.
     *
     * The actual event dispatcher will decide when this
     * method should be called. Checks are deliberately not
     * scheduled independently here.
     */
    public void handleAll(
            Player player,
            PlayerData data
    ) {
        if (player == null || data == null) {
            return;
        }

        if (!player.isOnline()) {
            return;
        }

        if (data.isExempt()
                || data.isTemporarilyExempt()) {
            return;
        }

        int maximumChecks = plugin.getConfigManager()
                .getMaximumChecksPerCycle();

        int processed = 0;

        for (Check check : checks.values()) {
            if (processed >= maximumChecks) {
                break;
            }

            if (!check.isEnabled()) {
                continue;
            }

            handle(check, player, data);
            processed++;
        }
    }

    /**
     * Clears every registered check.
     */
    public void clear() {
        checks.clear();
    }

    public int size() {
        return checks.size();
    }

    public boolean isRegistered(String name) {
        String normalized = normalize(name);

        return normalized != null
                && checks.containsKey(normalized);
    }

    public AegisAC getPlugin() {
        return plugin;
    }

    private String normalize(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        return name.trim().toLowerCase();
    }
  }
