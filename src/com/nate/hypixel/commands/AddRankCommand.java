package com.nate.hypixel.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nate.hypixel.HypixelRank;
import com.nate.hypixel.utils.data.mysql.Rank;

public class AddRankCommand implements CommandExecutor {
    private final HypixelRank plugin;
    private final Rank rank;

    public AddRankCommand(HypixelRank plugin) {
        this.plugin = plugin;
        this.rank = new Rank(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("rank.addrank")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            String playerName = args[1];
            String rankName = args[2];

            Player targetPlayer = plugin.getServer().getPlayerExact(playerName);

            if (targetPlayer == null) {
                player.sendMessage(ChatColor.RED + "Player '" + playerName + "' is not online.");
                return true;
            }

            rank.addRank(targetPlayer, rankName);
        }

        return true;
    }
}
