package com.durhol.minesweeper.network;

import java.io.Serializable;

/**
 * Server-to-Client packet for HUD/GUI status:
 * - Mines remaining (mines - flags)
 * - Elapsed seconds
 * - Status (PLAYING, WON, LOST)
 */
public class PacketGameStatusSync implements Serializable {
    private final int minesRemaining;
    private final long elapsedSeconds;
    private final String statusName;

    public PacketGameStatusSync(int minesRemaining, long elapsedSeconds, String statusName) {
        this.minesRemaining = minesRemaining;
        this.elapsedSeconds = elapsedSeconds;
        this.statusName = statusName;
    }

    public int getMinesRemaining() {
        return minesRemaining;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public String getStatusName() {
        return statusName;
    }
}
