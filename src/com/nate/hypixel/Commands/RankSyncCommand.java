package com.nate.hypixel.Commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;

import com.nate.hypixel.Data.MySQL.MySQLManager;
import com.nate.hypixel.Utils.Rank;

import java.util.ArrayList;

public class RankSyncCommand extends Command implements Listener {
    private MySQLManager mySQLManager;

    public RankSyncCommand(MySQLManager mySQLManager) {
        super("ranksync");
        this.mySQLManager = mySQLManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("This command can only be executed by players."));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        String playerName = player.getName();
        List<Rank> playerRanks = fetchRanksFromDatabase(playerName);
        applyPermissions(player, playerRanks);
        player.sendMessage(new TextComponent("Rank synchronization successful."));
    }

    private List<Rank> fetchRanksFromDatabase(String playerName) {
        List<Rank> playerRanks = new ArrayList<>();
        List<Rank> ranksData = mySQLManager.getRanksForPlayer(playerName);
        for (Rank rankData : ranksData) {
            String rankName = rankData.getRankName();
            String prefix = rankData.getPrefix();
            int weight = rankData.getWeight();
            String permissionString = rankData.getPermissionString();
            Rank rank = new Rank(rankName, prefix, weight, permissionString);
            playerRanks.add(rank);
        }
        return playerRanks;
    }

    private void applyPermissions(ProxiedPlayer player, List<Rank> ranks) {
        List<String> playerPermissionsCopy = new ArrayList<>(player.getPermissions());
        for (String permission : playerPermissionsCopy) {
            player.setPermission(permission, false);
        }
        ranks.sort((r1, r2) -> Integer.compare(r2.getWeight(), r1.getWeight()));
        for (Rank rank : ranks) {
            List<String> permissions = rank.getPermissions();
            for (String permission : permissions) {
                player.setPermission(permission, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String playerName = player.getName();
        List<Rank> playerRanks = fetchRanksFromDatabase(playerName);
        applyPermissions(player, playerRanks);
    }
}
