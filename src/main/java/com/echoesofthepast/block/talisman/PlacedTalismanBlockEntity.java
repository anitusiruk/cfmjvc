package com.echoesofthepast.block.talisman;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.QiPulse;
import com.echoesofthepast.qi.QiPulseReceiver;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.seal.SealRule;
import com.echoesofthepast.sound.Resonance;
import com.echoesofthepast.talisman.TalismanType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A pasted talisman. It fires when Qi runs into it, when a tone it is stamped for is heard, or when
 * the paper is simply full and the seal on it says to let go.
 *
 * <p>Paper is consumable: every firing costs a charge, and a spent talisman falls off the wall.
 */
public class PlacedTalismanBlockEntity extends QiDeviceBlockEntity implements QiPulseReceiver, Resonance.Listener {
    private static final int DEFAULT_CHARGES = 8;

    private TalismanType type = TalismanType.REPULSION;
    private @Nullable SealRule stampedRule;
    private int charges = DEFAULT_CHARGES;
    private int cooldown;

    public PlacedTalismanBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.PLACED_TALISMAN.get(), pos, state, 24.0F);
    }

    public void configure(TalismanType type, @Nullable SealRule stampedRule, int charges) {
        this.type = type;
        this.stampedRule = stampedRule;
        this.charges = charges;
        this.setChanged();
    }

    public TalismanType type() {
        return this.type;
    }

    /**
     * The rule this paper is stamped with, if any. Formations read this, which is why stamping a
     * talisman and sticking it on a circuit is how a formation gets told what to be.
     */
    public @Nullable SealRule sealRule() {
        return this.stampedRule == null ? this.type.rule() : this.stampedRule;
    }

    public int charges() {
        return this.charges;
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (this.cooldown > 0) this.cooldown--;

        // Qi fed in slowly, rather than pulsed, still sets the paper off once it is saturated.
        if (this.storage.amount() >= this.type.cost() * 2.0F) {
            this.fire(level, this.storage.fillRatio());
        }
    }

    @Override
    public boolean onQiPulse(QiPulse pulse, Direction from) {
        if (!(this.level instanceof ServerLevel level)) return false;
        if (pulse.amount() < this.type.cost() * 0.5F) {
            this.storage.insert(pulse.amount(), pulse.blend(), false);
            return true;
        }
        // How well the pulse suits the paper decides how hard the talisman hits.
        float match = pulse.blend().similarity(this.type.blend());
        this.fire(level, pulse.amount() / this.type.cost() * (0.5F + match));
        return true;
    }

    @Override
    public void onResonance(Resonance.Tone tone, BlockPos source, float strength) {
        // A talisman stamped to be quiet answers sound; the others ignore it.
        if (this.sealRule() != SealRule.SILENCE) return;
        if (!(this.level instanceof ServerLevel level)) return;
        if (this.storage.amount() < this.type.cost()) return;
        this.fire(level, 1.0F);
    }

    private void fire(ServerLevel level, float strength) {
        if (this.cooldown > 0 || this.charges <= 0) return;
        if (!this.storage.tryConsume(this.type.cost())) return;

        this.cooldown = 10;
        this.charges--;
        Direction facing = this.getBlockState().getValue(PlacedTalismanBlock.FACING);
        this.type.trigger(level, this.worldPosition, facing, Math.min(3.0F, strength));
        QiVisuals.bloom(level, Vec3.atCenterOf(this.worldPosition), this.type.blend());

        if (this.charges <= 0) {
            // The paper burns out and the block goes with it.
            level.removeBlock(this.worldPosition, false);
            QiVisuals.backlash(level, Vec3.atCenterOf(this.worldPosition), this.type.blend());
        } else {
            this.setChanged();
        }
    }

    /** Peeling a talisman off gives the paper back, minus what has been spent. */
    public void peelOff(Player player) {
        if (!(this.level instanceof ServerLevel level)) return;
        ItemStack stack = new ItemStack(EOTPItems.talisman(this.type).get());
        stack.set(EOTPComponents.CHARGES.get(), this.charges);
        if (this.stampedRule != null) {
            stack.set(EOTPComponents.SEAL_RULE.get(), this.stampedRule);
        }
        player.getInventory().placeItemBackInInventory(stack);
        level.removeBlock(this.worldPosition, false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("talisman", TalismanType.CODEC, this.type);
        output.storeNullable("stamp", SealRule.CODEC, this.stampedRule);
        output.putInt("charges", this.charges);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.type = input.read("talisman", TalismanType.CODEC).orElse(TalismanType.REPULSION);
        this.stampedRule = input.read("stamp", SealRule.CODEC).orElse(null);
        this.charges = input.getIntOr("charges", DEFAULT_CHARGES);
    }
}
