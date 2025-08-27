package com.example.avoidgrapefruit.interactions;

import java.util.HashMap;
import java.util.Map;

public class RegionMapper {

    private static final Map<String, String> REGION_MAP = new HashMap<>();

    static {
        REGION_MAP.put("United States", "USA");
        REGION_MAP.put("United States of America", "USA");
        REGION_MAP.put("US", "USA");

        // UK
        REGION_MAP.put("United Kingdom", "UK");
        REGION_MAP.put("Scotland", "UK");
        REGION_MAP.put("Wales", "UK");
        REGION_MAP.put("England", "UK");
        REGION_MAP.put("Ireland", "UK");
        REGION_MAP.put("Northern Ireland", "UK");

        // Mediterranean
        REGION_MAP.put("Greece", "Mediterranean");
        REGION_MAP.put("Albania", "Mediterranean");
        REGION_MAP.put("Croatia", "Mediterranean");
        REGION_MAP.put("Italy", "Mediterranean");
        REGION_MAP.put("Spain", "Mediterranean");

        // Middle East
        REGION_MAP.put("Iran", "Middle East");
        REGION_MAP.put("Iraq", "Middle East");
        REGION_MAP.put("Lebanon", "Middle East");
        REGION_MAP.put("Saudi Arabia", "Middle East");
        REGION_MAP.put("Jordan", "Middle East");
    }

    /**
     * Resolves a user region to either itself or its mapped group.
     * @param userRegion The region provided by the user (e.g., "United States")
     * @return The resolved region (e.g., "USA" or original if no mapping)
     */
    public static String resolve(String userRegion) {
        if (userRegion == null || userRegion.trim().isEmpty()) {
            return "";
        }
        userRegion = userRegion.trim();
        if (REGION_MAP.containsKey(userRegion)) {
            return REGION_MAP.get(userRegion);
        }
        return userRegion;
    }
    public static String mapRegion(String userRegion) {
        if (userRegion == null) return null;
        return REGION_MAP.getOrDefault(userRegion, userRegion);
    }
}
