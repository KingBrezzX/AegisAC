package id.kingbrezz.aegisac.manager;

import id.kingbrezz.aegisac.AegisAC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ConfigManager {

    private final AegisAC plugin;

    private FileConfiguration config;

    private final Map<String, CheckSettings> checkSettings = new HashMap<>();

    public ConfigManager(AegisAC plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        config = plugin.getConfig();

        checkSettings.clear();
        loadCheckSettings();

        plugin.getLogger().info(
                "Configuration loaded. Registered check configurations: "
                        + checkSettings.size()
        );
    }

    private void loadCheckSettings() {
        ConfigurationSection checks = config.getConfigurationSection("checks");

        if (checks == null) {
            return;
        }

        for (String category : checks.getKeys(false)) {
            ConfigurationSection categorySection =
                    checks.getConfigurationSection(category);

            if (categorySection == null) {
                continue;
            }

            /*
             * Supports both:
             *
             * checks:
             *   speed:
             *
             * and:
             *
             * checks:
             *   movement:
             *     speed:
             */
            if (looksLikeCheck(categorySection)) {
                loadCheck(category, categorySection);
                continue;
            }

            for (String checkName : categorySection.getKeys(false)) {
                ConfigurationSection section =
                        categorySection.getConfigurationSection(checkName);

                if (section == null) {
                    continue;
                }

                if (looksLikeCheck(section)) {
                    loadCheck(checkName, section);
                }
            }
        }
    }

    private boolean looksLikeCheck(ConfigurationSection section) {
        return section.contains("enabled")
                || section.contains("violation")
                || section.contains("vl")
                || section.contains("buffer")
                || section.contains("setback")
                || section.contains("punishment");
    }

    private void loadCheck(
            String name,
            ConfigurationSection section
    ) {
        String key = normalize(name);

        boolean enabled = section.getBoolean("enabled", true);

        double violationAmount = readDouble(
                section,
                "violation.amount",
                section.getDouble("vl", 1.0)
        );

        double buffer = readDouble(
                section,
                "buffer.amount",
                section.getDouble("buffer", 1.0)
        );

        double bufferDecay = readDouble(
                section,
                "buffer.decay",
                0.05
        );

        double maxBuffer = readDouble(
                section,
                "buffer.maximum",
                5.0
        );

        boolean setback = section.getBoolean(
                "setback",
                section.getBoolean("setback.enabled", false)
        );

        boolean punish = section.getBoolean(
                "punishment.enabled",
                section.getBoolean("punish", true)
        );

        int maxVl = section.getInt(
                "violation.maximum",
                section.getInt("max-vl", 20)
        );

        int alertInterval = section.getInt(
                "alert.interval",
                0
        );

        CheckSettings settings = new CheckSettings(
                enabled,
                violationAmount,
                buffer,
                bufferDecay,
                maxBuffer,
                setback,
                punish,
                maxVl,
                alertInterval
        );

        checkSettings.put(key, settings);
    }

    private double readDouble(
            ConfigurationSection section,
            String path,
            double fallback
    ) {
        if (!section.contains(path)) {
            return fallback;
        }

        return section.getDouble(path, fallback);
    }

    private String normalize(String value) {
        return value
                .toLowerCase()
                .replace('-', '_')
                .replace(' ', '_');
    }

    public boolean isCheckEnabled(String checkName) {
        return getCheckSettings(checkName).enabled();
    }

    public double getViolationAmount(String checkName) {
        return getCheckSettings(checkName).violationAmount();
    }

    public double getBufferAmount(String checkName) {
        return getCheckSettings(checkName).bufferAmount();
    }

    public double getBufferDecay(String checkName) {
        return getCheckSettings(checkName).bufferDecay();
    }

    public double getMaximumBuffer(String checkName) {
        return getCheckSettings(checkName).maxBuffer();
    }

    public boolean shouldSetback(String checkName) {
        return getCheckSettings(checkName).setback();
    }

    public boolean shouldPunish(String checkName) {
        return getCheckSettings(checkName).punish();
    }

    public int getMaximumViolation(String checkName) {
        return getCheckSettings(checkName).maxVl();
    }

    public int getAlertInterval(String checkName) {
        return getCheckSettings(checkName).alertInterval();
    }

    public CheckSettings getCheckSettings(String checkName) {
        String key = normalize(checkName);

        CheckSettings settings = checkSettings.get(key);

        if (settings != null) {
            return settings;
        }

        /*
         * Safe defaults.
         *
         * Unknown checks remain enabled so adding a new check does not
         * silently disable it. Individual checks can still opt out via
         * config.yml.
         */
        return CheckSettings.DEFAULT;
    }

    public Map<String, CheckSettings> getAllCheckSettings() {
        return Collections.unmodifiableMap(checkSettings);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reload() {
        load();
    }

    public static final class CheckSettings {

        public static final CheckSettings DEFAULT = new CheckSettings(
                true,
                1.0,
                1.0,
                0.05,
                5.0,
                false,
                true,
                20,
                0
        );

        private final boolean enabled;
        private final double violationAmount;
        private final double bufferAmount;
        private final double bufferDecay;
        private final double maxBuffer;
        private final boolean setback;
        private final boolean punish;
        private final int maxVl;
        private final int alertInterval;

        public CheckSettings(
                boolean enabled,
                double violationAmount,
                double bufferAmount,
                double bufferDecay,
                double maxBuffer,
                boolean setback,
                boolean punish,
                int maxVl,
                int alertInterval
        ) {
            this.enabled = enabled;
            this.violationAmount = violationAmount;
            this.bufferAmount = bufferAmount;
            this.bufferDecay = bufferDecay;
            this.maxBuffer = maxBuffer;
            this.setback = setback;
            this.punish = punish;
            this.maxVl = maxVl;
            this.alertInterval = alertInterval;
        }

        public boolean enabled() {
            return enabled;
        }

        public double violationAmount() {
            return violationAmount;
        }

        public double bufferAmount() {
            return bufferAmount;
        }

        public double bufferDecay() {
            return bufferDecay;
        }

        public double maxBuffer() {
            return maxBuffer;
        }

        public boolean setback() {
            return setback;
        }

        public boolean punish() {
            return punish;
        }

        public int maxVl() {
            return maxVl;
        }

        public int alertInterval() {
            return alertInterval;
        }
    }
            }
