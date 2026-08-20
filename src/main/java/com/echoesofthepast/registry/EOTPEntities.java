package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class EOTPEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EchoesOfThePast.MODID);

    private EOTPEntities() {}

    public static void register(BusGroup modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
