package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.block.formation.FootworkSealBlockEntity;
import com.echoesofthepast.block.formation.FormationCoreBlockEntity;
import com.echoesofthepast.block.qi.BaguaDistributorBlockEntity;
import com.echoesofthepast.block.qi.BronzeSpiritBellBlockEntity;
import com.echoesofthepast.block.qi.ConversionWheelBlockEntity;
import com.echoesofthepast.block.qi.JadeAbacusBlockEntity;
import com.echoesofthepast.block.qi.JadeBiReservoirBlockEntity;
import com.echoesofthepast.block.qi.MeridianThreadBlockEntity;
import com.echoesofthepast.block.qi.QiFlueBlockEntity;
import com.echoesofthepast.block.qi.QiPrismBlockEntity;
import com.echoesofthepast.block.qi.ResonanceStoneBlockEntity;
import com.echoesofthepast.block.talisman.PlacedTalismanBlockEntity;
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

    public static final RegistryObject<BlockEntityType<MeridianThreadBlockEntity>> MERIDIAN_THREAD =
        BLOCK_ENTITIES.register("meridian_thread", () -> new BlockEntityType<>(
            MeridianThreadBlockEntity::new, Set.of(EOTPBlocks.JADE_MERIDIAN_THREAD.get())));

    public static final RegistryObject<BlockEntityType<BaguaDistributorBlockEntity>> BAGUA_DISTRIBUTOR =
        BLOCK_ENTITIES.register("bagua_distributor", () -> new BlockEntityType<>(
            BaguaDistributorBlockEntity::new, Set.of(EOTPBlocks.BAGUA_DISTRIBUTOR.get())));

    public static final RegistryObject<BlockEntityType<ConversionWheelBlockEntity>> CONVERSION_WHEEL =
        BLOCK_ENTITIES.register("five_phase_conversion_wheel", () -> new BlockEntityType<>(
            ConversionWheelBlockEntity::new, Set.of(EOTPBlocks.FIVE_PHASE_CONVERSION_WHEEL.get())));

    public static final RegistryObject<BlockEntityType<QiPrismBlockEntity>> QI_PRISM =
        BLOCK_ENTITIES.register("qi_prism", () -> new BlockEntityType<>(
            QiPrismBlockEntity::new, Set.of(EOTPBlocks.QI_PRISM.get())));

    public static final RegistryObject<BlockEntityType<BronzeSpiritBellBlockEntity>> BRONZE_SPIRIT_BELL =
        BLOCK_ENTITIES.register("bronze_spirit_bell", () -> new BlockEntityType<>(
            BronzeSpiritBellBlockEntity::new, Set.of(EOTPBlocks.BRONZE_SPIRIT_BELL.get())));

    public static final RegistryObject<BlockEntityType<ResonanceStoneBlockEntity>> RESONANCE_STONE =
        BLOCK_ENTITIES.register("resonance_stone", () -> new BlockEntityType<>(
            ResonanceStoneBlockEntity::new, Set.of(EOTPBlocks.RESONANCE_STONE.get())));

    public static final RegistryObject<BlockEntityType<JadeAbacusBlockEntity>> JADE_ABACUS =
        BLOCK_ENTITIES.register("jade_abacus", () -> new BlockEntityType<>(
            JadeAbacusBlockEntity::new, Set.of(EOTPBlocks.JADE_ABACUS.get())));

    public static final RegistryObject<BlockEntityType<FormationCoreBlockEntity>> FORMATION_CORE =
        BLOCK_ENTITIES.register("formation_core", () -> new BlockEntityType<>(
            FormationCoreBlockEntity::new, Set.of(EOTPBlocks.FORMATION_CORE.get())));

    public static final RegistryObject<BlockEntityType<FootworkSealBlockEntity>> FOOTWORK_SEAL =
        BLOCK_ENTITIES.register("footwork_seal", () -> new BlockEntityType<>(
            FootworkSealBlockEntity::new, Set.of(EOTPBlocks.FOOTWORK_SEAL.get())));

    public static final RegistryObject<BlockEntityType<PlacedTalismanBlockEntity>> PLACED_TALISMAN =
        BLOCK_ENTITIES.register("placed_talisman", () -> new BlockEntityType<>(
            PlacedTalismanBlockEntity::new, Set.of(EOTPBlocks.PLACED_TALISMAN.get())));

    public static void register(BusGroup modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
