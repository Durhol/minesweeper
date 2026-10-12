package com.durhol.minesweeper.game;

/**
 * Difficulty tiers for Minesweeper boards according to classic specs:
 * - TIER_1: 9x9 grid, 10 mines (Beginner)
 * - TIER_2: 16x16 grid, 40 mines (Intermediate)
 * - TIER_3: 30x16 grid, 99 mines (Expert)
 */
public enum GameTier {
    TIER_1(1, 9, 9, 10, "Tier 1 (Beginner)"),
    TIER_2(2, 16, 16, 40, "Tier 2 (Intermediate)"),
    TIER_3(3, 30, 16, 99, "Tier 3 (Expert)");

    private final int level;
    private final int width;
    private final int height;
    private final int totalMines;
    private final String displayName;

    GameTier(int level, int width, int height, int totalMines, String displayName) {
        this.level = level;
        this.width = width;
        this.height = height;
        this.totalMines = totalMines;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTotalMines() {
        return totalMines;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GameTier fromLevel(int level) {
        for (GameTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }
        return TIER_1;
    }
}
