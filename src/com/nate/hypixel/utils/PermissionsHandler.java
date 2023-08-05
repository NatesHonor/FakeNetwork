package com.nate.hypixel.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.nate.hypixel.HypixelRank;

public class PermissionsHandler {
    private Connection connection;
    private HypixelRank plugin;

    public PermissionsHandler(Connection connection, HypixelRank plugin) {
        this.connection = connection;
        this.plugin = plugin;
    }

    public void addPermissionToRank(CommandSender sender, String rankName, String permissionNode) {
        try {
            if (!rankExists(rankName)) {
                sender.sendMessage("No rank found with the name: " + rankName);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            List<String> existingPermissions = getRankPermissions(rankName);

            existingPermissions.add(permissionNode);

            String updatedPermissions = String.join(",", existingPermissions);

            String query = "UPDATE ranks SET permission = ? WHERE rankname = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, updatedPermissions);
            statement.setString(2, rankName);
            statement.executeUpdate();
            statement.close();

            sender.sendMessage("Added permission node: " + permissionNode + " to rank: " + rankName);

            // Update users with the new permission
            updateUsersPermissions(rankName, existingPermissions);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUsersPermissions(String rankName, List<String> permissions) {
        String query = "UPDATE users SET permissions = ? WHERE rankname = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            String permissionsString = String.join(",", permissions);
            statement.setString(1, permissionsString);
            statement.setString(2, rankName);
            statement.executeUpdate();

            // Update players' permissions in-game
            plugin.updatePlayersPermissions(this.connection, rankName, permissions);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean rankExists(String rankName) throws SQLException {
        String query = "SELECT * FROM ranks WHERE rankname = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, rankName);
        ResultSet resultSet = statement.executeQuery();
        boolean exists = resultSet.next();
        resultSet.close();
        statement.close();
        return exists;
    }

    private List<String> getRankPermissions(String rankName) throws SQLException {
        String query = "SELECT permission FROM ranks WHERE rankname = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, rankName);
        ResultSet resultSet = statement.executeQuery();
        List<String> permissions = new ArrayList<>();
        if (resultSet.next()) {
            String permissionString = resultSet.getString("permission");
            if (permissionString != null && !permissionString.isEmpty()) {
                permissions = new ArrayList<>(Arrays.asList(permissionString.split(",")));
            }
        }
        resultSet.close();
        statement.close();
        return permissions;
    }
}
