package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiBeam;
import com.echoesofthepast.qi.QiPulse;
import com.echoesofthepast.qi.QiPulseReceiver;
import com.echoesofthepast.qi.QiStorage;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** The refraction rules. Everything a prism does is worked out from the face the pulse entered by. */
public class QiPrismBlockEntity extends QiDeviceBlockEntity implements QiPulseReceiver {
    private PrismFacet facet = PrismFacet.RELAY;
    private Phase phase = Phase.WATER;

    public QiPrismBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.QI_PRISM.get(), pos, state, 0.0F);
    }

    @Override
    public @Nullable QiStorage qiStorage(@Nullable Direction side) {
        return null;
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    public PrismFacet facet() {
        return this.facet;
    }

    public PrismFacet peekNextFacet() {
        return this.facet.next();
    }

    public PrismFacet recut() {
        this.facet = this.facet.next();
        this.setChanged();
        return this.facet;
    }

    public Phase turnPhase() {
        this.phase = this.phase.generates();
        this.setChanged();
        return this.phase;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        // A prism is purely reactive; it holds nothing between pulses.
    }

    @Override
    public boolean onQiPulse(QiPulse pulse, Direction from) {
        if (!(this.level instanceof ServerLevel level)) return false;
        Direction forward = from.getOpposite();
        QiVisuals.bloom(level, Vec3.atCenterOf(this.worldPosition), pulse.blend());

        switch (this.facet) {
            case RELAY -> QiBeam.fire(level, this.worldPosition, forward, pulse.withAmount(pulse.amount() * 0.85F));
            case SPLIT -> {
                QiPulse half = QiPulse.create(pulse.amount() * 0.45F, pulse.blend(), pulse.rule());
                QiBeam.fire(level, this.worldPosition, rotate(forward, true), half);
                QiBeam.fire(level, this.worldPosition, rotate(forward, false), QiPulse.create(half.amount(), half.blend(), half.rule()));
            }
            case FOCUS -> QiBeam.fire(level, this.worldPosition, forward, pulse.withAmount(pulse.amount() * 1.35F));
            case BEND -> QiBeam.fire(level, this.worldPosition, rotate(forward, true), pulse.withAmount(pulse.amount() * 0.92F));
            case FILTER -> {
                float share = pulse.blend().normalised().get(this.phase);
                float passed = pulse.amount() * share;
                if (passed > 0.05F) {
                    QiBeam.fire(level, this.worldPosition, forward,
                        QiPulse.create(passed, PhaseBlend.of(this.phase), pulse.rule()));
                }
                float dumped = pulse.amount() - passed;
                if (dumped > 0.05F) {
                    QiVisuals.leak(level, this.worldPosition, pulse.blend(), dumped);
                }
            }
            case SCATTER -> {
                float share = pulse.amount() * 0.22F;
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    QiBeam.fire(level, this.worldPosition, side, QiPulse.create(share, pulse.blend(), pulse.rule()));
                }
            }
        }
        return true;
    }

    /** A quarter turn around the vertical axis, or around north for vertical beams. */
    private static Direction rotate(Direction direction, boolean clockwise) {
        if (direction.getAxis().isVertical()) {
            return clockwise ? Direction.NORTH : Direction.SOUTH;
        }
        return clockwise ? direction.getClockWise() : direction.getCounterClockWise();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("facet", PrismFacet.CODEC, this.facet);
        output.store("phase", Phase.CODEC, this.phase);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.facet = input.read("facet", PrismFacet.CODEC).orElse(PrismFacet.RELAY);
        this.phase = input.read("phase", Phase.CODEC).orElse(Phase.WATER);
    }
}
