package id.kingbrezz.aegisac.manager;

import id.kingbrezz.aegisac.AegisAC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public final class MessageManager {

    private static final String FILE_NAME = "messages.yml";

    private final AegisAC plugin;
    private File file;
    private FileConfiguration messages;

    public MessageManager(AegisAC plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()
                && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning(
                    "Unable to create plugin data folder."
            );
        }

        file = new File(plugin.getDataFolder(), FILE_NAME);

        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }

        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
    }

    public String get(String path) {
        String value = messages.getString(path);

        if (value == null) {
            return colorize("&cMessage not found: " + path);
        }

        return colorize(value);
    }

    public String get(String path, Map<String, ?> placeholders) {
        String message = messages.getString(path);

        if (message == null) {
            return colorize("&cMessage not found: " + path);
        }

        return colorize(replacePlaceholders(message, placeholders));
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(getWithPrefix(path));
    }

    public void send(
            CommandSender sender,
            String path,
            Map<String, ?> placeholders
    ) {
        sender.sendMessage(getWithPrefix(path, placeholders));
    }

    public String getWithPrefix(String path) {
        String prefix = messages.getString("prefix", "");
        String message = messages.getString(path);

        if (message == null) {
            return colorize(
                    prefix + "&cMessage not found: " + path
            );
        }

        return colorize(prefix + message);
    }

    public String getWithPrefix(
            String path,
            Map<String, ?> placeholders
    ) {
        String prefix = messages.getString("prefix", "");
        String message = messages.getString(path);

        if (message == null) {
            return colorize(
                    prefix + "&cMessage not found: " + path
            );
        }

        String combined = prefix + message;

        return colorize(
                replacePlaceholders(combined, placeholders)
        );
    }

    public FileConfiguration getConfiguration() {
        return messages;
    }

    public File getFile() {
        return file;
    }

    private String replacePlaceholders(
            String message,
            Map<String, ?> placeholders
    ) {
        if (placeholders == null || placeholders.isEmpty()) {
            return message;
        }

        String result = message;

        for (Map.Entry<String, ?> entry : placeholders.entrySet()) {
            String key = entry.getKey();

            if (key == null || key.isBlank()) {
                continue;
            }

            String value = String.valueOf(entry.getValue());

            result = result.replace(
                    "{" + key + "}",
                    value
            );
        }

        return result;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes(
                '&',
                message
        );
    }
          }
