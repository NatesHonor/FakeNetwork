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
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.nate.hypixel.HypixelRank;
import com.nate.hypixel.utils.data.mysql.Rank;

public class SetRankCommand implements CommandExecutor, Listener {
    private final HypixelRank plugin;
    private final Connection connection;

    public SetRankCommand(HypixelRank plugin) {
        this.plugin = plugin;
        this.connection = plugin.getConnection();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /rank <player> <rank>");
            return true;
        }

        String playerName = args[1];
        String rankName = args[2];

        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (!rankExists(rankName)) {
            sender.sendMessage(ChatColor.RED + "The specified rank does not exist.");
            return true;
        }
        Rank rank = new Rank(plugin);
        rank.setRank(player, rankName);

        sender.sendMessage(ChatColor.GREEN + "Rank set successfully.");

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

}
