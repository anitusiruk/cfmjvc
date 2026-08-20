package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiPacket;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Turning fire into earth is cheap. Turning earth back into fire is not: it fights the cycle, wastes
 * most of what goes in, and roughens what comes out, which is exactly why turbulent Qi ends up being
 * something a workshop has to deal with rather than a number that never appears.
 */
public class ConversionWheelBlockEntity extends QiDeviceBlockEntity {
    private static final float FORWARD_EFFICIENCY = 0.7F;
    private static final float REVERSE_EFFICIENCY = 0.4F;
    private static final float BATCH = 8.0F;
    /** Conversions each catalyst item is good for. */
    private static final int CHARGES_PER_CATALYST = 16;

    private Phase source = Phase.WOOD;
    private boolean reversed;
    private int catalystCharges;

    public ConversionWheelBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.CONVERSION_WHEEL.get(), pos, state, 120.0F);
    }

    public Phase source() {
        return this.source;
    }

    public boolean reversed() {
        return this.reversed;
    }

    public int catalystCharges() {
        return this.catalystCharges;
    }

    /** Turns the rings on to the next phase. */
    public Phase turn() {
        this.source = this.source.generates();
        this.setChanged();
        return this.source;
    }

    public boolean toggleDirection() {
        this.reversed = !this.reversed;
        this.setChanged();
        return this.reversed;
    }

    /** Spirit stone powder is the catalyst; anything else is refused. */
    public boolean acceptCatalyst(ItemStack stack) {
        if (!stack.is(EOTPItems.SPIRIT_STONE_POWDER.get())) return false;
        if (this.catalystCharges > CHARGES_PER_CATALYST * 4) return false;
        stack.shrink(1);
        this.catalystCharges += CHARGES_PER_CATALYST;
        this.setChanged();
        return true;
    }

    public Phase target() {
        return this.reversed ? this.source.generatedBy() : this.source.generates();
    }

    @Override
    protected int idleParticleInterval() {
        return 30;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.age % 10 != 0) return;
        if (this.catalystCharges <= 0) return;

        // Only Qi that actually carries the phase on the rings can be worked on.
        float available = this.storage.amount() * this.storage.blend().get(this.source);
        if (available < BATCH * 0.5F) return;

        QiPacket drawn = this.storage.extract(Math.min(BATCH, available), false);
        if (drawn.isEmpty()) return;

        this.catalystCharges--;
        float efficiency = this.reversed ? REVERSE_EFFICIENCY : FORWARD_EFFICIENCY;
        Phase target = this.target();
        float produced = drawn.amount() * efficiency;

        this.storage.insert(produced, PhaseBlend.of(target), false);
        if (this.reversed) {
            // Working against the cycle leaves the whole pool rough.
            this.storage.addTurbulence(0.12F);
        }

        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        QiVisuals.ring(level, center, 0.6, target.color(), 10);
        QiVisuals.ring(level, center.add(0.0, 0.25, 0.0), 0.4, this.source.color(), 8);

        if (this.storage.fillRatio() > 0.6F) {
            QiNet.pushAround(level, this.worldPosition, this.storage, 10.0F, 0.95F);
        }
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("source", Phase.CODEC, this.source);
        output.putBoolean("reversed", this.reversed);
        output.putInt("catalyst", this.catalystCharges);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.source = input.read("source", Phase.CODEC).orElse(Phase.WOOD);
        this.reversed = input.getBooleanOr("reversed", false);
        this.catalystCharges = input.getIntOr("catalyst", 0);
    }
}
