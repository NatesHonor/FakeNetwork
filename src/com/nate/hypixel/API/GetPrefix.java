package com.nate.hypixel.API;

import com.nate.hypixel.Data.MySQL.MySQLManager;

public class GetPrefix {

    public void getPlayerPrefix(String player) {
        MySQLManager.getHighestWeightRankPrefix(player);
    }

}
