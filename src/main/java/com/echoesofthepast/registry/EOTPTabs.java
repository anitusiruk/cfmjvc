package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class EOTPTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EchoesOfThePast.MODID);

    private EOTPTabs() {}

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.eotp.main"))
        .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
        .icon(() -> EOTPItems.HIGH_SPIRIT_STONE.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            EOTPItems.tabItems().forEach(item -> output.accept(item.get()));
            EOTPBlocks.tabItems().forEach(item -> output.accept(item.get()));
        })
        .build());

    public static void register(BusGroup modBus) {
        TABS.register(modBus);
    }
}
