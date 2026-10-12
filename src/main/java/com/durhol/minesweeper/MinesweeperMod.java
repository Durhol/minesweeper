package com.durhol.minesweeper;

import com.durhol.minesweeper.arena.NeoForgeArenaManager;
import com.durhol.minesweeper.game.GameTier;
import com.durhol.minesweeper.item.ModItemNames;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MinesweeperMod.MOD_ID)
public class MinesweeperMod {
    public static final String MOD_ID = "minesweeper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // ── Tile Blocks (Indestructible Retro Windows Style) ───────────
    private static BlockBehaviour.Properties tileProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(-1.0f, 3600000.0f) // Indestructible like bedrock
                .sound(SoundType.STONE);
    }

    public static final DeferredBlock<Block> TILE_CLOSED = BLOCKS.register("tile_closed", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_EMPTY = BLOCKS.register("tile_empty", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_FLAG = BLOCKS.register("tile_flag", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_MINE = BLOCKS.register("tile_mine", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_MINE_EXPLODED = BLOCKS.register("tile_mine_exploded", () -> new Block(tileProps()));

    public static final DeferredBlock<Block> TILE_1 = BLOCKS.register("tile_1", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_2 = BLOCKS.register("tile_2", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_3 = BLOCKS.register("tile_3", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_4 = BLOCKS.register("tile_4", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_5 = BLOCKS.register("tile_5", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_6 = BLOCKS.register("tile_6", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_7 = BLOCKS.register("tile_7", () -> new Block(tileProps()));
    public static final DeferredBlock<Block> TILE_8 = BLOCKS.register("tile_8", () -> new Block(tileProps()));

    // ── Tier Device Items ─────────────────────────────────────────
    public static final DeferredItem<Item> TIER_1_DEVICE = ITEMS.register(ModItemNames.TIER_1_DEVICE,
            () -> new DeviceItem(GameTier.TIER_1, new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TIER_2_DEVICE = ITEMS.register(ModItemNames.TIER_2_DEVICE,
            () -> new DeviceItem(GameTier.TIER_2, new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TIER_3_DEVICE = ITEMS.register(ModItemNames.TIER_3_DEVICE,
            () -> new DeviceItem(GameTier.TIER_3, new Item.Properties().stacksTo(1)));

    // ── Temporary Game Tools ──────────────────────────────────────
    public static final DeferredItem<Item> MINESWEEPER_SHOVEL = ITEMS.register(ModItemNames.SHOVEL,
            () -> new ShovelItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MINESWEEPER_FLAG = ITEMS.register(ModItemNames.FLAG,
            () -> new FlagItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MINESWEEPER_EXIT = ITEMS.register(ModItemNames.EXIT,
            () -> new ExitItem(new Item.Properties().stacksTo(1)));

    // ── Creative Tab ──────────────────────────────────────────────
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.minesweeper"))
                    .icon(() -> new ItemStack(TIER_1_DEVICE.get()))
                    .displayItems((params, output) -> {
                        output.accept(TIER_1_DEVICE.get());
                        output.accept(TIER_2_DEVICE.get());
                        output.accept(TIER_3_DEVICE.get());
                        output.accept(MINESWEEPER_SHOVEL.get());
                        output.accept(MINESWEEPER_FLAG.get());
                        output.accept(MINESWEEPER_EXIT.get());
                    })
                    .build());

    public MinesweeperMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Retro Minesweeper Mod by Durhol!");
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        com.durhol.minesweeper.sound.ModSounds.register(modEventBus);
    }

    // ══════════════════════════════════════════════════════════════
    // DEVICE ITEM — Right-click to enter the Minesweeper dimension
    // ══════════════════════════════════════════════════════════════
    public static class DeviceItem extends Item {
        private final GameTier tier;

        public DeviceItem(GameTier tier, Properties properties) {
            super(properties);
            this.tier = tier;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                stack.shrink(1);
                player.setItemInHand(hand, stack);
                NeoForgeArenaManager.enterGame(serverPlayer, tier);
                return InteractionResultHolder.consume(stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // SHOVEL ITEM — Right-click a tile block to dig / reveal it
    // ══════════════════════════════════════════════════════════════
    public static class ShovelItem extends Item {
        public ShovelItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Player player = context.getPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                BlockPos pos = context.getClickedPos();
                NeoForgeArenaManager.handleDigAtBlock(serverPlayer, pos);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // FLAG ITEM — Right-click a tile block to toggle flag
    // ══════════════════════════════════════════════════════════════
    public static class FlagItem extends Item {
        public FlagItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Player player = context.getPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                BlockPos pos = context.getClickedPos();
                NeoForgeArenaManager.handleFlagAtBlock(serverPlayer, pos);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // EXIT ITEM — Right-click to forfeit and return home
    // ══════════════════════════════════════════════════════════════
    public static class ExitItem extends Item {
        public ExitItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NeoForgeArenaManager.handleForfeit(serverPlayer);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }
}
