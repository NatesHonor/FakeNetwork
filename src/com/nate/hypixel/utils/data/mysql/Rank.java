package com.nate.hypixel.utils.data.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.nate.hypixel.HypixelRank;

public class Rank {
	private final HypixelRank plugin;
	private final Connection connection;

	public Rank(HypixelRank plugin) {
		this.plugin = plugin;
		this.connection = plugin.getConnection();
	}

	public void defaultRank(Player player) {
		String query = "SELECT rankname FROM users WHERE username = ?";
		try (PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, player.getName());
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				setRank(player, "default");
			}
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setRank(Player player, String rankName) {

		try {
			PreparedStatement checkStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
			checkStatement.setString(1, player.getName());
			ResultSet resultSet = checkStatement.executeQuery();

			if (resultSet.next()) {
				PreparedStatement updateStatement = connection
						.prepareStatement("UPDATE users SET rankname = ? WHERE username = ?");
				updateStatement.setString(1, rankName);
				updateStatement.setString(2, player.getName());
				updateStatement.executeUpdate();
				updateStatement.close();
			} else {
				PreparedStatement insertStatement = connection
						.prepareStatement("INSERT INTO users (username, rankname) VALUES (?, ?)");
				insertStatement.setString(1, player.getName());
				insertStatement.setString(2, rankName);
				insertStatement.executeUpdate();
				insertStatement.close();
			}

			resultSet.close();
			checkStatement.close();
			Prefix prefix = new Prefix(plugin);
			prefix.updatePlayerPrefix(player);
			ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
			Scoreboard scoreboard = scoreboardManager.getMainScoreboard();
			Team team = scoreboard.getTeam(rankName);
			team.addEntry(player.getName());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addRank(Player player, String rankName) {
		try {
			String query = "SELECT rankname FROM users WHERE username = ?";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, player.getName());
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				String existingRank = resultSet.getString("rankname");
				if (!existingRank.equalsIgnoreCase(rankName)) {
					String updatedRank = existingRank + ", " + rankName;

					query = "UPDATE users SET rankname = ? WHERE username = ?";
					statement = connection.prepareStatement(query);
					statement.setString(1, updatedRank);
					statement.setString(2, player.getName());
					statement.executeUpdate();

					player.sendMessage(
							ChatColor.GREEN + "Successfully added rank '" + rankName + "' to your existing ranks.");
				} else {
					player.sendMessage(ChatColor.RED + "You already have the rank '" + rankName + "'.");
				}
			} else {
				player.sendMessage(
						ChatColor.RED + "You don't have an existing rank. Use /setrank command to set your rank.");
			}

			resultSet.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
