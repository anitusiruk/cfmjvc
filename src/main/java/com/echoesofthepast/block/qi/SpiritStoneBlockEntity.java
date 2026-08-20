package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Keeps the visible charge on the blockstate in step with what the stone is actually holding. */
public class SpiritStoneBlockEntity extends QiDeviceBlockEntity {
    public SpiritStoneBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.SPIRIT_STONE_BLOCK.get(), pos, state, capacityOf(state));
    }

    private static float capacityOf(BlockState state) {
        return state.getBlock() instanceof SpiritStoneBlock stone ? stone.capacity() : 200.0F;
    }

    @Override
    protected int idleParticleInterval() {
        return 80;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        // A charged stone feeds whatever is plumbed onto it, so stones double as the mod's simplest
        // way of getting Qi into a device by hand: mine one, place it where it is needed.
        if (this.storage.fillRatio() > 0.02F) {
            QiNet.pushAround(level, this.worldPosition, this.storage, 5.0F, 0.99F);
        }

        if (this.age % 20 != 0) return;
        BlockState state = this.getBlockState();
        if (!state.hasProperty(SpiritStoneBlock.CHARGE)) return;

        int shown = Math.round(this.storage.fillRatio() * 4.0F);
        if (state.getValue(SpiritStoneBlock.CHARGE) != shown) {
            level.setBlockAndUpdate(this.worldPosition, state.setValue(SpiritStoneBlock.CHARGE, shown));
        }
    }
}
