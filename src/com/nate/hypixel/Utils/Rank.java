package com.nate.hypixel.Utils;

import java.util.ArrayList;
import java.util.List;

public class Rank {
    private String rankName;
    private String prefix;
    private int weight;
    private List<String> permissions;

    public Rank(String rankName, String prefix, int weight, String permissionString) {
        this.rankName = rankName;
        this.prefix = prefix;
        this.weight = weight;
        this.permissions = parsePermissions(permissionString);
    }

    private List<String> parsePermissions(String permissionString) {
        List<String> permissions = new ArrayList<>();
        String[] permissionArray = permissionString.split(",");
        for (String permission : permissionArray) {
            permissions.add(permission.trim());
        }
        return permissions;
    }

    public String getRankName() {
        return rankName;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getWeight() {
        return weight;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public String getPermissionString() {
        return String.join(",", permissions);
    }
}
