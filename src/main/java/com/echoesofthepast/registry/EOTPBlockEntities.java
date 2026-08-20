package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.block.qi.JadeBiReservoirBlockEntity;
import com.echoesofthepast.block.qi.QiFlueBlockEntity;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EOTPBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, EchoesOfThePast.MODID);

    private EOTPBlockEntities() {}

    public static final RegistryObject<BlockEntityType<JadeBiReservoirBlockEntity>> JADE_BI_RESERVOIR =
        BLOCK_ENTITIES.register("jade_bi_reservoir", () -> new BlockEntityType<>(
            JadeBiReservoirBlockEntity::new, Set.of(EOTPBlocks.JADE_BI_RESERVOIR.get())));

    public static final RegistryObject<BlockEntityType<QiFlueBlockEntity>> QI_FLUE =
        BLOCK_ENTITIES.register("qi_flue", () -> new BlockEntityType<>(
            QiFlueBlockEntity::new, Set.of(
                EOTPBlocks.BAMBOO_QI_FLUE.get(),
                EOTPBlocks.HOLLOW_BAMBOO_QI_FLUE.get(),
                EOTPBlocks.JADE_FLUE_JOINT.get())));

    public static void register(BusGroup modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
