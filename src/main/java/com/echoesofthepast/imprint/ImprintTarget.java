package com.echoesofthepast.imprint;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * A device that will accept being operated by the translucent hands of an ancestral tablet.
 *
 * <p>Devices opt in explicitly. That is what keeps imprinting safe: a tablet can only ever do things
 * a device has agreed can be done to it, so nothing has to pretend to be a player.
 */
public interface ImprintTarget {
    /**
     * @param offered an item taken from a container next to the tablet, or empty
     * @return true if the remembered gesture accomplished anything
     */
    boolean acceptImprint(ServerLevel level, ImprintAction action, ItemStack offered);
}
