package com.echoesofthepast;

import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPBlocks;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.registry.EOTPEntities;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.registry.EOTPMobEffects;
import com.echoesofthepast.registry.EOTPTabs;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(EchoesOfThePast.MODID)
public final class EchoesOfThePast {
    public static final String MODID = "eotp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoesOfThePast(FMLJavaModLoadingContext context) {
        BusGroup modBus = context.getModBusGroup();

        EOTPBlocks.register(modBus);
        EOTPItems.register(modBus);
        EOTPBlockEntities.register(modBus);
        EOTPEntities.register(modBus);
        EOTPMobEffects.register(modBus);
        EOTPComponents.register(modBus);
        EOTPTabs.register(modBus);

        com.echoesofthepast.event.EOTPEvents.register();

        context.registerConfig(ModConfig.Type.COMMON, EOTPConfig.SPEC);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
