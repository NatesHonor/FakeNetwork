package com.nate.hypixel.Data.MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.nate.hypixel.Utils.Rank;

public class MySQLManager {
    private String host, database, username, password;
    private int port;
    private Connection connection;

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
