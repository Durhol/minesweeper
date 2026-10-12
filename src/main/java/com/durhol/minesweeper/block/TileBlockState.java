package com.durhol.minesweeper.block;

/**
 * Visual tile state identifiers for the in-world arena grid:
 * - CLOSED (Unpressed 3D button)
 * - FLAGGED (Flag placed on tile)
 * - EMPTY (Revealed 0 adjacent)
 * - ONE .. EIGHT (Adjacent mine counts 1..8)
 * - MINE (Revealed mine)
 * - MINE_EXPLODED (Triggered mine with red background)
 */
public enum TileBlockState {
    CLOSED("tile_closed"),
    FLAGGED("tile_flag"),
    EMPTY("tile_empty"),
    ONE("tile_1"),
    TWO("tile_2"),
    THREE("tile_3"),
    FOUR("tile_4"),
    FIVE("tile_5"),
    SIX("tile_6"),
    SEVEN("tile_7"),
    EIGHT("tile_8"),
    MINE("tile_mine"),
    MINE_EXPLODED("tile_mine_exploded");

    private final String modelName;

    TileBlockState(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public static TileBlockState fromAdjacent(int count) {
        return switch (count) {
            case 1 -> ONE;
            case 2 -> TWO;
            case 3 -> THREE;
            case 4 -> FOUR;
            case 5 -> FIVE;
            case 6 -> SIX;
            case 7 -> SEVEN;
            case 8 -> EIGHT;
            default -> EMPTY;
        };
    }
}
