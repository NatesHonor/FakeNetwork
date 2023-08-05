package com.nate.hypixel.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nate.hypixel.HypixelRank;

public class Test implements CommandExecutor {
	@SuppressWarnings("unused")
	private final HypixelRank plugin;

	public Test(HypixelRank plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("this.is.a.test")) {
			sender.sendMessage("Hey! It works and you have the permission this.is.a.test!");
		} else {
			sender.sendMessage("You don't have permission to run this command");
		}
		return true;
	}
}
