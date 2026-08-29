package com.echoesofthepast.alchemy;

import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.registry.EOTPItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Pill recipes are held in code rather than in datapacks because the interesting part is not the
 * ingredient list - it is the <em>order</em> they go in, the heat window they want, and the phase
 * relationship the cauldron has to hold while they cook.
 *
 * @param kind        which pill comes out
 * @param sequence    ingredients in the order they must be added
 * @param minHeat     lowest workable temperature
 * @param maxHeat     highest workable temperature
 * @param wantedBlend the phase relationship the vessel should hold
 * @param yield       how many pills a clean, well run batch produces
 */
public record AlchemyRecipe(
    PillKind kind,
    List<Item> sequence,
    int minHeat,
    int maxHeat,
    PhaseBlend wantedBlend,
    int yield
) {
    private static final List<AlchemyRecipe> RECIPES = new ArrayList<>();

    public static List<AlchemyRecipe> all() {
        if (RECIPES.isEmpty()) {
            build();
        }
        return RECIPES;
    }

    private static void build() {
        add(PillKind.QI_RECOVERY, 300, 620, 2,
            EOTPItems.LOW_SPIRIT_STONE.get(), EOTPItems.MOON_LOTUS_PETAL.get(), EOTPItems.SPIRIT_STONE_POWDER.get());

        add(PillKind.MERIDIAN_OPENING, 480, 760, 1,
            EOTPItems.GINSENG_ROOT.get(), EOTPItems.MERIDIAN_THREAD_SPOOL.get(), EOTPItems.MIDDLE_SPIRIT_STONE.get());

        add(PillKind.CLEAR_HEART, 220, 480, 2,
            EOTPItems.MOON_LOTUS_PETAL.get(), EOTPItems.PURIFIED_LINGZHI.get(), EOTPItems.WATER_ESSENCE.get());

        add(PillKind.BODY_TEMPERING, 620, 900, 1,
            EOTPItems.GINSENG_ROOT.get(), EOTPItems.EARTH_ESSENCE.get(), EOTPItems.METAL_ESSENCE.get(), EOTPItems.JADE_DUST.get());

        add(PillKind.CLOUDSTEP, 260, 520, 2,
            EOTPItems.SPIRIT_BAMBOO_SHOOT.get(), EOTPItems.MOON_LOTUS_PETAL.get(), EOTPItems.SPIRIT_SILK.get());

        add(PillKind.FIVE_PHASE_HARMONY, 400, 700, 1,
            EOTPItems.WOOD_ESSENCE.get(), EOTPItems.FIRE_ESSENCE.get(), EOTPItems.EARTH_ESSENCE.get(),
            EOTPItems.METAL_ESSENCE.get(), EOTPItems.WATER_ESSENCE.get());

        add(PillKind.BREAKTHROUGH_STABILISING, 540, 820, 1,
            EOTPItems.HIGH_SPIRIT_STONE.get(), EOTPItems.GINSENG_ROOT.get(), EOTPItems.ECHO_ESSENCE.get(), EOTPItems.JADE_DUST.get());
    }

    private static void add(PillKind kind, int minHeat, int maxHeat, int yield, Item... sequence) {
        RECIPES.add(new AlchemyRecipe(kind, List.of(sequence), minHeat, maxHeat, kind.recipeBlend(), yield));
    }

    /** True while the ingredients so far are still on the way to this recipe. */
    public boolean matchesPrefix(List<Item> added) {
        if (added.size() > this.sequence.size()) return false;
        for (int index = 0; index < added.size(); index++) {
            if (!added.get(index).equals(this.sequence.get(index))) return false;
        }
        return true;
    }

    public boolean isComplete(List<Item> added) {
        return added.size() == this.sequence.size() && this.matchesPrefix(added);
    }

    /** The next thing this recipe wants, which is what an alchemist's robe lets you see. */
    public @Nullable Item nextIngredient(List<Item> added) {
        return added.size() < this.sequence.size() ? this.sequence.get(added.size()) : null;
    }

    public int idealHeat() {
        return (this.minHeat + this.maxHeat) / 2;
    }

    /** How well a temperature suits this recipe, from 0 to 1. */
    public float heatScore(int heat) {
        if (heat < this.minHeat || heat > this.maxHeat) {
            int distance = heat < this.minHeat ? this.minHeat - heat : heat - this.maxHeat;
            return Math.max(0.0F, 1.0F - distance / 240.0F) * 0.4F;
        }
        int span = Math.max(1, (this.maxHeat - this.minHeat) / 2);
        int offset = Math.abs(heat - this.idealHeat());
        return 1.0F - (offset / (float) span) * 0.35F;
    }

    public ItemStack result(PillQuality quality) {
        ItemStack stack = new ItemStack(EOTPItems.pill(this.kind).get(), this.yield);
        stack.set(com.echoesofthepast.registry.EOTPComponents.PILL_QUALITY.get(), quality);
        return stack;
    }

    /** Finds the recipes still reachable from what has been thrown in so far. */
    public static List<AlchemyRecipe> candidates(List<Item> added) {
        List<AlchemyRecipe> matches = new ArrayList<>();
        for (AlchemyRecipe recipe : all()) {
            if (recipe.matchesPrefix(added)) matches.add(recipe);
        }
        return matches;
    }
}
