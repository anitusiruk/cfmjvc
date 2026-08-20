package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import com.echoesofthepast.fluid.SpiritSpringFluidType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Spirit spring water: a real fluid, so that it can be piped, bucketed, swum in and grown beside.
 * Deliberately precious - there is no way to make it in bulk, so moving it matters.
 */
public final class EOTPFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.Keys.FLUID_TYPES, EchoesOfThePast.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(ForgeRegistries.FLUIDS, EchoesOfThePast.MODID);

    private EOTPFluids() {}

    public static final RegistryObject<FluidType> SPIRIT_SPRING_TYPE =
        FLUID_TYPES.register("spirit_spring_water", SpiritSpringFluidType::new);

    public static final RegistryObject<ForgeFlowingFluid> SPIRIT_SPRING =
        FLUIDS.register("spirit_spring_water", () -> new ForgeFlowingFluid.Source(springProperties()));

    public static final RegistryObject<ForgeFlowingFluid> SPIRIT_SPRING_FLOWING =
        FLUIDS.register("flowing_spirit_spring_water", () -> new ForgeFlowingFluid.Flowing(springProperties()));

    public static final RegistryObject<net.minecraft.world.level.block.Block> SPIRIT_SPRING_BLOCK =
        EOTPBlocks.registerNoItem("spirit_spring_water",
            properties -> new LiquidBlock(SPIRIT_SPRING.get(), properties),
            () -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.WATER)
                .replaceable()
                .noCollision()
                .strength(100.0F)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY));

    private static ForgeFlowingFluid.Properties springProperties() {
        return new ForgeFlowingFluid.Properties(SPIRIT_SPRING_TYPE, SPIRIT_SPRING, SPIRIT_SPRING_FLOWING)
            .block(() -> (LiquidBlock) SPIRIT_SPRING_BLOCK.get())
            .bucket(EOTPItems.SPIRIT_SPRING_BUCKET)
            // A spring spreads a little further and a little faster than water.
            .slopeFindDistance(5)
            .levelDecreasePerBlock(1)
            .tickRate(4);
    }

    public static void register(BusGroup modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }
}
