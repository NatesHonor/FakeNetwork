package com.nate.hypixel.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nate.hypixel.HypixelRank;

public class EditorCommand implements CommandExecutor {
    private final HypixelRank plugin;

    public EditorCommand(HypixelRank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        String token = generateRandomToken();

        saveTokenToDatabase(player.getUniqueId(), token);
        String url = "http://localhost/editor/" + token;

        player.sendMessage(ChatColor.GREEN + "Your editor URL: " + ChatColor.YELLOW + url);
        return true;
    }

    private String generateRandomToken() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int index = (int) (Math.random() * characters.length());
            token.append(characters.charAt(index));
        }
        return token.toString();
    }

    private void saveTokenToDatabase(UUID playerUUID, String token) {
        try (Connection connection = plugin.getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("INSERT INTO webeditor (player_uuid, token) VALUES (?, ?)")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, token);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
