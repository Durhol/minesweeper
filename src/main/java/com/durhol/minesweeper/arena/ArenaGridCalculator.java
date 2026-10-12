package com.durhol.minesweeper.arena;

/**
 * Pure Java arena offset calculation.
 * Distributes player arenas on an isolated 2D grid:
 * Arena 0 -> (0, 0)
 * Arena 1 -> (10000, 0)
 * Arena 2 -> (20000, 0)
 * ...
 * Wrapped in columns/rows so arenas never collide or leak into each other.
 */
public class ArenaGridCalculator {
    public static final int ARENA_SPACING = 10000;
    public static final int GRID_ROW_SIZE = 1000;

    /**
     * Calculates the root world coordinates for a given session index.
     * @param sessionIndex monotonic unique ID of the game arena session.
     * @return ArenaPos containing X and Z offsets in blocks.
     */
    public static ArenaPos calculateOffset(long sessionIndex) {
        long gridX = sessionIndex % GRID_ROW_SIZE;
        long gridZ = sessionIndex / GRID_ROW_SIZE;

        int blockX = (int) (gridX * ARENA_SPACING);
        int blockZ = (int) (gridZ * ARENA_SPACING);

        return new ArenaPos(blockX, blockZ);
    }
}
