package com.durhol.minesweeper.game;

import java.util.*;

/**
 * Pure Java implementation of Minesweeper logic.
 * Features:
 * - Guaranteed safe first click (first cell and its 8 neighbors are always clear of mines)
 * - Recursive Flood Fill to reveal empty areas when clicking 0-adjacent cells
 * - Flag mechanics: flagged tiles cannot be opened; flag count cannot exceed total mines
 * - Win condition detection: all non-mine cells are revealed
 * - Loss condition detection: hitting a mine
 */
public class MinesweeperGame {
    private final GameTier tier;
    private final int width;
    private final int height;
    private final int totalMines;
    private final Cell[][] grid;

    private boolean minesGenerated = false;
    private GameStatus status = GameStatus.NOT_STARTED;
    private long startTimeMs = 0;
    private long endTimeMs = 0;

    public MinesweeperGame(GameTier tier) {
        this.tier = tier;
        this.width = tier.getWidth();
        this.height = tier.getHeight();
        this.totalMines = tier.getTotalMines();
        this.grid = new Cell[width][height];

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                grid[x][z] = new Cell(x, z);
            }
        }
    }

    public GameTier getTier() {
        return tier;
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

    public GameStatus getStatus() {
        return status;
    }

    public Cell getCell(int x, int z) {
        if (!isInBounds(x, z)) {
            return null;
        }
        return grid[x][z];
    }

    public boolean isInBounds(int x, int z) {
        return x >= 0 && x < width && z >= 0 && z < height;
    }

    /**
     * Toggles flag on cell (x, z). Flagged cells cannot be opened.
     * Prevents placing more flags than total mines (cannot go negative).
     * Returns true if toggled successfully, false if already revealed, at max flags, or game is over.
     */
    public boolean toggleFlag(int x, int z) {
        if (status == GameStatus.WON || status == GameStatus.LOST) {
            return false;
        }
        if (!isInBounds(x, z)) {
            return false;
        }

        Cell cell = grid[x][z];
        if (cell.isRevealed()) {
            return false;
        }

        // If not flagged yet, check if we reached maximum allowed flags
        if (!cell.isFlagged()) {
            if (getFlagCount() >= totalMines) {
                return false; // Reached maximum flag limit
            }
        }

        cell.toggleFlag();
        return true;
    }

    /**
     * Digs / opens cell at (x, z).
     * @return List of cells that changed state during this move.
     */
    public List<Cell> dig(int x, int z) {
        if (status == GameStatus.WON || status == GameStatus.LOST) {
            return Collections.emptyList();
        }
        if (!isInBounds(x, z)) {
            return Collections.emptyList();
        }

        Cell target = grid[x][z];
        if (target.isRevealed() || target.isFlagged()) {
            return Collections.emptyList();
        }

        // Guaranteed safe first click
        if (!minesGenerated) {
            generateMines(x, z);
            minesGenerated = true;
            status = GameStatus.IN_PROGRESS;
            startTimeMs = System.currentTimeMillis();
        }

        List<Cell> changedCells = new ArrayList<>();

        // If player hit a mine
        if (target.isMine()) {
            target.setRevealed(true);
            changedCells.add(target);
            status = GameStatus.LOST;
            endTimeMs = System.currentTimeMillis();

            // Reveal all other mines upon loss
            for (int ix = 0; ix < width; ix++) {
                for (int iz = 0; iz < height; iz++) {
                    Cell c = grid[ix][iz];
                    if (c.isMine() && !c.isRevealed()) {
                        c.setRevealed(true);
                        changedCells.add(c);
                    }
                }
            }
            return changedCells;
        }

        // Flood fill reveal
        floodFill(x, z, changedCells);

        // Check if all non-mine cells are revealed
        if (checkWinCondition()) {
            status = GameStatus.WON;
            endTimeMs = System.currentTimeMillis();
        }

        return changedCells;
    }

    /**
     * Mines are generated strictly AFTER first click.
     * Cell (safeX, safeZ) and all its 8 adjacent neighbors are guaranteed not to be mines.
     */
    private void generateMines(int safeX, int safeZ) {
        List<Cell> candidates = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                // Ensure (safeX, safeZ) and all 8 neighbors are excluded
                if (Math.abs(x - safeX) <= 1 && Math.abs(z - safeZ) <= 1) {
                    continue;
                }
                candidates.add(grid[x][z]);
            }
        }

        Collections.shuffle(candidates, new Random());

        int minesToPlace = Math.min(totalMines, candidates.size());
        for (int i = 0; i < minesToPlace; i++) {
            candidates.get(i).setMine(true);
        }

        // Calculate adjacent mine count for every cell
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                grid[x][z].setAdjacentMines(countAdjacentMines(x, z));
            }
        }
    }

    private int countAdjacentMines(int cx, int cz) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                int nx = cx + dx;
                int nz = cz + dz;
                if (isInBounds(nx, nz) && grid[nx][nz].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Breadth-First / Depth-First Flood fill reveal for zero-adjacent tiles.
     */
    private void floodFill(int startX, int startZ, List<Cell> changedCells) {
        Queue<Cell> queue = new LinkedList<>();
        Cell start = grid[startX][startZ];
        start.setRevealed(true);
        changedCells.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            // If current cell has 0 adjacent mines, reveal neighbors
            if (current.getAdjacentMines() == 0) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        int nx = current.getX() + dx;
                        int nz = current.getZ() + dz;

                        if (isInBounds(nx, nz)) {
                            Cell neighbor = grid[nx][nz];
                            if (!neighbor.isRevealed() && !neighbor.isFlagged() && !neighbor.isMine()) {
                                neighbor.setRevealed(true);
                                changedCells.add(neighbor);
                                if (neighbor.getAdjacentMines() == 0) {
                                    queue.add(neighbor);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Win check: Every non-mine cell must be revealed.
     */
    public boolean checkWinCondition() {
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                Cell cell = grid[x][z];
                if (!cell.isMine() && !cell.isRevealed()) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getFlagCount() {
        int count = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < height; z++) {
                if (grid[x][z].isFlagged()) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getRemainingMineDisplayCount() {
        return Math.max(0, totalMines - getFlagCount());
    }

    public long getElapsedTimeSeconds() {
        if (startTimeMs == 0) {
            return 0;
        }
        long end = (endTimeMs > 0) ? endTimeMs : System.currentTimeMillis();
        return (end - startTimeMs) / 1000;
    }
}
