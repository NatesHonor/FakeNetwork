package com.nate.hypixel.Data.MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.nate.hypixel.Utils.Rank;

import net.md_5.bungee.api.ChatColor;

public class MySQLManager {
    private String host, database, username, password;
    private int port;
    private static Connection connection;

    public MySQLManager(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public List<Rank> getRanksForPlayer(String playerName) {
        List<Rank> ranksData = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT ranks.rankname, ranks.prefix, ranks.weight, ranks.permission " +
                        "FROM ranks " +
                        "INNER JOIN users ON ranks.rankname = users.rankname " +
                        "WHERE username = ?")) {
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String rankName = resultSet.getString("rankname");
                String prefix = resultSet.getString("prefix");
                int weight = resultSet.getInt("weight");
                String permissionString = resultSet.getString("permission");
                Rank rankData = new Rank(rankName, prefix, weight, permissionString);
                ranksData.add(rankData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ranksData;
    }

    public static List<Rank> getRanksForPlayer2(String playerName) {
        List<Rank> ranksData = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT ranks.rankname, ranks.prefix, ranks.weight, ranks.permission " +
                        "FROM ranks " +
                        "INNER JOIN users ON ranks.rankname = users.rankname " +
                        "WHERE username = ?")) {
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String rankName = resultSet.getString("rankname");
                String prefix = resultSet.getString("prefix");
                int weight = resultSet.getInt("weight");
                String permissionString = resultSet.getString("permission");
                Rank rankData = new Rank(rankName, prefix, weight, permissionString);
                ranksData.add(rankData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ranksData;
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

    public static String getHighestWeightRankPrefix(String player) {
        String query = "SELECT prefix FROM ranks WHERE rankname IN (SELECT rankname FROM users WHERE username = ?) ORDER BY weight DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, player);
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

    public String getHighestRankPrefix(Player player) {
        String highestRank = getRankName(player);
        String prefix = getPrefixForRank(highestRank);
        return prefix;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
