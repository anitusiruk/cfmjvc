package com.echoesofthepast.block.plant;

import com.echoesofthepast.block.QiDeviceBlockEntity;
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

    private float towardsPetal;

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
                QiVisuals.bloom(level, net.minecraft.world.phys.Vec3.atCenterOf(this.worldPosition), PhaseBlend.of(Phase.WATER));
            }
        }
        if (!shouldBeOpen) return;

        // Moonlight is thin: gathering is slow, and the point of the plant is that it works while
        // you are asleep rather than that it works quickly.
        float gathered = GATHER_RATE * (1.0F + level.getMoonBrightness(this.worldPosition));
        this.storage.insert(gathered, PhaseBlend.of(Phase.WATER), false);
        this.towardsPetal += gathered;

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
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.towardsPetal = input.getFloatOr("towards_petal", 0.0F);
    }
}
