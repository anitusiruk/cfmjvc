package com.echoesofthepast.client;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.registry.EOTPEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client setup. Every entity is registered with vanilla's {@link NoopRenderer} on purpose: the mod
 * ships no models or textures, and a no-op renderer means the entities exist, behave and can be
 * interacted with while remaining invisible except for the particles they trail.
 *
 * <p>To give one of them a body, replace its {@code NoopRenderer} here with a renderer for your
 * Blockbench model. Nothing else needs to change.
 *
 * <p>The class is only loaded on the client, so nothing here can break a dedicated server.
 */
@Mod.EventBusSubscriber(modid = EchoesOfThePast.MODID, value = Dist.CLIENT)
public final class EOTPClient {
    private EOTPClient() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EOTPEntities.HEART_DEMON.get(), NoopRenderer::new);
        event.registerEntityRenderer(EOTPEntities.FLYING_SWORD.get(), NoopRenderer::new);
        event.registerEntityRenderer(EOTPEntities.PAPER_CRANE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EOTPEntities.MEDITATING_BODY.get(), NoopRenderer::new);
    }
}
