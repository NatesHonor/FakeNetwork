package com.nate.hypixel.API;

import com.nate.hypixel.Data.MySQL.MySQLManager;

public class GetRank {

    public void getUserRank(String player) {
        MySQLManager.getRanksForPlayer2(player);
    }
}
