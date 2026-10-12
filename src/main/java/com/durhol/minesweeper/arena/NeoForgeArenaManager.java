package com.durhol.minesweeper.arena;

import com.durhol.minesweeper.MinesweeperMod;
import com.durhol.minesweeper.game.GameTier;
import com.durhol.minesweeper.game.MinesweeperGame;
import com.durhol.minesweeper.game.GameStatus;
import com.durhol.minesweeper.game.Cell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side NeoForge arena manager handling the full lifecycle:
 * 1. Stash player inventory & record home coordinates
 * 2. Teleport player into minesweeper:game_world at isolated arena coordinates
 * 3. Build the physical indestructible arena using retro Windows Minesweeper style blocks
 * 4. Equip temporary tools (Shovel, Flag, Exit)
 * 5. On victory/defeat/forfeit: restore inventory, teleport home, grant rewards
 */
public class NeoForgeArenaManager {

    public static final ResourceKey<Level> GAME_DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("minesweeper", "game_world")
    );

    /** Arena platform Y level inside the void dimension */
    private static final int ARENA_Y = 64;

    /** Saved inventory stacks per player UUID */
    private static final Map<UUID, List<ItemStack>> savedInventories = new ConcurrentHashMap<>();

    /** Saved original gamemode per player UUID */
    private static final Map<UUID, GameType> savedGameModes = new ConcurrentHashMap<>();

    /** Saved used device item per player UUID */
    private static final Map<UUID, ItemStack> activeDevices = new ConcurrentHashMap<>();

    // ─── ENTER GAME ───────────────────────────────────────────────

    /**
     * Called when a player right-clicks a Minesweeper Device.
     * The item is already consumed from the player's active hand.
     */
    public static void enterGame(ServerPlayer player, GameTier tier, ItemStack usedDevice) {
        UUID playerId = player.getUUID();
        if (usedDevice != null && !usedDevice.isEmpty()) {
            activeDevices.put(playerId, usedDevice);
        }
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel gameLevel = server.getLevel(GAME_DIMENSION);
        if (gameLevel == null) {
            player.displayClientMessage(Component.literal("§cMinesweeper dimension not found!"), true);
            MinesweeperMod.LOGGER.error("Dimension minesweeper:game_world is not loaded!");
            return;
        }

        // 1. Save return coordinates and gamemode
        PlayerStash stash = new PlayerStash(
                playerId,
                player.level().dimension().location().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot()
        );
        CommonArenaManager.getInstance().stashPlayer(playerId, stash);
        savedGameModes.put(playerId, player.gameMode.getGameModeForPlayer());

        // 2. Save and clear inventory
        stashInventory(player);

        // 3. Allocate arena session
        CommonArenaManager.ArenaSession session = CommonArenaManager.getInstance().createSession(playerId, tier);
        ArenaPos origin = session.getOrigin();

        // 4. Build the arena platform + retro closed tile grid
        buildArena(gameLevel, origin, tier);

        // 5. Teleport player to the center of the arena
        int centerX = origin.getX() + tier.getWidth() / 2;
        int centerZ = origin.getZ() + tier.getHeight() / 2;
        BlockPos spawnPos = new BlockPos(centerX, ARENA_Y + 2, centerZ);

        DimensionTransition.PostDimensionTransition onTeleport = (teleportedPlayer) -> {
            if (teleportedPlayer instanceof ServerPlayer sp) {
                sp.setGameMode(GameType.ADVENTURE);
                equipGameTools(sp);
                gameLevel.playSound(null, spawnPos, com.durhol.minesweeper.sound.ModSounds.START.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                sp.displayClientMessage(
                        Component.literal("§a§lMinesweeper " + tier.getDisplayName() + " started! §r§7Use Shovel to dig, Flag to mark."),
                        false
                );
            }
        };

        player.changeDimension(new DimensionTransition(
                gameLevel,
                new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET).then(onTeleport)
        ));

        MinesweeperMod.LOGGER.info("Player {} entered {} at Arena ({}, {})",
                player.getName().getString(), tier.getDisplayName(), origin.getX(), origin.getZ());
    }

    // ─── BUILD ARENA ──────────────────────────────────────────────

    private static void buildArena(ServerLevel level, ArenaPos origin, GameTier tier) {
        int w = tier.getWidth();
        int h = tier.getHeight();
        int ox = origin.getX();
        int oz = origin.getZ();

        // Build a Bedrock platform under the grid so it's completely indestructible
        for (int x = ox - 2; x <= ox + w + 1; x++) {
            for (int z = oz - 2; z <= oz + h + 1; z++) {
                level.setBlockAndUpdate(new BlockPos(x, ARENA_Y, z), Blocks.BEDROCK.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(x, ARENA_Y - 1, z), Blocks.BEDROCK.defaultBlockState());
            }
        }

        // Place authentic Retro Windows Minesweeper Closed Tiles
        BlockState closedState = MinesweeperMod.TILE_CLOSED.get().defaultBlockState();
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < h; z++) {
                BlockPos tilePos = new BlockPos(ox + x, ARENA_Y + 1, oz + z);
                level.setBlockAndUpdate(tilePos, closedState);
            }
        }

        // Border walls (Bedrock with Tinted Glass above)
        for (int x = ox - 1; x <= ox + w; x++) {
            level.setBlockAndUpdate(new BlockPos(x, ARENA_Y + 1, oz - 1), Blocks.BEDROCK.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x, ARENA_Y + 1, oz + h), Blocks.BEDROCK.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x, ARENA_Y + 2, oz - 1), Blocks.TINTED_GLASS.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x, ARENA_Y + 2, oz + h), Blocks.TINTED_GLASS.defaultBlockState());
        }
        for (int z = oz - 1; z <= oz + h; z++) {
            level.setBlockAndUpdate(new BlockPos(ox - 1, ARENA_Y + 1, z), Blocks.BEDROCK.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(ox + w, ARENA_Y + 1, z), Blocks.BEDROCK.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(ox - 1, ARENA_Y + 2, z), Blocks.TINTED_GLASS.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(ox + w, ARENA_Y + 2, z), Blocks.TINTED_GLASS.defaultBlockState());
        }

        // Glowstone lighting around border at Y+3
        for (int x = ox - 1; x <= ox + w; x += 3) {
            level.setBlockAndUpdate(new BlockPos(x, ARENA_Y + 3, oz - 1), Blocks.GLOWSTONE.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x, ARENA_Y + 3, oz + h), Blocks.GLOWSTONE.defaultBlockState());
        }
    }

    // ─── INVENTORY MANAGEMENT ─────────────────────────────────────

    private static void stashInventory(ServerPlayer player) {
        Inventory inv = player.getInventory();
        List<ItemStack> saved = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            saved.add(inv.getItem(i).copy());
        }
        savedInventories.put(player.getUUID(), saved);
        inv.clearContent();
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }

    private static void restoreInventory(ServerPlayer player) {
        List<ItemStack> saved = savedInventories.remove(player.getUUID());
        if (saved == null) return;
        Inventory inv = player.getInventory();
        inv.clearContent();
        for (int i = 0; i < saved.size() && i < inv.getContainerSize(); i++) {
            inv.setItem(i, saved.get(i));
        }
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }

    private static void equipGameTools(ServerPlayer player) {
        Inventory inv = player.getInventory();
        inv.clearContent();
        inv.setItem(0, new ItemStack(MinesweeperMod.MINESWEEPER_SHOVEL.get()));
        inv.setItem(1, new ItemStack(MinesweeperMod.MINESWEEPER_FLAG.get()));
        inv.setItem(8, new ItemStack(MinesweeperMod.MINESWEEPER_EXIT.get()));
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }

    // ─── DIG / FLAG ACTIONS ───────────────────────────────────────

    /**
     * Called when player uses the Shovel on a block in the arena.
     * Converts world block position to grid coordinates and performs dig.
     */
    public static void handleDigAtBlock(ServerPlayer player, BlockPos blockPos) {
        UUID playerId = player.getUUID();
        CommonArenaManager.ArenaSession session = CommonArenaManager.getInstance().getSession(playerId);
        if (session == null) return;

        ArenaPos origin = session.getOrigin();
        int gridX = blockPos.getX() - origin.getX();
        int gridZ = blockPos.getZ() - origin.getZ();

        MinesweeperGame game = session.getGame();
        if (!game.isInBounds(gridX, gridZ)) return;

        List<Cell> changed = game.dig(gridX, gridZ);
        MinesweeperMod.LOGGER.info("Player {} dug at ({}, {}), changed cells count: {}",
                player.getName().getString(), gridX, gridZ, changed.size());
        if (changed.isEmpty()) return;

        ServerLevel level = (ServerLevel) player.level();

        // Sound design: custom retro click sound when opening tiles
        level.playSound(null, blockPos, com.durhol.minesweeper.sound.ModSounds.CLICK.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

        // Update blocks in world to reflect revealed state with retro textures
        for (Cell c : changed) {
            BlockPos tilePos = new BlockPos(origin.getX() + c.getX(), ARENA_Y + 1, origin.getZ() + c.getZ());
            BlockState newState;
            if (c.isMine()) {
                if (c.getX() == gridX && c.getZ() == gridZ) {
                    newState = MinesweeperMod.TILE_MINE_EXPLODED.get().defaultBlockState();
                } else {
                    newState = MinesweeperMod.TILE_MINE.get().defaultBlockState();
                }
            } else {
                newState = getBlockForNumber(c.getAdjacentMines());
            }

            level.setBlock(tilePos, newState, net.minecraft.world.level.block.Block.UPDATE_ALL | net.minecraft.world.level.block.Block.UPDATE_IMMEDIATE);
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(tilePos, newState));
        }

        // Display mine counter in action bar
        player.displayClientMessage(Component.literal(
                "§7Mines: §c" + game.getRemainingMineDisplayCount() +
                " §7| Flags: §6" + game.getFlagCount() + "/" + game.getTotalMines() +
                " §7| Time: §e" + game.getElapsedTimeSeconds() + "s"
        ), true);

        // Check game end
        if (game.getStatus() == GameStatus.WON) {
            handleVictory(player);
        } else if (game.getStatus() == GameStatus.LOST) {
            handleDefeat(player, blockPos);
        }
    }

    /**
     * Called when player uses the Flag on a block in the arena.
     */
    public static void handleFlagAtBlock(ServerPlayer player, BlockPos blockPos) {
        UUID playerId = player.getUUID();
        CommonArenaManager.ArenaSession session = CommonArenaManager.getInstance().getSession(playerId);
        if (session == null) return;

        ArenaPos origin = session.getOrigin();
        int gridX = blockPos.getX() - origin.getX();
        int gridZ = blockPos.getZ() - origin.getZ();

        MinesweeperGame game = session.getGame();
        if (!game.isInBounds(gridX, gridZ)) return;

        Cell cellBefore = game.getCell(gridX, gridZ);
        if (cellBefore != null && !cellBefore.isFlagged() && game.getFlagCount() >= game.getTotalMines()) {
            player.displayClientMessage(Component.literal("§cMaximum flags reached! (" + game.getTotalMines() + ")"), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.7f, 1.2f);
            return;
        }

        boolean toggled = game.toggleFlag(gridX, gridZ);
        if (toggled) {
            Cell c = game.getCell(gridX, gridZ);
            BlockPos tilePos = new BlockPos(origin.getX() + gridX, ARENA_Y + 1, origin.getZ() + gridZ);
            ServerLevel level = (ServerLevel) player.level();

            BlockState newState;
            if (c.isFlagged()) {
                newState = MinesweeperMod.TILE_FLAG.get().defaultBlockState();
                level.playSound(null, tilePos, com.durhol.minesweeper.sound.ModSounds.FLAG.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                newState = MinesweeperMod.TILE_CLOSED.get().defaultBlockState();
                level.playSound(null, tilePos, com.durhol.minesweeper.sound.ModSounds.FLAG.get(), SoundSource.BLOCKS, 1.0f, 0.8f);
            }
            level.setBlock(tilePos, newState, net.minecraft.world.level.block.Block.UPDATE_ALL | net.minecraft.world.level.block.Block.UPDATE_IMMEDIATE);
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(tilePos, newState));

            player.displayClientMessage(Component.literal(
                    "§7Mines: §c" + game.getRemainingMineDisplayCount() +
                    " §7| Flags: §6" + game.getFlagCount() + "/" + game.getTotalMines()
            ), true);
        }
    }

    // ─── GAME END ─────────────────────────────────────────────────

    public static void handleVictory(ServerPlayer player) {
        UUID playerId = player.getUUID();
        CommonArenaManager.ArenaSession session = CommonArenaManager.getInstance().removeSession(playerId);
        if (session == null) return;

        GameTier tier = session.getGame().getTier();

        // Determine reward
        ItemStack reward = switch (tier) {
            case TIER_1 -> new ItemStack(Items.DIAMOND, 1);
            case TIER_2 -> new ItemStack(Items.DIAMOND, 5);
            case TIER_3 -> new ItemStack(Items.NETHERITE_INGOT, 2);
        };

        // Custom victory sound & message
        player.level().playSound(null, player.blockPosition(),
                com.durhol.minesweeper.sound.ModSounds.WIN.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        player.displayClientMessage(Component.literal(
                "§a§l★ YOU WIN! ★ §r§7Cleared " + tier.getDisplayName() + " in " +
                session.getGame().getElapsedTimeSeconds() + "s!"
        ), false);

        player.displayClientMessage(Component.literal("§6+ " + reward.getCount() + "x " + reward.getItem().getDescription().getString()), false);

        // Handle used device durability: deduct 1 durability, return if not broken
        ItemStack device = activeDevices.remove(playerId);
        ItemStack returnedDevice = null;
        if (device != null && !device.isEmpty()) {
            int newDamage = device.getDamageValue() + 1;
            int maxDamage = device.getMaxDamage();
            if (newDamage < maxDamage) {
                device.setDamageValue(newDamage);
                returnedDevice = device;
                int remaining = maxDamage - newDamage;
                player.displayClientMessage(Component.literal("§aDevice saved! §eDurability: §f" + remaining + "/" + maxDamage), false);
            } else {
                player.displayClientMessage(Component.literal("§cYour Minesweeper Device has broken from wear!"), false);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        // Teleport home with reward and restore inventory post-teleport
        teleportHome(player, playerId, reward, returnedDevice);
    }

    public static void handleDefeat(ServerPlayer player, BlockPos explodedPos) {
        UUID playerId = player.getUUID();
        CommonArenaManager.ArenaSession session = CommonArenaManager.getInstance().getSession(playerId);
        if (session == null) return;

        ServerLevel level = (ServerLevel) player.level();

        // 1. Play custom defeat sound
        level.playSound(null, player.blockPosition(),
                com.durhol.minesweeper.sound.ModSounds.LOSE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        // 2. Beautiful explosion animation: particles on the exploded mine and smoke across all mines
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                explodedPos.getX() + 0.5, explodedPos.getY() + 1.0, explodedPos.getZ() + 0.5,
                1, 0, 0, 0, 0);

        ArenaPos origin = session.getOrigin();
        MinesweeperGame game = session.getGame();
        for (int x = 0; x < game.getWidth(); x++) {
            for (int z = 0; z < game.getHeight(); z++) {
                Cell cell = game.getCell(x, z);
                if (cell != null && cell.isMine()) {
                    BlockPos p = new BlockPos(origin.getX() + x, ARENA_Y + 1, origin.getZ() + z);
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                            p.getX() + 0.5, p.getY() + 1.1, p.getZ() + 0.5,
                            4, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }

        // 3. Device is destroyed upon defeat
        ItemStack lostDevice = activeDevices.remove(playerId);
        if (lostDevice != null && !lostDevice.isEmpty()) {
            player.displayClientMessage(Component.literal("§c💥 Your Minesweeper Device was completely destroyed in the explosion!"), false);
            level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
        }

        // 4. Clear shovel and flag, only leave Exit item so player can inspect the field and leave when ready
        Inventory inv = player.getInventory();
        inv.clearContent();
        inv.setItem(8, new ItemStack(MinesweeperMod.MINESWEEPER_EXIT.get()));
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();

        player.displayClientMessage(Component.literal("§c§l💥 BOOM! §r§7You hit a mine! All mines revealed. Use §e[Forfeit & Exit]§7 to return."), false);
    }

    public static void handleForfeit(ServerPlayer player) {
        UUID playerId = player.getUUID();
        CommonArenaManager.ArenaSession session = CommonArenaManager.getInstance().removeSession(playerId);
        if (session == null) return;

        ItemStack lostDevice = activeDevices.remove(playerId);
        if (lostDevice != null && !lostDevice.isEmpty()) {
            player.displayClientMessage(Component.literal("§cYour Minesweeper Device was destroyed by forfeit!"), false);
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
        }

        player.displayClientMessage(Component.translatable("message.minesweeper.forfeit"), false);

        teleportHome(player, playerId, null, null);
    }

    private static void teleportHome(ServerPlayer player, UUID playerId, ItemStack victoryReward, ItemStack returnedDevice) {
        PlayerStash stash = CommonArenaManager.getInstance().retrieveStash(playerId);
        GameType originalGameMode = savedGameModes.remove(playerId);

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel targetLevel;
        Vec3 targetPos;
        float targetYaw = 0;
        float targetPitch = 0;

        if (stash != null) {
            ResourceKey<Level> homeDimKey = ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.parse(stash.getDimensionKey())
            );
            targetLevel = server.getLevel(homeDimKey);
            if (targetLevel == null) targetLevel = server.getLevel(Level.OVERWORLD);
            targetPos = new Vec3(stash.getPosX(), stash.getPosY(), stash.getPosZ());
            targetYaw = stash.getYaw();
            targetPitch = stash.getPitch();
        } else {
            targetLevel = server.getLevel(Level.OVERWORLD);
            if (targetLevel == null) return;
            BlockPos spawn = targetLevel.getSharedSpawnPos();
            targetPos = new Vec3(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        }

        DimensionTransition.PostDimensionTransition onHome = (homePlayer) -> {
            if (homePlayer instanceof ServerPlayer sp) {
                if (originalGameMode != null) {
                    sp.setGameMode(originalGameMode);
                }
                restoreInventory(sp);
                if (victoryReward != null && !victoryReward.isEmpty()) {
                    giveOrDrop(sp, victoryReward.copy());
                }
                if (returnedDevice != null && !returnedDevice.isEmpty()) {
                    giveOrDrop(sp, returnedDevice.copy());
                }
                sp.containerMenu.broadcastChanges();
                sp.inventoryMenu.broadcastChanges();
            }
        };

        player.changeDimension(new DimensionTransition(
                targetLevel,
                targetPos,
                Vec3.ZERO,
                targetYaw,
                targetPitch,
                DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET).then(onHome)
        ));
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    // ─── RETRO BLOCK MAPPING ──────────────────────────────────────

    /**
     * Maps adjacent mine count (0-8) to the authentic retro Windows Minesweeper block
     */
    private static BlockState getBlockForNumber(int adjacent) {
        return switch (adjacent) {
            case 0 -> MinesweeperMod.TILE_EMPTY.get().defaultBlockState();
            case 1 -> MinesweeperMod.TILE_1.get().defaultBlockState();
            case 2 -> MinesweeperMod.TILE_2.get().defaultBlockState();
            case 3 -> MinesweeperMod.TILE_3.get().defaultBlockState();
            case 4 -> MinesweeperMod.TILE_4.get().defaultBlockState();
            case 5 -> MinesweeperMod.TILE_5.get().defaultBlockState();
            case 6 -> MinesweeperMod.TILE_6.get().defaultBlockState();
            case 7 -> MinesweeperMod.TILE_7.get().defaultBlockState();
            case 8 -> MinesweeperMod.TILE_8.get().defaultBlockState();
            default -> MinesweeperMod.TILE_EMPTY.get().defaultBlockState();
        };
    }
}
