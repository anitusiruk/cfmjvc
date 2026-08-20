package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The disc itself. Resonance with neighbouring discs is recounted occasionally rather than every
 * tick, so building a large array costs nothing at runtime.
 */
public class JadeBiReservoirBlockEntity extends QiDeviceBlockEntity {
    private static final float BASE_CAPACITY = 400.0F;
    /** Radius searched for sibling discs. Concentric rings of five are the classic layout. */
    private static final int RESONANCE_RADIUS = 3;

    private int siblings;
    private float resonanceBonus = 1.0F;

    public JadeBiReservoirBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.JADE_BI_RESERVOIR.get(), pos, state, BASE_CAPACITY);
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.age % 40 == 0) {
            this.recountSiblings(level);
        }

        // A charged disc spills its surplus into whatever is plumbed onto it, so a stack of discs
        // behaves like one deep pool without any of them being a "master".
        if (this.storage.fillRatio() > 0.05F) {
            QiNet.pushAround(level, this.worldPosition, this.storage, 6.0F * this.resonanceBonus, 0.98F);
        }

        if (this.age % 15 == 0 && this.storage.fillRatio() > 0.1F) {
            // Qi turning through the hole in the middle of the disc.
            Vec3 center = Vec3.atCenterOf(this.worldPosition);
            QiVisuals.ring(level, center, 0.32 + this.storage.fillRatio() * 0.12,
                this.storage.blend().color(), 6 + (int) (this.storage.fillRatio() * 6.0F));
        }
    }

    private void recountSiblings(ServerLevel level) {
        int found = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            this.worldPosition.offset(-RESONANCE_RADIUS, -RESONANCE_RADIUS, -RESONANCE_RADIUS),
            this.worldPosition.offset(RESONANCE_RADIUS, RESONANCE_RADIUS, RESONANCE_RADIUS))) {
            if (pos.equals(this.worldPosition)) continue;
            if (level.getBlockEntity(pos) instanceof JadeBiReservoirBlockEntity) {
                found++;
            }
        }
        this.siblings = found;
        // Each neighbouring disc adds a little; the gain tapers so a solid cube of discs is a waste
        // of jade compared with a ring of six.
        this.resonanceBonus = 1.0F + (float) Math.sqrt(found) * 0.35F;
        this.storage.setCapacityMultiplier(this.resonanceBonus);
    }

    @Override
    public float qiTransferRate() {
        return 6.0F * this.resonanceBonus;
    }

    public Component describe() {
        return Component.translatable("eotp.message.reservoir", this.storage.describe(), this.siblings);
    }
}
