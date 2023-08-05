package com.nate.hypixel;

import java.sql.SQLException;

import com.nate.hypixel.Commands.RankSyncCommand;
import com.nate.hypixel.Data.MySQL.MySQLManager;

import net.md_5.bungee.api.plugin.Plugin;

public class Rank extends Plugin {
    MySQLManager mySQLManager = new MySQLManager("localhost", 3306, "fakenetwork", "root", "");

    @Override
    public void onEnable() {

        try {
            mySQLManager.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getProxy().getPluginManager().registerCommand(this, new RankSyncCommand(mySQLManager));
        getProxy().getPluginManager().registerListener(this, new RankSyncCommand(mySQLManager));
    }

    @Override
    public void onDisable() {

        try {
            mySQLManager.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
