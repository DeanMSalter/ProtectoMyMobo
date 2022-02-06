package McEssence.ProtectoMyMobo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;


public class Commands implements CommandExecutor {
    private final Config config;
    private final Main main;
    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("ProtectoMyMobo");

    public Commands(Config configTemp, Main mainTemp) {
        config = configTemp;
        main = mainTemp;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args == null || args[0] == null) {
            return false;
        }
        switch (args[0].toUpperCase()) {
            case "RELOAD":
                reload(commandSender, command, label, args);
                break;
            case "TRUST":
                trust(commandSender, command, label, args);
                break;
            case "UNTRUST":
                unTrust(commandSender, command, label, args);
                break;
            case "TRUSTLIST":
                trustList(commandSender, command, label, args);
                break;
            default:
                break;
        }
        return true;
    }

    private Boolean trust(CommandSender commandSender, Command command, String s, String[] args) {
        try {
            if (!checkPlayerRan(commandSender)) {
                commandSender.sendMessage("Command not ran by player");
                return true;
            }

            if (!checkPlayerNameSupplied(args)) {
                commandSender.sendMessage("No player name supplied");
                return true;
            }

            Player playerToTrust = Bukkit.getServer().getPlayer(args[1]);

            if (playerToTrust == null) {
                commandSender.sendMessage("Player not found");
                return true;
            }
            Player player = (Player) commandSender;

            File trustsFile = new File(main.getDataFolder(), "trusts.yml");
            FileConfiguration trustsConfig = YamlConfiguration.loadConfiguration(trustsFile);

            ArrayList<String> trustedPlayers = new ArrayList<>();
            if (trustsConfig.getList(String.valueOf(player.getUniqueId())) != null) {
                trustedPlayers = (ArrayList<String>) trustsConfig.getList(String.valueOf(player.getUniqueId()));
            }

            String playerUUIDToAdd = String.valueOf(playerToTrust.getUniqueId());
            if (!trustedPlayers.contains(playerUUIDToAdd)) {
                trustedPlayers.add(playerUUIDToAdd);
                trustsConfig.set(String.valueOf(player.getUniqueId()), trustedPlayers);
                trustsConfig.save(trustsFile);
                commandSender.sendMessage("Successfully trusted player.");
            } else {
                commandSender.sendMessage("Player already trusted.");
            }
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Exception " + e.getMessage());
            return false;
        }
    }

    private Boolean unTrust(CommandSender commandSender, Command command, String s, String[] args) {
        try {
            if (!checkPlayerRan(commandSender)) {
                commandSender.sendMessage("Command not ran by player");
                return true;
            }

            if (!checkPlayerNameSupplied(args)) {
                commandSender.sendMessage("No player name supplied");
                return true;
            }

            String playerNameToUntrust = args[1];
            Player player = (Player) commandSender;

            File trustsFile = new File(main.getDataFolder(), "trusts.yml");
            FileConfiguration trustsConfig = YamlConfiguration.loadConfiguration(trustsFile);

            ArrayList<String> trustedPlayers = new ArrayList<>();
            if (trustsConfig.getList(String.valueOf(player.getUniqueId())) != null) {
                trustedPlayers = (ArrayList<String>) trustsConfig.getList(String.valueOf(player.getUniqueId()));
            }
            String UUIDOfPlayerToUntrust = null;
            for (String trustedPlayer : trustedPlayers) {
                String trustedPlayerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(trustedPlayer)).getName();
                if (trustedPlayerName.equalsIgnoreCase(playerNameToUntrust)) {
                    UUIDOfPlayerToUntrust = String.valueOf(UUID.fromString(trustedPlayer));
                    break;
                }
            }
            if (UUIDOfPlayerToUntrust == null) {
                commandSender.sendMessage("You have not trusted that player.");
                return true;
            }
            trustedPlayers.remove(UUIDOfPlayerToUntrust);
            trustsConfig.set(String.valueOf(player.getUniqueId()), trustedPlayers);
            trustsConfig.save(trustsFile);
            commandSender.sendMessage("You have unTrusted " + playerNameToUntrust);

            return true;
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Exception " + e.getMessage());
            return false;
        }
    }

    private Boolean trustList(CommandSender commandSender, Command command, String s, String[] args) {
        try {
            if (!checkPlayerRan(commandSender)) {
                commandSender.sendMessage("Command not ran by player");
                return true;
            }

            Player player = (Player) commandSender;

            File trustsFile = new File(main.getDataFolder(), "trusts.yml");
            FileConfiguration trustsConfig = YamlConfiguration.loadConfiguration(trustsFile);

            ArrayList<String> trustedPlayers = new ArrayList<>();
            if (trustsConfig.getList(String.valueOf(player.getUniqueId())) != null) {
                trustedPlayers = (ArrayList<String>) trustsConfig.getList(String.valueOf(player.getUniqueId()));
            }
            String trustedPlayersString = "";
            for (String trustedPlayer : trustedPlayers) {
                String trustedPlayerName = Bukkit.getServer().getOfflinePlayer(UUID.fromString(trustedPlayer)).getName();
                trustedPlayersString = trustedPlayersString + trustedPlayerName;
            }
            if (trustedPlayersString == "") {
                commandSender.sendMessage("You have not trusted any players.");
                return true;
            } else {
                commandSender.sendMessage(trustedPlayersString);
            }
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Exception " + e.getMessage());
            return false;
        }
    }

    private Boolean reload(CommandSender commandSender, Command command, String s, String[] args) {
        plugin.reloadConfig();
        commandSender.sendMessage("Reload Complete");
        return true;
    }


    private Boolean checkPlayerRan(CommandSender commandSender) {
        return commandSender instanceof Player;
    }

    private Boolean checkPlayerNameSupplied(String[] args) {
        return args != null && args[1] != null;
    }
}

