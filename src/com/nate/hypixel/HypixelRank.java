package com.nate.hypixel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.nate.hypixel.commands.AddRankCommand;
import com.nate.hypixel.commands.DeleteRankCommand;
import com.nate.hypixel.commands.EditorCommand;
import com.nate.hypixel.commands.ListCommand;
import com.nate.hypixel.commands.ListRanksCommand;
import com.nate.hypixel.commands.Permission;
import com.nate.hypixel.commands.RankCommand;
import com.nate.hypixel.commands.SetRankCommand;
import com.nate.hypixel.commands.Test;
import com.nate.hypixel.utils.data.mysql.Prefix;

public class HypixelRank extends JavaPlugin implements Listener {
    private Connection connection;

    @Override
    public void onEnable() {
        connection = setupDatabase();

        if (connection != null) {
            createRanksTable();
            createUserTable();
        }

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("test").setExecutor(new Test(this));
        getCommand("rank").setExecutor(this);
    }

    @Override
    public void onDisable() {
        disconnectFromDatabase();
    }

    private Connection setupDatabase() {
        String url = "jdbc:mysql://localhost:3306/fakenetwork?useSSL=false&serverTimezone=UTC";
        String username = "root";
        String password = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    private void disconnectFromDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createRanksTable() {
        try {
            Statement statement = connection.createStatement();
            String query = "CREATE TABLE IF NOT EXISTS ranks (rankname VARCHAR(255), prefix VARCHAR(255), weight INT, permission TEXT DEFAULT NULL)";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUserTable() {
        try {
            Statement statement = connection.createStatement();
            String query = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255), player_id VARCHAR(255), rankname VARCHAR(255), permissions VARCHAR(255), prefix_category VARCHAR(255), total_playtime BIGINT DEFAULT 0)";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void updatePlayerPrefix(Player player, String prefix) {
        String playerName = player.getName();
        player.setDisplayName(prefix + playerName);
        player.setPlayerListName(prefix + playerName);
        player.setCustomName(prefix + playerName);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " meta setprefix 100 " + prefix);
    }

    public void updatePlayersPermissions(Connection connection, String rankName, List<String> permissions) {
        String query = "SELECT username FROM users WHERE rankname = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, rankName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                Player player = Bukkit.getPlayerExact(username);
                if (player != null) {
                    for (String permission : permissions) {
                        player.addAttachment(this, permission, true);
                    }
                    player.recalculatePermissions();
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Prefix prefix = new Prefix(this);
        Player player = event.getPlayer();
    
        if (prefix.getRankName(player) == null) {
            return;
        }
    
        prefix.updatePlayerPrefix(player);
        updatePlayersPermissions(connection, prefix.getRankName(player), prefix.getPermissionsForRank(prefix.getRankName(player)));
        player.recalculatePermissions();
    }
    

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Prefix prefixManager = new Prefix(this);
        Player player = e.getPlayer();
        String message = e.getMessage();
        String prefix = prefixManager.getPrefixForRank(prefixManager.getRankName(player));

        if (!player.hasPermission("hypixel.staff")) {
            if (!player.hasPermission("hypixel.chat.color")) {
                String formattedMessage = ChatColor.translateAlternateColorCodes('&', prefix + " " + player.getName()
                        + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
                e.setCancelled(true);
                Bukkit.getServer().broadcastMessage(formattedMessage);
            } else {
                String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                        prefix + " " + player.getName() + ChatColor.WHITE + ": " + message);
                e.setCancelled(true);
                Bukkit.getServer().broadcastMessage(formattedMessage);
            }
        } else {
            String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                    prefix + " " + player.getName() + ChatColor.RESET + ": " + ChatColor.GREEN + message);
            e.setCancelled(true);
            Bukkit.getServer().broadcastMessage(formattedMessage);
        }
    }

    public void log(String message) {
        Bukkit.getLogger().info(message);
    }

    public boolean console(CommandSender sender) {
        return !(sender instanceof Player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rank")) {
            if (sender.hasPermission("rank.use")) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.GREEN + "Available commands:");
                    sender.sendMessage(ChatColor.GOLD + "- create: Create a new rank");
                    sender.sendMessage(ChatColor.GOLD + "- list: List all ranks");
                    sender.sendMessage(ChatColor.GOLD + "- assign: Assign a rank to a player");
                    sender.sendMessage(ChatColor.GOLD + "- delete: Delete a rank");
                    return true;
                } else if (args.length >= 1) {
                    String subCommand = args[0].toLowerCase();
                    switch (subCommand) {
                        case "create":
                            if (args.length >= 4) {
                                RankCommand createCommand = new RankCommand(this);
                                createCommand.onCommand(sender, command, label, args);
                                return true;
                            } else {
                                sender.sendMessage(
                                        ChatColor.RED + "Usage: /rank create <rankname> <prefix> <weight>");
                            }
                            break;
                        case "editor":
                            EditorCommand editorCommand = new EditorCommand(this);
                            editorCommand.onCommand(sender, command, label, args);
                            break;
                        case "delete":
                            if (args.length >= 2) {
                                DeleteRankCommand deleteCommand = new DeleteRankCommand(this);
                                deleteCommand.onCommand(sender, command, label, args);
                            } else {
                                sender.sendMessage(ChatColor.RED + "Usage: /rank delete <rankname>");
                            }
                            break;
                        case "list":
                            ListRanksCommand listCommand = new ListRanksCommand(this);
                            listCommand.onCommand(sender, command, label, args);
                            break;
                        case "assign":
                            if (args.length >= 3) {
                                SetRankCommand setRankCommand = new SetRankCommand(this);
                                setRankCommand.onCommand(sender, command, label, args);
                            } else {
                                sender.sendMessage(ChatColor.RED + "Usage: /rank assign <player> <rankname>");
                            }
                            break;
                        case "add":
                            if (args.length >= 3) {
                                AddRankCommand addRankCommand = new AddRankCommand(this);
                                addRankCommand.onCommand(sender, command, label, args);
                            } else {
                                sender.sendMessage(ChatColor.RED + "Usage: /rank add <player> <rank>");
                            }
                            break;
                        case "permission":
                            if (args.length >= 2) {
                                if (args[1].equals("add")) {
                                    if (args.length >= 4) {
                                        Permission permissionCommand = new Permission(this);
                                        permissionCommand.onCommand(sender, command, label, args);
                                    } else {
                                        sender.sendMessage(ChatColor.RED
                                                + "Usage: /rank permission add <group> <permission>");
                                    }
                                } else if (args[1].equals("list")) {
                                    if (args.length >= 3) {
                                        ListCommand listCommand1 = new ListCommand(this);
                                        listCommand1.onCommand(sender, command, label, args);
                                    } else {
                                        sender.sendMessage(
                                                ChatColor.RED + "Usage: /rank permission list <group>");
                                    }
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Unknown permission subcommand: " + args[1]);
                                }
                            } else {
                                sender.sendMessage(
                                        ChatColor.RED + "Usage: /rank permission add <group> <permission>");
                                sender.sendMessage(ChatColor.RED + "Usage: /rank permission list <group>");
                            }
                            break;

                        default:
                            sender.sendMessage(ChatColor.RED + "Unknown command: " + subCommand);
                            break;
                    }
                    return true;
                }
            }
        } else {
            sender.sendMessage("Unknown Command. Type ./help for help!");
        }
        return false;
    }

}
