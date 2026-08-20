package com.echoesofthepast.item;

import com.echoesofthepast.registry.EOTPItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * Full-set effects. Individual pieces do something small and honest; wearing the whole set changes
 * how a technique behaves, which is the reason to commit to one discipline.
 */
public final class ArmorSets {
    private ArmorSets() {}

    public static boolean wearsSwordSet(Player player) {
        return wearing(player,
            EOTPItems.SWORD_CULTIVATOR_CROWN.get(),
            EOTPItems.SWORD_CULTIVATOR_ROBE.get(),
            EOTPItems.SWORD_CULTIVATOR_SKIRT.get(),
            EOTPItems.SWORD_CULTIVATOR_BOOTS.get());
    }

    public static boolean wearsAlchemistSet(Player player) {
        return wearing(player,
            EOTPItems.ALCHEMIST_HOOD.get(),
            EOTPItems.ALCHEMIST_ROBE.get(),
            EOTPItems.ALCHEMIST_TROUSERS.get(),
            EOTPItems.ALCHEMIST_SANDALS.get());
    }

    /** Sword pieces make techniques cheaper; the whole set makes them noticeably so. */
    public static float techniqueCostMultiplier(Player player) {
        int pieces = countSwordPieces(player);
        if (pieces == 0) return 1.0F;
        return wearsSwordSet(player) ? 0.7F : 1.0F - pieces * 0.05F;
    }

    private static int countSwordPieces(Player player) {
        int count = 0;
        if (isWorn(player, EquipmentSlot.HEAD, EOTPItems.SWORD_CULTIVATOR_CROWN.get())) count++;
        if (isWorn(player, EquipmentSlot.CHEST, EOTPItems.SWORD_CULTIVATOR_ROBE.get())) count++;
        if (isWorn(player, EquipmentSlot.LEGS, EOTPItems.SWORD_CULTIVATOR_SKIRT.get())) count++;
        if (isWorn(player, EquipmentSlot.FEET, EOTPItems.SWORD_CULTIVATOR_BOOTS.get())) count++;
        return count;
    }

    private static boolean wearing(Player player, Item head, Item chest, Item legs, Item feet) {
        return isWorn(player, EquipmentSlot.HEAD, head)
            && isWorn(player, EquipmentSlot.CHEST, chest)
            && isWorn(player, EquipmentSlot.LEGS, legs)
            && isWorn(player, EquipmentSlot.FEET, feet);
    }

    private static boolean isWorn(Player player, EquipmentSlot slot, Item item) {
        return player.getItemBySlot(slot).is(item);
    }
}
