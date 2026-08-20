package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.alchemy.PillQuality;
import com.echoesofthepast.ink.InkType;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.QiCharge;
import com.echoesofthepast.seal.SealRule;
import com.echoesofthepast.sword.SwordIntentData;
import com.mojang.serialization.Codec;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Item state. Everything an artifact remembers about itself lives here. */
public final class EOTPComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EchoesOfThePast.MODID);

    private EOTPComponents() {}

    /** Qi stored inside an item. */
    public static final RegistryObject<DataComponentType<QiCharge>> QI_CHARGE =
        register("qi_charge", QiCharge.CODEC);

    /** The cultivator an artifact has been spiritually bound to. */
    public static final RegistryObject<DataComponentType<UUID>> BOUND_OWNER =
        register("bound_owner", UUIDUtil.LENIENT_CODEC);

    /** What a sword has learned from being used. */
    public static final RegistryObject<DataComponentType<SwordIntentData>> SWORD_INTENT =
        register("sword_intent", SwordIntentData.CODEC);

    /** The rule carved into a seal, or stamped onto a talisman. */
    public static final RegistryObject<DataComponentType<SealRule>> SEAL_RULE =
        register("seal_rule", SealRule.CODEC);

    /**
     * The channel an imperial seal marks things with. Cranes and spirits treat everything carrying
     * the same mark as one household, which is what replaces numbered network channels.
     */
    public static final RegistryObject<DataComponentType<String>> SEAL_CHANNEL =
        register("seal_channel", Codec.STRING);

    /** Grade of a finished pill. */
    public static final RegistryObject<DataComponentType<PillQuality>> PILL_QUALITY =
        register("pill_quality", PillQuality.CODEC);

    /** What is on a brush, and how many strokes are left in it. */
    public static final RegistryObject<DataComponentType<InkType>> INK_TYPE =
        register("ink_type", InkType.CODEC);

    public static final RegistryObject<DataComponentType<Integer>> INK_STROKES =
        register("ink_strokes", Codec.INT);

    /** Phase an item has been attuned to, used by essences, banners and fans. */
    public static final RegistryObject<DataComponentType<Phase>> PHASE =
        register("phase", Phase.CODEC);

    /** The teaching written on an echo scroll, as a discovery id. */
    public static final RegistryObject<DataComponentType<String>> TEACHING =
        register("teaching", Codec.STRING);

    /** Charges left in a talisman before the paper is spent. */
    public static final RegistryObject<DataComponentType<Integer>> CHARGES =
        register("charges", Codec.INT);

    public static void register(BusGroup modBus) {
        COMPONENTS.register(modBus);
    }

    private static <T> RegistryObject<DataComponentType<T>> register(String name, Codec<T> codec) {
        return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).build());
    }
}
