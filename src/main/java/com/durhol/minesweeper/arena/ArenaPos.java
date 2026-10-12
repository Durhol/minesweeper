package com.durhol.minesweeper.arena;

import java.util.Objects;

/**
 * Representation of a 2D integer coordinate (Arena block origin).
 */
public final class ArenaPos {
    private final int x;
    private final int z;

    public ArenaPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArenaPos arenaPos = (ArenaPos) o;
        return x == arenaPos.x && z == arenaPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "ArenaPos{x=" + x + ", z=" + z + "}";
    }
}
