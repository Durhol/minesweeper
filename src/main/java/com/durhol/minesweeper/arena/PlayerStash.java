package com.durhol.minesweeper.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Portable representation of player state before entering the Minesweeper pocket dimension.
 * Stores original coordinates, dimension ID, and serialized inventory item stacks.
 */
public class PlayerStash {
    private final UUID playerUUID;
    private final String dimensionKey;
    private final double posX;
    private final double posY;
    private final double posZ;
    private final float yaw;
    private final float pitch;
    private final List<Object> inventoryItems = new ArrayList<>();

    public PlayerStash(UUID playerUUID, String dimensionKey, double posX, double posY, double posZ, float yaw, float pitch) {
        this.playerUUID = playerUUID;
        this.dimensionKey = dimensionKey;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getDimensionKey() {
        return dimensionKey;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public List<Object> getInventoryItems() {
        return inventoryItems;
    }
}
