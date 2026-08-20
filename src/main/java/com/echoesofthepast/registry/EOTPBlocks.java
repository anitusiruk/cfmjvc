package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.block.qi.JadeBiReservoirBlock;
import com.echoesofthepast.block.qi.QiFlueBlock;
import com.echoesofthepast.block.qi.QiFlueBlockEntity;
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

    // ---------------------------------------------------------------------------------- plumbing

    public static void register(BusGroup modBus) {
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
