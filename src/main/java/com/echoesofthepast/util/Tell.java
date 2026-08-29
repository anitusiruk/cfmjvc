package com.echoesofthepast.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * The mod has no GUIs and no HUD. Everything a device wants to tell you either happens in particles
 * or arrives as a short line above the hotbar, which keeps the readout in the world.
 */
public final class Tell {
    private Tell() {}

    /** A brief line over the hotbar; the usual way a device answers a question. */
    public static void overlay(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message, true);
        }
    }

    /** Chat, for things worth keeping: discoveries, breakthroughs, echoes read from a mirror. */
    public static void chat(Player player, Component message) {
        player.sendSystemMessage(message);
    }

    public static void overlay(Player player, String key, Object... args) {
        overlay(player, Component.translatable(key, args));
    }

    public static void chat(Player player, String key, Object... args) {
        chat(player, Component.translatable(key, args));
    }
}
