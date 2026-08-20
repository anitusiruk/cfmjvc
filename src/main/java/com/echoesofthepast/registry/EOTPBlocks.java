package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.block.alchemy.DingCauldronBlock;
import com.echoesofthepast.block.craft.HerbDryingRackBlock;
import com.echoesofthepast.block.echo.AncestralTabletBlock;
import com.echoesofthepast.block.plant.EarthrootGinsengBlock;
import com.echoesofthepast.block.plant.LingzhiFungusBlock;
import com.echoesofthepast.block.plant.MoonLotusBlock;
import com.echoesofthepast.block.plant.SpiritBambooBlock;
import com.echoesofthepast.block.qi.SpiritStoneBlock;
import com.echoesofthepast.block.craft.IncenseCenserBlock;
import com.echoesofthepast.block.craft.InkstoneBlock;
import com.echoesofthepast.block.craft.SealCarvingTableBlock;
import com.echoesofthepast.block.formation.FootworkSealBlock;
import com.echoesofthepast.block.formation.FormationBannerBlock;
import com.echoesofthepast.block.formation.FormationCoreBlock;
import com.echoesofthepast.block.formation.FormationInkBlock;
import com.echoesofthepast.block.formation.FormationTileBlock;
import com.echoesofthepast.block.qi.BaguaDistributorBlock;
import com.echoesofthepast.block.qi.BronzeSpiritBellBlock;
import com.echoesofthepast.block.qi.ConversionWheelBlock;
import com.echoesofthepast.block.qi.JadeAbacusBlock;
import com.echoesofthepast.block.qi.JadeBiReservoirBlock;
import com.echoesofthepast.block.qi.MeridianThreadBlock;
import com.echoesofthepast.block.qi.QiFlueBlock;
import com.echoesofthepast.block.qi.QiFlueBlockEntity;
import com.echoesofthepast.block.qi.QiPrismBlock;
import com.echoesofthepast.block.qi.ResonanceStoneBlock;
import com.echoesofthepast.block.talisman.PlacedTalismanBlock;
import com.echoesofthepast.formation.FormationPart;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EOTPBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EchoesOfThePast.MODID);

    /** Every block that should show up in the creative tab, in registration order. */
    private static final List<RegistryObject<Item>> TAB_ITEMS = new ArrayList<>();

    private EOTPBlocks() {}

    // ----------------------------------------------------------------------------------- materials

    private static BlockBehaviour.Properties jade() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_GREEN).strength(1.8F, 4.0F).sound(SoundType.CALCITE).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties bamboo() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.6F).sound(SoundType.BAMBOO).noOcclusion();
    }

    private static BlockBehaviour.Properties bronze() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.5F, 6.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties stoneware() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_CYAN).strength(1.6F, 3.0F).sound(SoundType.DECORATED_POT).requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties lacquer() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.6F, 2.5F).sound(SoundType.WOOD).ignitedByLava();
    }

    private static BlockBehaviour.Properties formationTile() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.2F, 2.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties paper() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(0.3F).sound(SoundType.WOOL).noOcclusion();
    }

    // ------------------------------------------------------------------------------- Qi plumbing

    public static final RegistryObject<Block> JADE_BI_RESERVOIR =
        register("jade_bi_reservoir", JadeBiReservoirBlock::new, () -> jade().noOcclusion().lightLevel(state -> 4));

    public static final RegistryObject<Block> BAMBOO_QI_FLUE =
        register("bamboo_qi_flue", props -> new QiFlueBlock(props, QiFlueBlockEntity.Material.BAMBOO), EOTPBlocks::bamboo);

    public static final RegistryObject<Block> HOLLOW_BAMBOO_QI_FLUE =
        register("hollow_bamboo_qi_flue", props -> new QiFlueBlock(props, QiFlueBlockEntity.Material.HOLLOW), EOTPBlocks::bamboo);

    public static final RegistryObject<Block> JADE_FLUE_JOINT =
        register("jade_flue_joint", props -> new QiFlueBlock(props, QiFlueBlockEntity.Material.JADE_JOINT), () -> jade().noOcclusion());

    public static final RegistryObject<Block> JADE_MERIDIAN_THREAD =
        register("jade_meridian_thread", MeridianThreadBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.2F).sound(SoundType.WOOL).noOcclusion().noCollision());

    public static final RegistryObject<Block> BAGUA_DISTRIBUTOR =
        register("bagua_distributor", BaguaDistributorBlock::new, EOTPBlocks::bronze);

    public static final RegistryObject<Block> FIVE_PHASE_CONVERSION_WHEEL =
        register("five_phase_conversion_wheel", ConversionWheelBlock::new, () -> bronze().noOcclusion());

    public static final RegistryObject<Block> QI_PRISM =
        register("qi_prism", QiPrismBlock::new, () -> jade().noOcclusion().lightLevel(state -> 6));

    public static final RegistryObject<Block> BRONZE_SPIRIT_BELL =
        register("bronze_spirit_bell", BronzeSpiritBellBlock::new, () -> bronze().noOcclusion());

    public static final RegistryObject<Block> RESONANCE_STONE =
        register("resonance_stone", ResonanceStoneBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.4F, 3.0F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops());

    public static final RegistryObject<Block> JADE_ABACUS =
        register("jade_abacus", JadeAbacusBlock::new, () -> jade().noOcclusion());

    // ------------------------------------------------------------------------------- formations

    public static final RegistryObject<Block> FORMATION_NODE_TILE =
        register("formation_node_tile", props -> new FormationTileBlock(props, FormationPart.NODE), EOTPBlocks::formationTile);

    public static final RegistryObject<Block> FORMATION_LINE_TILE =
        register("formation_line_tile", props -> new FormationTileBlock(props, FormationPart.LINE), EOTPBlocks::formationTile);

    public static final RegistryObject<Block> FORMATION_ARC_TILE =
        register("formation_arc_tile", props -> new FormationTileBlock(props, FormationPart.ARC), EOTPBlocks::formationTile);

    public static final RegistryObject<Block> FORMATION_TRIGRAM_TILE =
        register("formation_trigram_tile", props -> new FormationTileBlock(props, FormationPart.TRIGRAM), EOTPBlocks::formationTile);

    /** Drawn by a loaded brush rather than placed, so it has no item. */
    public static final RegistryObject<Block> FORMATION_INK =
        registerNoItem("formation_ink", FormationInkBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.05F).sound(SoundType.WOOL).noCollision().noOcclusion().instabreak());

    public static final RegistryObject<Block> FORMATION_BANNER =
        register("formation_banner", FormationBannerBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.5F).sound(SoundType.WOOL).noCollision().noOcclusion());

    public static final RegistryObject<Block> FORMATION_CORE =
        register("formation_core", FormationCoreBlock::new, () -> jade().noOcclusion().lightLevel(state -> 3));

    public static final RegistryObject<Block> FOOTWORK_SEAL =
        register("footwork_seal", FootworkSealBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.4F).sound(SoundType.STONE).noCollision().noOcclusion());

    /** Placed by pressing a talisman onto a surface; peeling it off returns the paper. */
    public static final RegistryObject<Block> PLACED_TALISMAN =
        registerNoItem("placed_talisman", PlacedTalismanBlock::new, EOTPBlocks::paper);

    // -------------------------------------------------------------------------- craft and alchemy

    public static final RegistryObject<Block> DING_CAULDRON =
        register("ding_cauldron", DingCauldronBlock::new, () -> bronze().noOcclusion());

    public static final RegistryObject<Block> INKSTONE =
        register("inkstone", InkstoneBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.4F, 3.0F).sound(SoundType.STONE).noOcclusion());

    public static final RegistryObject<Block> SEAL_CARVING_TABLE =
        register("seal_carving_table", SealCarvingTableBlock::new, () -> lacquer().noOcclusion());

    public static final RegistryObject<Block> INCENSE_CENSER =
        register("incense_censer", IncenseCenserBlock::new, () -> bronze().noOcclusion().lightLevel(state -> state.getValue(IncenseCenserBlock.LIT) ? 5 : 0));

    public static final RegistryObject<Block> HERB_DRYING_RACK =
        register("herb_drying_rack", HerbDryingRackBlock::new, () -> bamboo().noOcclusion());

    // -------------------------------------------------------------------------------- the echoes

    public static final RegistryObject<Block> ANCESTRAL_TABLET =
        register("ancestral_tablet", AncestralTabletBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0F, 4.0F)
                .sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(AncestralTabletBlock.AWAKENED) ? 7 : 0));

    // ---------------------------------------------------------------------------- growing things

    public static final RegistryObject<Block> SPIRIT_BAMBOO =
        register("spirit_bamboo", SpiritBambooBlock::new,
            () -> bamboo().randomTicks().noCollision().instabreak());

    public static final RegistryObject<Block> HOLLOW_SPIRIT_BAMBOO =
        register("hollow_spirit_bamboo", SpiritBambooBlock::new,
            () -> bamboo().noCollision().instabreak());

    public static final RegistryObject<Block> MOON_LOTUS =
        register("moon_lotus", MoonLotusBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.2F)
                .sound(SoundType.WET_GRASS).noOcclusion().noCollision()
                .lightLevel(state -> state.getValue(MoonLotusBlock.OPEN) ? 6 : 0));

    public static final RegistryObject<Block> EARTHROOT_GINSENG =
        register("earthroot_ginseng", EarthrootGinsengBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2F)
                .sound(SoundType.CROP).randomTicks().noOcclusion().noCollision());

    public static final RegistryObject<Block> LINGZHI_SPIRIT_FUNGUS =
        register("lingzhi_spirit_fungus", LingzhiFungusBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.3F)
                .sound(SoundType.FUNGUS).noOcclusion().noCollision());

    // ------------------------------------------------------------------------------ spirit stone

    public static final RegistryObject<Block> SPIRIT_STONE_ORE =
        register("spirit_stone_ore", props -> new net.minecraft.world.level.block.Block(props),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 3.0F).requiresCorrectToolForDrops());

    public static final RegistryObject<Block> DEEPSLATE_SPIRIT_STONE_ORE =
        register("deepslate_spirit_stone_ore", props -> new net.minecraft.world.level.block.Block(props),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F)
                .sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());

    public static final RegistryObject<Block> LOW_SPIRIT_STONE_BLOCK = spiritStoneBlock("low_spirit_stone_block", 4);
    public static final RegistryObject<Block> MIDDLE_SPIRIT_STONE_BLOCK = spiritStoneBlock("middle_spirit_stone_block", 8);
    public static final RegistryObject<Block> HIGH_SPIRIT_STONE_BLOCK = spiritStoneBlock("high_spirit_stone_block", 12);

    /**
     * A stored block of spirit stone. It is a genuine reservoir, and the brightness in its
     * blockstate falls as it is drained, so a wall of these visibly dims when the workshop is
     * running hard.
     */
    private static RegistryObject<Block> spiritStoneBlock(String name, int light) {
        return register(name, props -> new SpiritStoneBlock(props, light),
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                .strength(2.5F, 4.0F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(SpiritStoneBlock.CHARGE) * light / 4));
    }

    // ---------------------------------------------------------------------------------- plumbing

    public static void register(BusGroup modBus) {
        EOTPDecorative.init();
        BLOCKS.register(modBus);
    }

    public static List<RegistryObject<Item>> tabItems() {
        return TAB_ITEMS;
    }

    /** Registers a block plus the item that places it. */
    public static RegistryObject<Block> register(
        String name,
        Function<BlockBehaviour.Properties, Block> factory,
        Supplier<BlockBehaviour.Properties> properties
    ) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> factory.apply(properties.get().setId(BLOCKS.key(name))));
        TAB_ITEMS.add(EOTPItems.ITEMS.register(name, () -> new BlockItem(
            block.get(),
            new Item.Properties().setId(EOTPItems.ITEMS.key(name)).useBlockDescriptionPrefix()
        )));
        return block;
    }

    /** Registers a block that has no item, such as a fluid or a drawn line placed by a tool. */
    public static RegistryObject<Block> registerNoItem(
        String name,
        Function<BlockBehaviour.Properties, Block> factory,
        Supplier<BlockBehaviour.Properties> properties
    ) {
        return BLOCKS.register(name, () -> factory.apply(properties.get().setId(BLOCKS.key(name))));
    }
}
