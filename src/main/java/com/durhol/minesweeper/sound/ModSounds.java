package com.durhol.minesweeper.sound;

import com.durhol.minesweeper.MinesweeperMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MinesweeperMod.MOD_ID);

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(MinesweeperMod.MOD_ID, name)
        ));
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> CLICK = registerSound("click");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLAG = registerSound("flag");
    public static final DeferredHolder<SoundEvent, SoundEvent> START = registerSound("start");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIN = registerSound("win");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOSE = registerSound("lose");

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
