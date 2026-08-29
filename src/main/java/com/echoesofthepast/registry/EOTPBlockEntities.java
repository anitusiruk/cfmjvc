package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.block.alchemy.DingCauldronBlockEntity;
import com.echoesofthepast.block.craft.HerbDryingRackBlockEntity;
import com.echoesofthepast.block.craft.IncenseCenserBlockEntity;
import com.echoesofthepast.block.craft.InkstoneBlockEntity;
import com.echoesofthepast.block.craft.SealCarvingTableBlockEntity;
import com.echoesofthepast.block.echo.AncestralTabletBlockEntity;
import com.echoesofthepast.block.landscape.LandscapeSteleBlockEntity;
import com.echoesofthepast.block.plant.LingzhiFungusBlockEntity;
import com.echoesofthepast.block.plant.MoonLotusBlockEntity;
import com.echoesofthepast.block.qi.SpiritStoneBlockEntity;
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

    public static final RegistryObject<BlockEntityType<DingCauldronBlockEntity>> DING_CAULDRON =
        BLOCK_ENTITIES.register("ding_cauldron", () -> new BlockEntityType<>(
            DingCauldronBlockEntity::new, Set.of(EOTPBlocks.DING_CAULDRON.get())));

    public static final RegistryObject<BlockEntityType<InkstoneBlockEntity>> INKSTONE =
        BLOCK_ENTITIES.register("inkstone", () -> new BlockEntityType<>(
            InkstoneBlockEntity::new, Set.of(EOTPBlocks.INKSTONE.get())));

    public static final RegistryObject<BlockEntityType<SealCarvingTableBlockEntity>> SEAL_CARVING_TABLE =
        BLOCK_ENTITIES.register("seal_carving_table", () -> new BlockEntityType<>(
            SealCarvingTableBlockEntity::new, Set.of(EOTPBlocks.SEAL_CARVING_TABLE.get())));

    public static final RegistryObject<BlockEntityType<IncenseCenserBlockEntity>> INCENSE_CENSER =
        BLOCK_ENTITIES.register("incense_censer", () -> new BlockEntityType<>(
            IncenseCenserBlockEntity::new, Set.of(EOTPBlocks.INCENSE_CENSER.get())));

    public static final RegistryObject<BlockEntityType<HerbDryingRackBlockEntity>> HERB_DRYING_RACK =
        BLOCK_ENTITIES.register("herb_drying_rack", () -> new BlockEntityType<>(
            HerbDryingRackBlockEntity::new, Set.of(EOTPBlocks.HERB_DRYING_RACK.get())));

    public static final RegistryObject<BlockEntityType<AncestralTabletBlockEntity>> ANCESTRAL_TABLET =
        BLOCK_ENTITIES.register("ancestral_tablet", () -> new BlockEntityType<>(
            AncestralTabletBlockEntity::new, Set.of(EOTPBlocks.ANCESTRAL_TABLET.get())));

    public static final RegistryObject<BlockEntityType<MoonLotusBlockEntity>> MOON_LOTUS =
        BLOCK_ENTITIES.register("moon_lotus", () -> new BlockEntityType<>(
            MoonLotusBlockEntity::new, Set.of(EOTPBlocks.MOON_LOTUS.get())));

    public static final RegistryObject<BlockEntityType<LingzhiFungusBlockEntity>> LINGZHI_FUNGUS =
        BLOCK_ENTITIES.register("lingzhi_spirit_fungus", () -> new BlockEntityType<>(
            LingzhiFungusBlockEntity::new, Set.of(EOTPBlocks.LINGZHI_SPIRIT_FUNGUS.get())));

    public static final RegistryObject<BlockEntityType<SpiritStoneBlockEntity>> SPIRIT_STONE_BLOCK =
        BLOCK_ENTITIES.register("spirit_stone_block", () -> new BlockEntityType<>(
            SpiritStoneBlockEntity::new, Set.of(
                EOTPBlocks.LOW_SPIRIT_STONE_BLOCK.get(),
                EOTPBlocks.MIDDLE_SPIRIT_STONE_BLOCK.get(),
                EOTPBlocks.HIGH_SPIRIT_STONE_BLOCK.get())));

    public static final RegistryObject<BlockEntityType<LandscapeSteleBlockEntity>> LANDSCAPE_STELE =
        BLOCK_ENTITIES.register("landscape_stele", () -> new BlockEntityType<>(
            LandscapeSteleBlockEntity::new, Set.of(EOTPBlocks.LANDSCAPE_STELE.get())));

    public static void register(BusGroup modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
