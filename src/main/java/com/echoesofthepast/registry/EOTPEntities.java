package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.entity.FlyingSwordEntity;
import com.echoesofthepast.entity.HeartDemonEntity;
import com.echoesofthepast.entity.MeditatingBodyEntity;
import com.echoesofthepast.entity.PaperCraneEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EOTPEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EchoesOfThePast.MODID);

    private EOTPEntities() {}

    public static final RegistryObject<EntityType<HeartDemonEntity>> HEART_DEMON =
        ENTITY_TYPES.register("heart_demon", () -> EntityType.Builder
            .of(HeartDemonEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(10)
            .fireImmune()
            .build(key("heart_demon")));

    public static final RegistryObject<EntityType<FlyingSwordEntity>> FLYING_SWORD =
        ENTITY_TYPES.register("flying_sword", () -> EntityType.Builder
            .<FlyingSwordEntity>of(FlyingSwordEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .fireImmune()
            .build(key("flying_sword")));

    public static final RegistryObject<EntityType<PaperCraneEntity>> PAPER_CRANE =
        ENTITY_TYPES.register("paper_crane", () -> EntityType.Builder
            .<PaperCraneEntity>of(PaperCraneEntity::new, MobCategory.MISC)
            .sized(0.4F, 0.3F)
            .clientTrackingRange(8)
            .updateInterval(2)
            .build(key("paper_crane")));

    public static final RegistryObject<EntityType<MeditatingBodyEntity>> MEDITATING_BODY =
        ENTITY_TYPES.register("meditating_body", () -> EntityType.Builder
            .<MeditatingBodyEntity>of(MeditatingBodyEntity::new, MobCategory.MISC)
            .sized(0.6F, 1.2F)
            .clientTrackingRange(10)
            .build(key("meditating_body")));

    public static void register(BusGroup modBus) {
        ENTITY_TYPES.register(modBus);
        EntityAttributeCreationEvent.BUS.addListener(EOTPEntities::createAttributes);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(HEART_DEMON.get(), HeartDemonEntity.createAttributes().build());
    }

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(EchoesOfThePast.MODID, name));
    }
}
