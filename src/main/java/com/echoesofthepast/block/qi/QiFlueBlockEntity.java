package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The loss model that makes flue routing a design problem rather than a formality:
 *
 * <ul>
 *   <li>every section wastes a little, set by its material;</li>
 *   <li>turning a corner wastes much more, because the Qi has to be persuaded to change direction;</li>
 *   <li>climbing wastes more still, since Qi vapour would rather sink;</li>
 *   <li>a jade joint forgives one corner completely, which is what makes jade worth the cost;</li>
 *   <li>running along a dragon vein claws some of the loss back.</li>
 * </ul>
 */
public class QiFlueBlockEntity extends QiDeviceBlockEntity {
    public enum Material {
        /** Ordinary spirit bamboo: cheap, honest, lossy. */
        BAMBOO(20.0F, 0.94F, 0.70F, 0.72F, false),
        /** Brittle bamboo from a plant that was fed too much Qi. Fast to make, wasteful to use. */
        HOLLOW(14.0F, 0.86F, 0.55F, 0.60F, false),
        /** A jointed jade section: expensive, and the only thing that turns a corner cleanly. */
        JADE_JOINT(36.0F, 0.99F, 0.97F, 0.92F, true);

        private final float capacity;
        private final float straight;
        private final float bend;
        private final float climb;
        private final boolean forgivesBends;

        Material(float capacity, float straight, float bend, float climb, boolean forgivesBends) {
            this.capacity = capacity;
            this.straight = straight;
            this.bend = bend;
            this.climb = climb;
            this.forgivesBends = forgivesBends;
        }

        public float capacity() {
            return this.capacity;
        }

        public boolean forgivesBends() {
            return this.forgivesBends;
        }
    }

    private final Material material;
    /** Sections travelled since the Qi last passed through something that straightened it out. */
    private int runLength;

    public QiFlueBlockEntity(BlockPos pos, BlockState state, Material material) {
        super(EOTPBlockEntities.QI_FLUE.get(), pos, state, material.capacity());
        this.material = material;
    }

    public QiFlueBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, materialOf(state));
    }

    private static Material materialOf(BlockState state) {
        return state.getBlock() instanceof QiFlueBlock flue ? flue.material() : Material.BAMBOO;
    }

    public Material material() {
        return this.material;
    }

    @Override
    public float qiTransferRate() {
        return this.material.capacity() * 0.4F;
    }

    @Override
    public void onQiArrived(Direction side, float amount) {
        super.onQiArrived(side, amount);
        this.runLength = this.material.forgivesBends() ? 0 : this.runLength + 1;
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.storage.isEmpty()) return;

        Direction preferred = this.lastInflow == null ? null : this.lastInflow.getOpposite();
        float rate = this.qiTransferRate();
        float moved = 0.0F;

        // Straight ahead first, so a long run does not spray Qi sideways at every junction.
        if (preferred != null) {
            moved += QiNet.push(level, this.worldPosition, this.storage, preferred, rate, this.efficiency(level, preferred));
        }
        for (Direction side : Direction.values()) {
            if (side == preferred || side == this.lastInflow) continue;
            moved += QiNet.push(level, this.worldPosition, this.storage, side, rate * 0.5F, this.efficiency(level, side));
        }

        if (moved > 0.4F && this.age % 4 == 0) {
            QiVisuals.vapour(level, this.worldPosition, this.storage.blend(), Math.min(2.0F, moved * 0.2F));
        }
    }

    private float efficiency(ServerLevel level, Direction out) {
        float efficiency = this.material.straight;

        boolean bending = this.lastInflow != null && out.getAxis() != this.lastInflow.getAxis();
        if (bending && !this.material.forgivesBends()) {
            efficiency *= this.material.bend;
        }
        if (out == Direction.UP) {
            efficiency *= this.material.climb;
        }
        // A long unsupported run sags. Twelve sections is about the practical limit for bamboo
        // before you want a jade joint or a reservoir to break the run up.
        efficiency *= Math.max(0.45F, 1.0F - this.runLength * 0.015F);
        efficiency *= Math.min(1.15F, 0.94F + (QiNet.siteQuality(level, this.worldPosition) - 1.0F) * 0.12F);

        // Turbulent Qi fights the walls of the flue.
        efficiency *= 1.0F - this.storage.turbulence() * 0.25F;
        return Math.max(0.1F, Math.min(1.0F, efficiency));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("run", this.runLength);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.runLength = input.getIntOr("run", 0);
    }
}
