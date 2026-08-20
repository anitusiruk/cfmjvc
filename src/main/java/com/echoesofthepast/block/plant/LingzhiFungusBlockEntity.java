package com.echoesofthepast.block.plant;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.imprint.ImprintAction;
import com.echoesofthepast.imprint.ImprintTarget;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiNode;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The fungus reaches into every device around it and pulls the turbulence out, calming their Qi as
 * it feeds. That means placing Lingzhi is genuinely useful maintenance and not just a crop.
 */
public class LingzhiFungusBlockEntity extends QiDeviceBlockEntity implements ImprintTarget {
    /** How far the mycelium reaches. */
    private static final int REACH = 4;
    /** Turbulence eaten per second from each device. */
    private static final float BITE = 0.02F;
    /** Turbulence eaten to fill one saturation step. */
    private static final float PER_STEP = 0.8F;

    private float fed;

    public LingzhiFungusBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.LINGZHI_FUNGUS.get(), pos, state, 40.0F);
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.age % 20 != 0) return;

        float eaten = 0.0F;
        for (BlockPos pos : BlockPos.betweenClosed(
            this.worldPosition.offset(-REACH, -2, -REACH), this.worldPosition.offset(REACH, 2, REACH))) {
            if (pos.equals(this.worldPosition)) continue;
            QiNode node = QiNet.nodeAt(level, pos);
            if (node == null) continue;
            QiStorage storage = node.qiStorage(null);
            if (storage == null || storage.turbulence() <= 0.01F) continue;

            float bite = Math.min(BITE, storage.turbulence());
            storage.calmTurbulence(bite);
            eaten += bite;
            if (this.age % 100 == 0) {
                QiVisuals.line(level,
                    net.minecraft.world.phys.Vec3.atCenterOf(pos),
                    net.minecraft.world.phys.Vec3.atCenterOf(this.worldPosition),
                    0x7E6B4F, 2);
            }
        }

        if (eaten <= 0.0F) return;
        this.fed += eaten;

        BlockState state = this.getBlockState();
        int saturation = state.getValue(LingzhiFungusBlock.SATURATION);
        if (this.fed >= PER_STEP && saturation < 4) {
            this.fed = 0.0F;
            level.setBlockAndUpdate(this.worldPosition, state.setValue(LingzhiFungusBlock.SATURATION, saturation + 1));
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.4, this.worldPosition.getZ() + 0.5,
                4, 0.2, 0.1, 0.2, 0.0);
        }
        this.setChanged();
    }

    /** A saturated cap is worth picking; an unsaturated one is not. */
    public ItemStack harvest(ServerLevel level) {
        BlockState state = this.getBlockState();
        int saturation = state.getValue(LingzhiFungusBlock.SATURATION);
        if (saturation <= 0) return ItemStack.EMPTY;
        level.setBlockAndUpdate(this.worldPosition, state.setValue(LingzhiFungusBlock.SATURATION, 0));
        return new ItemStack(EOTPItems.LINGZHI_CAP.get(), saturation);
    }

    @Override
    public boolean acceptImprint(ServerLevel level, ImprintAction action, ItemStack offered) {
        if (action != ImprintAction.HARVEST) return false;
        ItemStack harvested = this.harvest(level);
        if (harvested.isEmpty()) return false;
        level.addFreshEntity(new ItemEntity(level,
            this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.4, this.worldPosition.getZ() + 0.5, harvested));
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("fed", this.fed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.fed = input.getFloatOr("fed", 0.0F);
    }
}
