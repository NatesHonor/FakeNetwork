package com.nate.hypixel.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nate.hypixel.HypixelRank;

public class ListCommand implements CommandExecutor {
    @SuppressWarnings("unused")
    private final HypixelRank plugin;
    private final Connection connection;

    public ListCommand(HypixelRank plugin) {
        this.plugin = plugin;
        this.connection = plugin.getConnection();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            String rankName = args[2];

            try {
                String query = "SELECT permission FROM ranks WHERE rankname = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, rankName);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String permissions = resultSet.getString("permission");
                    if (permissions != null && !permissions.isEmpty()) {
                        String[] permissionNodes = permissions.split(",");
                        StringBuilder formattedPermissions = new StringBuilder();
                        for (String permission : permissionNodes) {
                            formattedPermissions.append(permission.trim()).append(", ");
                        }
                        String formattedResult = formattedPermissions.substring(0, formattedPermissions.length() - 2);

                        sender.sendMessage(ChatColor.YELLOW + "Permissions for rank " + rankName + ":");
                        sender.sendMessage(ChatColor.GREEN + formattedResult);
                    } else {
                        sender.sendMessage(ChatColor.RED + "The specified rank does not have any permissions.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "The specified rank does not exist.");
                }

                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                sender.sendMessage(ChatColor.RED + "An error occurred while retrieving the permissions.");
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /viewpermissions <rankname>");
        }

        return true;
    }
}
