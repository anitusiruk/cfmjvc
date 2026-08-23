package com.echoesofthepast.block.plant;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.fluid.SpiritSpringEffects;
import com.echoesofthepast.imprint.ImprintAction;
import com.echoesofthepast.imprint.ImprintTarget;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gathering is only possible while the flower is open, and it only opens under an unobstructed night
 * sky. Petals accumulate slowly and can be picked by hand or by a tablet.
 */
public class MoonLotusBlockEntity extends QiDeviceBlockEntity implements ImprintTarget {
    /** Qi gathered per second while open under the moon. */
    private static final float GATHER_RATE = 0.9F;
    /** Qi that must pass through the flower before it sets another petal. */
    private static final float PETAL_COST = 120.0F;
    /** Qi a single night must yield for the cycle to count as a complete one. */
    private static final float CYCLE_PRODUCTION = 60.0F;

    private float towardsPetal;
    /** Qi gathered since the flower last opened, reset at each dusk and dawn. */
    private float nightProduction;

    public MoonLotusBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.MOON_LOTUS.get(), pos, state, 90.0F);
    }

    @Override
    protected int idleParticleInterval() {
        return 60;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.age % 20 != 0) return;

        boolean shouldBeOpen = level.isDarkOutside() && level.canSeeSky(this.worldPosition);
        BlockState state = this.getBlockState();
        if (state.getValue(MoonLotusBlock.OPEN) != shouldBeOpen) {
            level.setBlockAndUpdate(this.worldPosition, state.setValue(MoonLotusBlock.OPEN, shouldBeOpen));
            state = this.getBlockState();
            if (shouldBeOpen) {
                this.nightProduction = 0.0F;
                QiVisuals.bloom(level, net.minecraft.world.phys.Vec3.atCenterOf(this.worldPosition), PhaseBlend.of(Phase.WATER));
            } else {
                // Closing at dawn after a productive night, still plumbed into something, is a whole
                // spiritual cycle: the Witness of Heaven for any mortal who watched it happen.
                boolean connected = false;
                for (net.minecraft.core.Direction side : net.minecraft.core.Direction.values()) {
                    if (QiNet.nodeAt(level, this.worldPosition.relative(side)) != null) {
                        connected = true;
                        break;
                    }
                }
                if (connected && this.nightProduction >= CYCLE_PRODUCTION) {
                    com.echoesofthepast.cultivation.Witnesses.completeHeaven(level, this.worldPosition);
                }
                this.nightProduction = 0.0F;
            }
        }
        if (!shouldBeOpen) return;

        // Moonlight is thin: gathering is slow, and the point of the plant is that it works while
        // you are asleep rather than that it works quickly.
        float springMultiplier = SpiritSpringEffects.isSpring(level, this.worldPosition.below()) ? 1.5F : 1.0F;
        float gathered = GATHER_RATE * (1.0F + level.getMoonBrightness(this.worldPosition)) * springMultiplier;
        this.storage.insert(gathered, PhaseBlend.of(Phase.WATER), false);
        this.towardsPetal += gathered;
        this.nightProduction += gathered;

        if (this.towardsPetal >= PETAL_COST) {
            this.towardsPetal = 0.0F;
            int petals = state.getValue(MoonLotusBlock.PETALS);
            if (petals < 3) {
                level.setBlockAndUpdate(this.worldPosition, state.setValue(MoonLotusBlock.PETALS, petals + 1));
            }
        }

        QiNet.pushAround(level, this.worldPosition, this.storage, 4.0F, 0.95F);
        this.setChanged();
    }

    /** Picking petals. Returns how many came off. */
    public int pick(ServerLevel level) {
        BlockState state = this.getBlockState();
        int petals = state.getValue(MoonLotusBlock.PETALS);
        if (petals <= 0) return 0;
        level.setBlockAndUpdate(this.worldPosition, state.setValue(MoonLotusBlock.PETALS, 0));
        return petals;
    }

    @Override
    public boolean acceptImprint(ServerLevel level, ImprintAction action, ItemStack offered) {
        if (action != ImprintAction.HARVEST) return false;
        int picked = this.pick(level);
        if (picked <= 0) return false;
        level.addFreshEntity(new ItemEntity(level,
            this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.4, this.worldPosition.getZ() + 0.5,
            new ItemStack(EOTPItems.MOON_LOTUS_PETAL.get(), picked)));
        return true;
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("towards_petal", this.towardsPetal);
        output.putFloat("night_production", this.nightProduction);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.towardsPetal = input.getFloatOr("towards_petal", 0.0F);
        this.nightProduction = input.getFloatOr("night_production", 0.0F);
    }
}
