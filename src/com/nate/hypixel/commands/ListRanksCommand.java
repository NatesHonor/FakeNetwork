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

public class ListRanksCommand implements CommandExecutor {
    @SuppressWarnings("unused")
    private final HypixelRank plugin;
    private final Connection connection;

    public ListRanksCommand(HypixelRank plugin) {
        this.plugin = plugin;
        this.connection = plugin.getConnection();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT rankname, prefix, weight FROM ranks ORDER BY weight DESC");
            ResultSet resultSet = statement.executeQuery();

            sender.sendMessage(ChatColor.YELLOW + "Ranks:");
            while (resultSet.next()) {
                String rankName = resultSet.getString("rankname");
                String prefix = ChatColor.translateAlternateColorCodes('&', resultSet.getString("prefix"));
                int weight = resultSet.getInt("weight");

                sender.sendMessage(ChatColor.GRAY + "  " + prefix + " " + rankName + " (Weight: " + weight + ")");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}
