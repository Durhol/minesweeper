package com.durhol.minesweeper.game;

/**
 * Represents a single cell on the Minesweeper board.
 */
public class Cell {
    private final int x;
    private final int z;
    private boolean mine;
    private boolean revealed;
    private boolean flagged;
    private int adjacentMines;

    public Cell(int x, int z) {
        this.x = x;
        this.z = z;
        this.mine = false;
        this.revealed = false;
        this.flagged = false;
        this.adjacentMines = 0;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public void toggleFlag() {
        if (!revealed) {
            this.flagged = !this.flagged;
        }
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }
}
