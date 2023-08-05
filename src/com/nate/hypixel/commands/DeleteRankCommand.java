package com.nate.hypixel.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.nate.hypixel.HypixelRank;

public class DeleteRankCommand implements CommandExecutor {
    @SuppressWarnings("unused")
    private final HypixelRank plugin;
    private final Connection connection;

    public DeleteRankCommand(HypixelRank plugin) {
        this.plugin = plugin;
        this.connection = plugin.getConnection();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /hypixelrank delete <rankname>");
            return true;
        }

        String rankName = args[1];

        if (!rankExists(rankName)) {
            sender.sendMessage(ChatColor.RED + "The specified rank does not exist.");
            return true;
        }

        deleteRank(rankName);
        sender.sendMessage(ChatColor.GREEN + "Rank deleted successfully.");

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

    private void deleteRank(String rankName) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM ranks WHERE rankname = ?");
            statement.setString(1, rankName);
            statement.executeUpdate();
            statement.close();

            // Delete the team from the scoreboard
            ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
            Team team = scoreboard.getTeam(rankName);
            if (team != null) {
                team.unregister();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
