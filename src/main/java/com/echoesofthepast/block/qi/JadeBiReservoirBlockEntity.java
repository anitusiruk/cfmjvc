package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.qi.QiNet;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.qi.SpiritStoneTempering;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * The disc itself. Resonance with neighbouring discs is recounted occasionally rather than every
 * tick, so building a large array costs nothing at runtime.
 *
 * <p>The hole in the middle is also where a Low Spirit Stone is left to temper. A stone kept there
 * remembers each phase that runs through the disc, which is how it earns its way up a grade.
 */
public class JadeBiReservoirBlockEntity extends QiDeviceBlockEntity {
    private static final float BASE_CAPACITY = 400.0F;
    /** Radius searched for sibling discs. Concentric rings of five are the classic layout. */
    private static final int RESONANCE_RADIUS = 3;

    private int siblings;
    private float resonanceBonus = 1.0F;
    /** A Low Spirit Stone left in the disc to be tempered by what passes through it. */
    private ItemStack tempering = ItemStack.EMPTY;

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

        this.temperStone();
    }

    /**
     * A stone sitting in the disc learns whatever phase is currently running through it, and forgets
     * everything if the disc is allowed to go rough. This is what turns a Low stone into a Middle
     * one: lived history rather than another crafting grid.
     */
    private void temperStone() {
        if (this.tempering.isEmpty() || this.storage.fillRatio() < 0.05F) return;

        if (this.storage.turbulence() > SpiritStoneTempering.SPOIL_TURBULENCE) {
            SpiritStoneTempering.spoil(this.tempering);
            this.setChanged();
            return;
        }
        if (this.age % 40 != 0) return;

        Phase carried = this.storage.blend().dominant();
        if (carried != null && SpiritStoneTempering.recordPhase(this.tempering, carried)) {
            this.setChanged();
        }
    }

    /**
     * Puts a Low stone in to temper, or takes back whatever is in the hole. A stone that has carried
     * enough different phases comes out cut into a Middle Spirit Stone.
     */
    public boolean exchangeStone(Player player, ItemStack held) {
        if (this.tempering.isEmpty()) {
            if (!held.is(EOTPItems.LOW_SPIRIT_STONE.get())) return false;
            this.tempering = held.split(1);
            this.setChanged();
            Tell.overlay(player, "eotp.message.stone_tempering");
            return true;
        }

        if (!held.isEmpty()) return false;

        boolean ready = SpiritStoneTempering.isTempered(this.tempering);
        ItemStack returned = ready
            ? SpiritStoneTempering.cutToMiddle(this.tempering.copy())
            : this.tempering.copy();
        Tell.chat(player, ready
            ? Component.translatable("eotp.message.stone_tempered")
            : SpiritStoneTempering.describe(this.tempering));

        this.tempering = ItemStack.EMPTY;
        this.setChanged();
        player.getInventory().placeItemBackInInventory(returned);
        return true;
    }

    public ItemStack temperingStone() {
        return this.tempering;
    }

    /**
     * Called when routed tribulation lightning lands on this disc. A Middle stone left in the hole
     * takes the strike and stays quenchable for a short while.
     */
    public void takeTribulationStrike(long now) {
        if (this.tempering.is(EOTPItems.MIDDLE_SPIRIT_STONE.get())) {
            SpiritStoneTempering.markTribulationCharged(this.tempering, now);
            this.setChanged();
        }
    }

    /** Finishing a charged Middle stone with Spirit Spring Water produces a High stone. */
    public boolean quenchChargedStone(Player player, ItemStack bucket) {
        if (!(this.level instanceof ServerLevel level)) return false;
        if (!SpiritStoneTempering.isTribulationCharged(this.tempering, level.getGameTime())) return false;

        ItemStack high = SpiritStoneTempering.quenchToHigh(this.tempering.copy());
        this.tempering = ItemStack.EMPTY;
        this.setChanged();

        if (!player.getAbilities().instabuild) {
            bucket.shrink(1);
            player.getInventory().placeItemBackInInventory(new ItemStack(net.minecraft.world.item.Items.BUCKET));
        }
        player.getInventory().placeItemBackInInventory(high);
        QiVisuals.bloom(level, Vec3.atCenterOf(this.worldPosition), this.storage.blend());
        Tell.chat(player, Component.translatable("eotp.message.stone_quenched"));
        return true;
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

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.tempering.isEmpty()) {
            output.store("tempering", ItemStack.CODEC, this.tempering);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.tempering = input.read("tempering", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
}
