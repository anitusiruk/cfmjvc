package com.echoesofthepast.seal;

import com.echoesofthepast.registry.EOTPItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * What a seal is cut from. Softer materials are cheap and wear out; jade holds an edge, and a jade
 * seal is what a serious workshop is built around.
 */
public enum SealMaterial {
    WOOD(8, 1.0F),
    STONE(20, 1.05F),
    BRONZE(48, 1.15F),
    JADE(128, 1.35F);

    private final int stampings;
    private final float clarity;

    SealMaterial(int stampings, float clarity) {
        this.stampings = stampings;
        this.clarity = clarity;
    }

    /** How many talismans this seal can stamp before the face wears smooth. */
    public int stampings() {
        return this.stampings;
    }

    /** Multiplier on the strength of what it stamps. A crisp seal makes a stronger talisman. */
    public float clarity() {
        return this.clarity;
    }

    public static @Nullable SealMaterial of(ItemStack stack) {
        if (stack.is(EOTPItems.RAW_JADE.get())) return JADE;
        if (stack.is(Items.COPPER_INGOT) || stack.is(Items.GOLD_INGOT)) return BRONZE;
        if (stack.is(Items.STONE) || stack.is(Items.SMOOTH_STONE) || stack.is(Items.DEEPSLATE)) return STONE;
        if (stack.is(Items.OAK_PLANKS) || stack.is(EOTPItems.HOLLOW_BAMBOO.get())) return WOOD;
        return null;
    }
}
