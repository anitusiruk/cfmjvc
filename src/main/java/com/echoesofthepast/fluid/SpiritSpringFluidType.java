package com.echoesofthepast.fluid;

import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;

/**
 * Spirit spring water. It looks almost like water and behaves almost like water; what makes it worth
 * routing is what grows beside it and what can be brewed with it.
 *
 * <p>No client extensions are registered on purpose, so the fluid renders with vanilla water
 * textures until an artist replaces them.
 */
public class SpiritSpringFluidType extends FluidType {
    public SpiritSpringFluidType() {
        super(FluidType.Properties.create()
            .descriptionId("fluid.eotp.spirit_spring_water")
            .fallDistanceModifier(0.0F)
            .canExtinguish(true)
            // Spring sources are precious world finds, not an infinite 2x2-water trick.
            .canConvertToSource(false)
            .supportsBoating(true)
            .canHydrate(true)
            // Slightly thinner than water: a spring runs quick and bright.
            .density(960)
            .viscosity(900)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY));
    }
}
