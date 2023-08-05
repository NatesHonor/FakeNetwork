package com.nate.hypixel.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.nate.hypixel.HypixelRank;
import com.nate.hypixel.utils.PermissionsHandler;

public class Permission implements CommandExecutor {
    private HypixelRank plugin;

    public Permission(HypixelRank plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length >= 3) {
            String rankName = args[2];
            String permissionNode = args[3];

            execute(sender, rankName, permissionNode);

        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /permission <rankname> <permission>");
        }
        return true;
    }

    public void execute(CommandSender sender, String rankName, String permissionNode) {
        PermissionsHandler permissionsHandler = new PermissionsHandler(plugin.getConnection(), plugin);
        permissionsHandler.addPermissionToRank(sender, rankName, permissionNode);
    }
}
