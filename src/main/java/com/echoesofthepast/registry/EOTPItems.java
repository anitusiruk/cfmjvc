package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EOTPItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EchoesOfThePast.MODID);

    private static final List<RegistryObject<Item>> TAB_ITEMS = new ArrayList<>();

    private EOTPItems() {}

    // ------------------------------------------------------------------------------ spirit stones

    public static final RegistryObject<Item> RAW_SPIRIT_STONE = simple("raw_spirit_stone");
    public static final RegistryObject<Item> LOW_SPIRIT_STONE = simple("low_spirit_stone");
    public static final RegistryObject<Item> MIDDLE_SPIRIT_STONE = simple("middle_spirit_stone");
    public static final RegistryObject<Item> HIGH_SPIRIT_STONE = simple("high_spirit_stone");
    public static final RegistryObject<Item> SPIRIT_STONE_POWDER = simple("spirit_stone_powder");

    // ------------------------------------------------------------------------------------ essences

    public static final RegistryObject<Item> ECHO_ESSENCE = simple("echo_essence");
    public static final RegistryObject<Item> WOOD_ESSENCE = simple("wood_essence");
    public static final RegistryObject<Item> FIRE_ESSENCE = simple("fire_essence");
    public static final RegistryObject<Item> EARTH_ESSENCE = simple("earth_essence");
    public static final RegistryObject<Item> METAL_ESSENCE = simple("metal_essence");
    public static final RegistryObject<Item> WATER_ESSENCE = simple("water_essence");

    // ----------------------------------------------------------------------------------- materials

    public static final RegistryObject<Item> JADE_DUST = simple("jade_dust");
    public static final RegistryObject<Item> RAW_JADE = simple("raw_jade");
    public static final RegistryObject<Item> CINNABAR_PIGMENT = simple("cinnabar_pigment");
    public static final RegistryObject<Item> SPIRIT_SILK = simple("spirit_silk");
    public static final RegistryObject<Item> MERIDIAN_THREAD_SPOOL = simple("meridian_thread_spool");
    public static final RegistryObject<Item> TALISMAN_PAPER = simple("talisman_paper");
    public static final RegistryObject<Item> HOLLOW_BAMBOO = simple("hollow_bamboo");
    public static final RegistryObject<Item> SPIRIT_BAMBOO_SHOOT = simple("spirit_bamboo_shoot");

    // ---------------------------------------------------------------------------------- plumbing

    public static void register(BusGroup modBus) {
        ITEMS.register(modBus);
    }

    public static List<RegistryObject<Item>> tabItems() {
        return TAB_ITEMS;
    }

    public static RegistryObject<Item> simple(String name) {
        return register(name, Item::new);
    }

    public static RegistryObject<Item> simple(String name, int stackSize) {
        return register(name, properties -> new Item(properties.stacksTo(stackSize)));
    }

    public static RegistryObject<Item> register(String name, Function<Item.Properties, Item> factory) {
        RegistryObject<Item> item = ITEMS.register(name, () -> factory.apply(new Item.Properties().setId(ITEMS.key(name))));
        TAB_ITEMS.add(item);
        return item;
    }
}
