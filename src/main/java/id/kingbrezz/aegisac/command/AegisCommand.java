package id.kingbrezz.aegisac.command;

import id.kingbrezz.aegisac.AegisAC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class AegisCommand implements CommandExecutor, TabCompleter {

    private final AegisAC plugin;

    public AegisCommand(AegisAC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("aegisac.admin")) {
            send(sender, "general.no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "alerts" -> handleAlerts(sender);
            case "verbose" -> handleVerbose(sender);
            case "checks" -> handleChecks(sender);
            case "info" -> handleInfo(sender);
            case "help" -> sendHelp(sender);
            default -> send(sender, "general.unknown-command");
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.reloadConfig();
            send(sender, "general.reload-success");
        } catch (Exception exception) {
            plugin.getLogger().warning(
                    "Failed to reload configuration: " + exception.getMessage()
            );

            send(sender, "general.reload-failed");
        }
    }

    private void handleAlerts(CommandSender sender) {
        send(sender, "command.alerts.enabled");
    }

    private void handleVerbose(CommandSender sender) {
        send(sender, "command.verbose.enabled");
    }

    private void handleChecks(CommandSender sender) {
        sender.sendMessage(colorize("&8&m------------&r &bAegisAC Checks &8&m------------"));

        sender.sendMessage(status("Movement", true));
        sender.sendMessage(status("Combat", true));
        sender.sendMessage(status("Player", true));

        sender.sendMessage(colorize("&8&m---------------------------------------------"));
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage(colorize("&8&m----------------&r &bAegisAC &8&m----------------"));
        sender.sendMessage(colorize(
                "&7Version: &f" + plugin.getDescription().getVersion()
        ));
        sender.sendMessage(colorize("&7Author: &fKingBrezz"));
        sender.sendMessage(colorize("&7Platform: &fPaper"));
        sender.sendMessage(colorize("&7Edition: &fJava Edition"));
        sender.sendMessage(colorize("&8&m---------------------------------------------"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(colorize("&8&m--------------------&r &bAegisAC &8&m--------------------"));
        sender.sendMessage(colorize("&b/aegisac reload &8- &7Reload configuration."));
        sender.sendMessage(colorize("&b/aegisac alerts &8- &7Manage violation alerts."));
        sender.sendMessage(colorize("&b/aegisac checks &8- &7Show enabled checks."));
        sender.sendMessage(colorize("&b/aegisac verbose &8- &7Toggle verbose detection."));
        sender.sendMessage(colorize("&b/aegisac info &8- &7Show plugin information."));
        sender.sendMessage(colorize("&8&m------------------------------------------------"));
    }

    private String status(String name, boolean enabled) {
        return colorize(
                (enabled ? "&a✔ " : "&c✘ ")
                        + "&f"
                        + name
                        + " &8- "
                        + (enabled ? "&aEnabled" : "&cDisabled")
        );
    }

    private void send(CommandSender sender, String path) {
        String message = plugin.getConfig().getString(path);

        if (message == null) {
            message = getDefaultMessage(path);
        }

        sender.sendMessage(colorize(message.replace(
                "{prefix}",
                "&8[&bAegisAC&8] &r"
        )));
    }

    private String getDefaultMessage(String path) {
        return switch (path) {
            case "general.no-permission" ->
                    "{prefix}&cYou don't have permission to do that.";

            case "general.unknown-command" ->
                    "{prefix}&cUnknown subcommand. Use &f/aegisac help&c.";

            case "general.reload-success" ->
                    "{prefix}&aConfiguration reloaded successfully.";

            case "general.reload-failed" ->
                    "{prefix}&cFailed to reload configuration.";

            default ->
                    "{prefix}&cMessage not found.";
        };
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (!sender.hasPermission("aegisac.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            suggestions.add("reload");
            suggestions.add("alerts");
            suggestions.add("checks");
            suggestions.add("verbose");
            suggestions.add("info");
            suggestions.add("help");

            String input = args[0].toLowerCase();

            return suggestions.stream()
                    .filter(value -> value.startsWith(input))
                    .toList();
        }

        return List.of();
    }
        }
