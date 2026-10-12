package com.durhol.minesweeper.network;

import java.io.Serializable;

/**
 * Server-to-Client packet broadcasting a single or batch tile update to the client.
 */
public class PacketTileUpdate implements Serializable {
    private final int gridX;
    private final int gridZ;
    private final String stateName;

    public PacketTileUpdate(int gridX, int gridZ, String stateName) {
        this.gridX = gridX;
        this.gridZ = gridZ;
        this.stateName = stateName;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridZ() {
        return gridZ;
    }

    public String getStateName() {
        return stateName;
    }
}
