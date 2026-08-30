package id.kingbrezz.aegisac.command;

import id.kingbrezz.aegisac.AegisAC;
import id.kingbrezz.aegisac.check.Check;
import id.kingbrezz.aegisac.check.CheckManager;
import id.kingbrezz.aegisac.manager.MessageManager;
import id.kingbrezz.aegisac.player.PlayerData;
import id.kingbrezz.aegisac.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AegisCommand
        implements CommandExecutor, TabCompleter {

    private final AegisAC plugin;
    private final MessageManager messages;
    private final CheckManager checkManager;
    private final PlayerDataManager playerDataManager;

    public AegisCommand(
            AegisAC plugin,
            MessageManager messages,
            CheckManager checkManager,
            PlayerDataManager playerDataManager
    ) {
        this.plugin = plugin;
        this.messages = messages;
        this.checkManager = checkManager;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("aegisac.admin")) {
            send(sender, "errors.no-permission");
            return true;
        }

        if (args.length == 0) {
            send(sender, "command.help");
            return true;
        }

        String subCommand =
                args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "reload" -> reload(sender);
            case "info" -> info(sender);
            case "checks" -> checks(sender);
            case "status" -> status(sender);
            case "alerts" -> alerts(sender);
            case "reset" -> reset(sender, args);
            case "version" -> version(sender);
            case "help" -> send(sender, "command.help");
            default -> send(
                    sender,
                    "command.unknown",
                    Map.of("command", args[0])
            );
        }

        return true;
    }

    private void reload(CommandSender sender) {
        plugin.getConfigManager().load();
        messages.reload();

        send(sender, "command.reload");
    }

    private void info(CommandSender sender) {
        String enabled = plugin.getConfigManager().isEnabled()
                ? "&aEnabled"
                : "&cDisabled";

        sender.sendMessage(
                colorize(
                        "&8&m--------------------------"
                )
        );

        sender.sendMessage(
                colorize(
                        "&bAegisAC &7Information"
                )
        );

        sender.sendMessage(
                colorize(
                        "&7Version: &f"
                                + plugin.getDescription()
                                .getVersion()
                )
        );

        sender.sendMessage(
                colorize(
                        "&7Status: " + enabled
                )
        );

        sender.sendMessage(
                colorize(
                        "&7Registered checks: &f"
                                + checkManager.size()
                )
        );

        sender.sendMessage(
                colorize(
                        "&7Tracked players: &f"
                                + playerDataManager.size()
                )
        );

        sender.sendMessage(
                colorize(
                        "&8&m--------------------------"
                )
        );
    }

    private void checks(CommandSender sender) {
        List<Check> checks =
                new ArrayList<>(
                        checkManager.getChecks()
                );

        if (checks.isEmpty()) {
            send(sender, "command.no-checks");
            return;
        }

        sender.sendMessage(
                colorize("&bAegisAC Checks")
        );

        for (Check check : checks) {
            String state = check.isEnabled()
                    ? "&aON"
                    : "&cOFF";

            sender.sendMessage(
                    colorize(
                            "&8- &f"
                                    + check.getName()
                                    + " &7["
                                    + state
                                    + "&7] &8("
                                    + check.getCategory()
                                    + "&8)"
                    )
            );
        }
    }

    private void status(CommandSender sender) {
        boolean enabled =
                plugin.getConfigManager().isEnabled();

        sender.sendMessage(
                colorize(
                        "&7AegisAC: "
                                + (enabled
                                ? "&aONLINE"
                                : "&cDISABLED")
                )
        );

        sender.sendMessage(
                colorize(
                        "&7Checks: &f"
                                + checkManager.size()
        );

        sender.sendMessage(
                colorize(
                        "&7Players tracked: &f"
                                + playerDataManager.size()
        );
    }

    private void alerts(CommandSender sender) {
        if (!sender.hasPermission(
                "aegisac.alerts"
        )) {
            send(sender, "errors.no-permission");
            return;
        }

        send(sender, "command.alerts-enabled");
    }

    private void reset(
            CommandSender sender,
            String[] args
    ) {
        if (args.length < 2) {
            send(
                    sender,
                    "command.reset-usage"
            );
            return;
        }

        Player target =
                Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            send(
                    sender,
                    "errors.player-not-found",
                    Map.of("player", args[1])
            );
            return;
        }

        PlayerData data =
                playerDataManager.find(
                        target.getUniqueId()
                );

        if (data == null) {
            send(
                    sender,
                    "errors.player-not-tracked",
                    Map.of("player", target.getName())
            );
            return;
        }

        if (args.length >= 3) {
            String check =
                    args[2].toLowerCase(Locale.ROOT);

            data.resetViolation(check);

            send(
                    sender,
                    "command.reset-check",
                    Map.of(
                            "player",
                            target.getName(),
                            "check",
                            check
                    )
            );

            return;
        }

        data.resetViolations();

        send(
                sender,
                "command.reset-player",
                Map.of(
                        "player",
                        target.getName()
                )
        );
    }

    private void version(CommandSender sender) {
        send(
                sender,
                "command.version",
                Map.of(
                        "version",
                        plugin.getDescription()
                                .getVersion()
                )
        );
    }

    private void send(
            CommandSender sender,
            String path
    ) {
        sender.sendMessage(
                messages.getWithPrefix(path)
        );
    }

    private void send(
            CommandSender sender,
            String path,
            Map<String, ?> placeholders
    ) {
        sender.sendMessage(
                messages.getWithPrefix(
                        path,
                        placeholders
                )
        );
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes(
                '&',
                text
        );
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (!sender.hasPermission(
                "aegisac.admin"
        )) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filter(
                    List.of(
                            "help",
                            "reload",
                            "info",
                            "checks",
                            "status",
                            "alerts",
                            "reset",
                            "version"
                    ),
                    args[0]
            );
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("reset")) {
            List<String> names = new ArrayList<>();

            for (Player player :
                    Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }

            return filter(names, args[1]);
        }

        if (args.length == 3
                && args[0].equalsIgnoreCase("reset")) {
            List<String> names = new ArrayList<>();

            for (Check check :
                    checkManager.getChecks()) {
                names.add(check.getName());
            }

            return filter(names, args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(
            List<String> values,
            String input
    ) {
        String lower =
                input.toLowerCase(Locale.ROOT);

        List<String> result = new ArrayList<>();

        for (String value : values) {
            if (value.toLowerCase(
                    Locale.ROOT
            ).startsWith(lower)) {
                result.add(value);
            }
        }

        return result;
    }
                }
