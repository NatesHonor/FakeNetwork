package com.nate.hypixel.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.nate.hypixel.HypixelRank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RankCommand implements CommandExecutor {
    @SuppressWarnings("unused")
    private final HypixelRank plugin;
    private final Connection connection;

    public RankCommand(HypixelRank plugin) {
        this.plugin = plugin;
        this.connection = plugin.getConnection();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /rank create <rankname> <prefix> <weight>");
            return true;
        }

        String rankName = args[1];
        String prefix = args[2];
        int weight;

        try {
            weight = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid weight. Please enter a number.");
            return true;
        }

        if (rankExists(rankName)) {
            sender.sendMessage(ChatColor.RED + "A rank with that name already exists.");
            return true;
        }

        createRank(rankName, prefix, weight);
        sender.sendMessage(ChatColor.GREEN + "Rank created successfully.");

        return true;
    }

    private boolean rankExists(String rankName) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT rankname FROM ranks WHERE rankname = ?");
            statement.setString(1, rankName);
            ResultSet resultSet = statement.executeQuery();

            boolean exists = resultSet.next();

            resultSet.close();
            statement.close();

            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createRank(String rankName, String prefix, int weight) {
        try {
            PreparedStatement statement = connection
                    .prepareStatement("INSERT INTO ranks (rankname, prefix, weight) VALUES (?, ?, ?)");
            statement.setString(1, rankName);
            statement.setString(2, ChatColor.translateAlternateColorCodes('&', prefix));
            statement.setInt(3, weight);
            statement.executeUpdate();
            statement.close();

            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = scoreboard.registerNewTeam(rankName);
            team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix + " "));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
