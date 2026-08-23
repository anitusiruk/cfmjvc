package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.alchemy.PillKind;
import com.echoesofthepast.aura.IncenseKind;
import com.echoesofthepast.item.CarvedSealItem;
import com.echoesofthepast.item.CultivationPlantItem;
import com.echoesofthepast.item.DragonVeinCompassItem;
import com.echoesofthepast.item.EchoMirrorItem;
import com.echoesofthepast.item.EchoScrollItem;
import com.echoesofthepast.item.JadeImperialSealItem;
import com.echoesofthepast.item.PaperCraneItem;
import com.echoesofthepast.item.CloudstepShoesItem;
import com.echoesofthepast.item.EOTPArmorMaterials;
import com.echoesofthepast.item.FlyingSwordItem;
import com.echoesofthepast.item.IncenseStickItem;
import com.echoesofthepast.item.PillItem;
import com.echoesofthepast.item.SpiritBrushItem;
import com.echoesofthepast.item.TalismanItem;
import com.echoesofthepast.talisman.TalismanType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.echoesofthepast.qi.Phase;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jspecify.annotations.Nullable;

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
    public static final RegistryObject<Item> SPIRIT_BAMBOO_SHOOT = register(
        "spirit_bamboo_shoot",
        properties -> new CultivationPlantItem(properties, () -> EOTPBlocks.SPIRIT_BAMBOO.get())
    );

    // -------------------------------------------------------------------------- plants and spoils

    public static final RegistryObject<Item> MOON_LOTUS_PETAL = simple("moon_lotus_petal");
    public static final RegistryObject<Item> GINSENG_ROOT = register(
        "ginseng_root",
        properties -> new CultivationPlantItem(properties, () -> EOTPBlocks.EARTHROOT_GINSENG.get())
    );
    public static final RegistryObject<Item> LINGZHI_CAP = simple("lingzhi_cap");
    public static final RegistryObject<Item> PURIFIED_LINGZHI = simple("purified_lingzhi");
    public static final RegistryObject<Item> PILL_RESIDUE = simple("pill_residue");
    public static final RegistryObject<Item> SPIRIT_SPRING_BUCKET = register("spirit_spring_bucket",
        properties -> new BucketItem(EOTPFluids.SPIRIT_SPRING.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));

    // ------------------------------------------------------------------------------------- tools

    public static final RegistryObject<Item> SPIRIT_BRUSH = register("spirit_brush", properties -> new SpiritBrushItem(properties.stacksTo(1)));
    public static final RegistryObject<Item> DRAGON_VEIN_COMPASS = register("dragon_vein_compass", properties -> new DragonVeinCompassItem(properties.stacksTo(1)));
    public static final RegistryObject<Item> ECHO_MIRROR = register("echo_mirror", properties -> new EchoMirrorItem(properties.stacksTo(1)));
    public static final RegistryObject<Item> JADE_IMPERIAL_SEAL = register("jade_imperial_seal", properties -> new JadeImperialSealItem(properties.stacksTo(1)));
    public static final RegistryObject<Item> ECHO_SCROLL = register("echo_scroll", properties -> new EchoScrollItem(properties.stacksTo(8)));
    public static final RegistryObject<Item> PAPER_CRANE = register("paper_crane", properties -> new PaperCraneItem(properties.stacksTo(16)));
    public static final RegistryObject<Item> CARVED_SEAL = register("carved_seal", properties -> new CarvedSealItem(properties.stacksTo(1)));

    // ---------------------------------------------------------------------------------- incense

    private static final Map<IncenseKind, RegistryObject<Item>> INCENSE = new EnumMap<>(IncenseKind.class);

    static {
        for (IncenseKind kind : IncenseKind.VALUES) {
            INCENSE.put(kind, register(kind.itemName(), properties -> new IncenseStickItem(properties.stacksTo(16), kind)));
        }
    }

    public static RegistryObject<Item> incense(IncenseKind kind) {
        return INCENSE.get(kind);
    }

    // ----------------------------------------------------------------------- artifacts and armour

    public static final RegistryObject<Item> FLYING_SWORD = register("flying_sword",
        properties -> new FlyingSwordItem(properties.stacksTo(1).sword(ToolMaterial.DIAMOND, 3.0F, -2.4F)));

    public static final RegistryObject<Item> CLOUDSTEP_SHOES = register("cloudstep_shoes",
        properties -> new CloudstepShoesItem(EOTPArmorMaterials.JADE_SILK, properties.stacksTo(1), 2));

    public static final RegistryObject<Item> SWORD_CULTIVATOR_CROWN = armour("sword_cultivator_crown", EOTPArmorMaterials.JADE_SILK, ArmorType.HELMET);
    public static final RegistryObject<Item> SWORD_CULTIVATOR_ROBE = armour("sword_cultivator_robe", EOTPArmorMaterials.JADE_SILK, ArmorType.CHESTPLATE);
    public static final RegistryObject<Item> SWORD_CULTIVATOR_SKIRT = armour("sword_cultivator_skirt", EOTPArmorMaterials.JADE_SILK, ArmorType.LEGGINGS);
    public static final RegistryObject<Item> SWORD_CULTIVATOR_BOOTS = armour("sword_cultivator_boots", EOTPArmorMaterials.JADE_SILK, ArmorType.BOOTS);

    public static final RegistryObject<Item> ALCHEMIST_HOOD = armour("alchemist_hood", EOTPArmorMaterials.ROBE, ArmorType.HELMET);
    public static final RegistryObject<Item> ALCHEMIST_ROBE = armour("alchemist_robe", EOTPArmorMaterials.ROBE, ArmorType.CHESTPLATE);
    public static final RegistryObject<Item> ALCHEMIST_TROUSERS = armour("alchemist_trousers", EOTPArmorMaterials.ROBE, ArmorType.LEGGINGS);
    public static final RegistryObject<Item> ALCHEMIST_SANDALS = armour("alchemist_sandals", EOTPArmorMaterials.ROBE, ArmorType.BOOTS);

    private static RegistryObject<Item> armour(String name, ArmorMaterial material, ArmorType type) {
        return register(name, properties -> new Item(properties.humanoidArmor(material, type)));
    }

    // ------------------------------------------------------------------------------------- pills

    private static final Map<PillKind, RegistryObject<Item>> PILLS = new EnumMap<>(PillKind.class);

    static {
        for (PillKind kind : PillKind.VALUES) {
            PILLS.put(kind, register(kind.itemName(), properties -> new PillItem(properties.stacksTo(16), kind)));
        }
    }

    public static RegistryObject<Item> pill(PillKind kind) {
        return PILLS.get(kind);
    }

    // ---------------------------------------------------------------------------------- talismans

    private static final Map<TalismanType, RegistryObject<Item>> TALISMANS = new EnumMap<>(TalismanType.class);

    static {
        for (TalismanType type : TalismanType.VALUES) {
            TALISMANS.put(type, register(type.itemName(), properties -> new TalismanItem(properties.stacksTo(16), type)));
        }
    }

    public static RegistryObject<Item> talisman(TalismanType type) {
        return TALISMANS.get(type);
    }

    /** The phase an essence item carries, or null if the stack is not an essence. */
    public static @Nullable Phase essencePhase(ItemStack stack) {
        if (stack.is(WOOD_ESSENCE.get())) return Phase.WOOD;
        if (stack.is(FIRE_ESSENCE.get())) return Phase.FIRE;
        if (stack.is(EARTH_ESSENCE.get())) return Phase.EARTH;
        if (stack.is(METAL_ESSENCE.get())) return Phase.METAL;
        if (stack.is(WATER_ESSENCE.get())) return Phase.WATER;
        return null;
    }

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
