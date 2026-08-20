package com.echoesofthepast.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Qi-powered footwear. The shoes themselves hold no logic; what they do is grant midair steps, and
 * that is handled in {@link com.echoesofthepast.event.MovementEvents} because it has to watch the
 * whole arc of a jump rather than a single interaction.
 */
public class CloudstepShoesItem extends Item {
    /** Steps a pair of shoes is good for per airborne period. */
    private final int steps;

    public CloudstepShoesItem(ArmorMaterial material, Properties properties, int steps) {
        super(properties.humanoidArmor(material, ArmorType.BOOTS));
        this.steps = steps;
    }

    public int steps() {
        return this.steps;
    }
}
