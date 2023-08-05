package com.nate.hypixel.utils.data.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.nate.hypixel.HypixelRank;

public class Prefix {
    @SuppressWarnings("unused")
    private final HypixelRank plugin;
    private final Connection connection;

    public Prefix(HypixelRank plugin) {
        this.plugin = plugin;
        this.connection = plugin.getConnection();
    }

    public void updatePlayerPrefix(Player player) {
        String rankName = getRankName(player);
        String prefix = getPrefixForRank(rankName);

        player.setPlayerListName(prefix + " " + player.getName());
        player.setDisplayName(prefix + " " + player.getName());
        player.setCustomNameVisible(true);
        player.setCustomName(prefix + " " + player.getName());

        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
        Team team = scoreboard.getTeam(rankName);
        if (team == null) {
            team = scoreboard.registerNewTeam(rankName);
        }
        team.addEntry(player.getName());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "lp user " + player.getName() + " meta setprefix 100 " + prefix);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " refresh");
    }

    public String getPrefixForRank(String rankName) {
        String query = "SELECT prefix FROM ranks WHERE rankname = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, rankName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return ChatColor.translateAlternateColorCodes('&', resultSet.getString("prefix"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<String> getPermissionsForRank(String rankName) {
        List<String> permissions = new ArrayList<>();
        String query = "SELECT permission FROM ranks WHERE rankname = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, rankName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String permissionString = resultSet.getString("permission");
                if (permissionString != null && !permissionString.isEmpty()) {
                    String[] permissionArray = permissionString.split(",");
                    for (String permission : permissionArray) {
                        permissions.add(permission.trim());
                    }
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public String getRankName(Player player) {
        String rankName = null;
        int highestWeight = 0;

        String query = "SELECT rankname FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player.getName());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String ranks = resultSet.getString("rankname");
                String[] individualRanks = ranks.split(", ");

                for (String currentRank : individualRanks) {
                    int currentWeight = getRankWeight(currentRank);

                    if (currentWeight > highestWeight) {
                        highestWeight = currentWeight;
                        rankName = currentRank;
                    }
                }
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Bukkit.getLogger().info(rankName);
        return rankName;
    }

    public String getHighestWeightRankPrefix(Player player) {
        String query = "SELECT prefix FROM ranks WHERE rankname IN (SELECT rankname FROM users WHERE username = ?) ORDER BY weight DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player.getName());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("prefix");
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public int getRankWeight(String rankName) {
        int weight = 0;
        String query = "SELECT weight FROM ranks WHERE rankname = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, rankName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                weight = resultSet.getInt("weight");
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return weight;
    }

    public String getPrefix(Player player) {
        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT prefix FROM ranks INNER JOIN users ON ranks.rankname = users.rankname " +
                            "WHERE users.username = ?");
            statement.setString(1, player.getName());
            ResultSet resultSet = statement.executeQuery();

            StringBuilder prefixBuilder = new StringBuilder();

            while (resultSet.next()) {
                String prefix = ChatColor.translateAlternateColorCodes('&', resultSet.getString("prefix"));
                prefixBuilder.append(prefix).append(" ");
            }

            resultSet.close();
            statement.close();

            String prefixString = prefixBuilder.toString().trim();
            return prefixString;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

}
