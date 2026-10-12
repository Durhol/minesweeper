package com.durhol.minesweeper.event;

import com.durhol.minesweeper.arena.NeoForgeArenaManager;
import com.durhol.minesweeper.MinesweeperMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = MinesweeperMod.MOD_ID)
public class ArenaProtectionEvents {

    /**
     * In the Minesweeper dimension, continually ensure that slots 0, 1, and 8 contain the game tools.
     * If the client or engine clears any slot, this immediately restores and syncs it.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.level().dimension().equals(NeoForgeArenaManager.GAME_DIMENSION)) {
                var session = com.durhol.minesweeper.arena.CommonArenaManager.getInstance().getSession(serverPlayer.getUUID());
                boolean isLost = session != null && session.getGame().getStatus() == com.durhol.minesweeper.game.GameStatus.LOST;

                var inv = serverPlayer.getInventory();
                boolean changed = false;

                if (isLost) {
                    // Game is lost: remove shovel and flag, only leave Exit item
                    if (!inv.getItem(0).isEmpty()) {
                        inv.setItem(0, ItemStack.EMPTY);
                        changed = true;
                    }
                    if (!inv.getItem(1).isEmpty()) {
                        inv.setItem(1, ItemStack.EMPTY);
                        changed = true;
                    }
                    if (!inv.getItem(8).is(MinesweeperMod.MINESWEEPER_EXIT.get())) {
                        inv.setItem(8, new ItemStack(MinesweeperMod.MINESWEEPER_EXIT.get()));
                        changed = true;
                    }
                } else {
                    if (!inv.getItem(0).is(MinesweeperMod.MINESWEEPER_SHOVEL.get())) {
                        inv.setItem(0, new ItemStack(MinesweeperMod.MINESWEEPER_SHOVEL.get()));
                        changed = true;
                    }
                    if (!inv.getItem(1).is(MinesweeperMod.MINESWEEPER_FLAG.get())) {
                        inv.setItem(1, new ItemStack(MinesweeperMod.MINESWEEPER_FLAG.get()));
                        changed = true;
                    }
                    if (!inv.getItem(8).is(MinesweeperMod.MINESWEEPER_EXIT.get())) {
                        inv.setItem(8, new ItemStack(MinesweeperMod.MINESWEEPER_EXIT.get()));
                        changed = true;
                    }
                }

                if (changed) {
                    serverPlayer.containerMenu.broadcastChanges();
                    serverPlayer.inventoryMenu.broadcastChanges();
                }
            }
        }
    }

    /**
     * Guarantees RightClickBlock triggers shovel and flag actions even in Adventure mode or with unbreakable blocks.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().dimension().equals(NeoForgeArenaManager.GAME_DIMENSION)) {
            return;
        }

        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer && event.getHand() == InteractionHand.MAIN_HAND) {
            ItemStack held = serverPlayer.getMainHandItem();
            BlockPos pos = event.getPos();

            if (held.is(MinesweeperMod.MINESWEEPER_SHOVEL.get())) {
                NeoForgeArenaManager.handleDigAtBlock(serverPlayer, pos);
                event.setCanceled(true);
            } else if (held.is(MinesweeperMod.MINESWEEPER_FLAG.get())) {
                NeoForgeArenaManager.handleFlagAtBlock(serverPlayer, pos);
                event.setCanceled(true);
            }
        }
    }

    /**
     * Left click on a tile with Shovel also digs it (convenient LMB digging).
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().dimension().equals(NeoForgeArenaManager.GAME_DIMENSION)) {
            return;
        }

        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack held = serverPlayer.getMainHandItem();
            if (held.is(MinesweeperMod.MINESWEEPER_SHOVEL.get())) {
                NeoForgeArenaManager.handleDigAtBlock(serverPlayer, event.getPos());
                event.setCanceled(true);
            }
        }
    }

    /**
     * Prevents breaking any blocks inside the minesweeper dimension.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() != null && event.getPlayer().level().dimension().equals(NeoForgeArenaManager.GAME_DIMENSION)) {
            event.setCanceled(true);
        }
    }

    /**
     * Prevents placing blocks inside the minesweeper dimension.
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() != null && event.getEntity().level().dimension().equals(NeoForgeArenaManager.GAME_DIMENSION)) {
            event.setCanceled(true);
        }
    }

    /**
     * Prevents dropping/tossing items inside the minesweeper dimension so game tools cannot be lost.
     */
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        Player player = event.getPlayer();
        if (player != null && player.level().dimension().equals(NeoForgeArenaManager.GAME_DIMENSION)) {
            event.setCanceled(true);
            player.getInventory().add(event.getEntity().getItem());
        }
    }
}
