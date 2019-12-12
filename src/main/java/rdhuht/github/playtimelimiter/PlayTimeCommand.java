package rdhuht.github.playtimelimiter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import rdhuht.github.playtimelimiter.exceptions.UnknownPlayerException;
import rdhuht.github.playtimelimiter.utils.TabCompleteHelper;


public class PlayTimeCommand implements TabExecutor {
    private final PlayTimeLimiter plugin;

    public PlayTimeCommand(PlayTimeLimiter plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        boolean isPlayer = true;
        if (!(sender instanceof Player)) {
            isPlayer = false;
        }

        if (args.length == 0) {
            this.printUsage(sender);
            return false;
        }

        if (args[0].equalsIgnoreCase("start") && args.length == 1) {
            if (!sender.hasPermission("playtimelimiter.playtime.start")) {
                sender.sendMessage(ChatColor.RED
                        + "You don't have permission to start the playtime counter!");
                return false;
            } else {
                if (plugin.start()) {
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED
                            + "Playtime already started!");
                    return false;
                }
            }
        } else if (args[0].equalsIgnoreCase("stop") && args.length == 1) {
            if (!sender.hasPermission("playtimelimiter.playtime.stop")) {
                sender.sendMessage(ChatColor.RED
                        + "You don't have permission to stop the playtime counter!");
                return false;
            } else {
                if (plugin.stop()) {
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED
                            + "Playtime already stopped!");
                    return false;
                }
            }
        } else if (args[0].equalsIgnoreCase("add") && args.length == 3) {
            if (!plugin.hasStarted()) {
                sender.sendMessage(ChatColor.RED
                        + "Playtime hasn't started yet!");
                return false;
            }
            if (!sender.hasPermission("playtimelimiter.playtime.add")) {
                sender.sendMessage(ChatColor.RED
                        + "You don't have permission to add time to a players playtime!");
                return false;
            } else {
                try {
                    plugin.addPlayTime(
                            plugin.getServer().getOfflinePlayer(args[1])
                                    .getUniqueId(), Integer.parseInt(args[2]));
                    sender.sendMessage(ChatColor.GREEN + "Added "
                            + Integer.parseInt(args[2])
                            + " seconds of playtime to " + args[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED
                            + "Invalid number of seconds given!");
                    return false;
                } catch (UnknownPlayerException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }
                return true;
            }
        } else if (args[0].equalsIgnoreCase("remove") && args.length == 3) {
            if (!plugin.hasStarted()) {
                sender.sendMessage(ChatColor.RED
                        + "Playtime hasn't started yet!");
                return false;
            }
            if (!sender.hasPermission("playtimelimiter.playtime.remove")) {
                sender.sendMessage(ChatColor.RED
                        + "You don't have permission to remove time from a players playtime!");
                return false;
            } else {
                try {
                    plugin.removePlayTime(
                            plugin.getServer().getOfflinePlayer(args[1])
                                    .getUniqueId(), Integer.parseInt(args[2]));
                    sender.sendMessage(ChatColor.GREEN + "Removed "
                            + Integer.parseInt(args[2])
                            + " seconds of playtime from " + args[1]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED
                            + "Invalid number of seconds given!");
                    return false;
                } catch (UnknownPlayerException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }
                return true;
            }
        } else if (args[0].equalsIgnoreCase("set") && args.length == 3) {
            if (!plugin.hasStarted()) {
                sender.sendMessage(ChatColor.RED
                        + "Playtime hasn't started yet!");
                return false;
            }
            if (!sender.hasPermission("playtimelimiter.playtime.set")) {
                sender.sendMessage(ChatColor.RED
                        + "You don't have permission to set a player's playtime!");
                return false;
            } else {
                try {
                    plugin.setPlayTime(
                            plugin.getServer().getOfflinePlayer(args[1])
                                    .getUniqueId(), Integer.parseInt(args[2]));
                    sender.sendMessage(ChatColor.GREEN + "Set "
                            + args[1]
                            + " playtime to " + Integer.parseInt(args[2]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED
                            + "Invalid number of seconds given!");
                    return false;
                } catch (UnknownPlayerException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }
                return true;
            }
        } else if (args[0].equalsIgnoreCase("check")) {
            if (!plugin.hasStarted()) {
                sender.sendMessage(ChatColor.RED
                        + "Playtime hasn't started yet!");
                return false;
            }
            if (args.length == 1 && isPlayer) {
                if (!sender
                        .hasPermission("playtimelimiter.playtime.check.self")) {
                    sender.sendMessage(ChatColor.RED
                            + "You don't have permission to check your playtime!");
                    return false;
                } else {
                    sender.sendMessage(ChatColor.RESET
//							+ "You have played for "
//							+ plugin.secondsToDaysHoursSecondsString(plugin
//									.getPlayerPlayTime(plugin.getServer()
//											.getOfflinePlayer(sender.getName())
//											.getUniqueId()))
//							+ " and have "
                            + "You have "
                            + ChatColor.GREEN
                            + plugin.secondsToDaysHoursSecondsString(plugin
                            .getTimeAllowedInSeconds(plugin.getServer()
                                    .getOfflinePlayer(sender.getName())
                                    .getUniqueId()))
                            + ChatColor.RESET + " remaining!\n");
                    return true;
                }
            } else if (args.length == 2) {
                if (!sender
                        .hasPermission("playtimelimiter.playtime.check.others")) {
                    sender.sendMessage(ChatColor.RED
                            + "You don't have permission to check other players playtime!");
                    return false;
                } else {
                    sender.sendMessage(ChatColor.GREEN
                            + args[1]
                            + " has played for "
                            + plugin.secondsToDaysHoursSecondsString(plugin
                            .getPlayerPlayTime(plugin.getServer()
                                    .getOfflinePlayer(args[1])
                                    .getUniqueId()))
                            + " and has "
                            + plugin.secondsToDaysHoursSecondsString(plugin
                            .getTimeAllowedInSeconds(plugin.getServer()
                                    .getOfflinePlayer(args[1])
                                    .getUniqueId())) + " remaining!");
                    return true;
                }
            }
        }
        this.printUsage(sender);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] parameters) {
        if (parameters.length == 1) {
            return TabCompleteHelper.matchTo(parameters, new String[]{"start", "stop", "add", "remove", "set",
                    "check"});
        } else if (parameters.length == 2
                && (parameters[1].equals("add") || parameters[1].equals("remove") || parameters[1].equals("check")
                || parameters[1].equals("set"))) {
            return TabCompleteHelper.matchTo(parameters, TabCompleteHelper.getKnownPlayerNames());
        } else {
            return null;
        }
    }

    public void printUsage(CommandSender sender) {
        List<String> usage = new ArrayList<String>();
        usage.add(ChatColor.YELLOW + "/playtime usage:");
        if (sender.hasPermission("playtimelimiter.playtime.start")) {
            usage.add(ChatColor.AQUA + "/playtime start" + ChatColor.RESET
                    + " - Start the playtime counter.");
        }
        if (sender.hasPermission("playtimelimiter.playtime.stop")) {
            usage.add(ChatColor.AQUA + "/playtime stop" + ChatColor.RESET
                    + " - Stop the playtime counter.");
        }
        if (sender.hasPermission("playtimelimiter.playtime.add")) {
            usage.add(ChatColor.AQUA + "/playtime add <user> <time>"
                    + ChatColor.RESET
                    + " - Add time in seconds to the user's playtime.");
        }
        if (sender.hasPermission("playtimelimiter.playtim.set")) {
            usage.add(ChatColor.AQUA + "/playtime set <user> <time>"
                    + ChatColor.RESET
                    + " - Set a player's playtime in seconds.");
        }
        if (sender.hasPermission("playtimelimiter.playtime.check.others")) {
            usage.add(ChatColor.AQUA
                    + "/playtime check [user]"
                    + ChatColor.RESET
                    + " - Check the time played and time left for a given user, or if blank, for yourself.");
        } else if (sender.hasPermission("playtimelimiter.playtime.check.self")) {
            usage.add(ChatColor.AQUA + "/playtime check" + ChatColor.RESET
                    + " - Check the time played and time left for yourself.");
        }
        if (sender.hasPermission("playtimelimiter.playtime.remove")) {
            usage.add(ChatColor.AQUA + "/playtime remove <user> <time>"
                    + ChatColor.RESET
                    + " - Remove time in seconds from the user's playtime.");
        }
        sender.sendMessage(usage.toArray(new String[usage.size()]));
    }
}