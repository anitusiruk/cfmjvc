package com.echoesofthepast.block.craft;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.ink.InkType;
import com.echoesofthepast.item.SpiritBrushItem;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Grinding is a physical act with a rhythm to it: put a material in, work it until it is fine, then
 * dip. Stopping halfway leaves gritty ink that runs out faster.
 */
public class InkstoneBlockEntity extends QiDeviceBlockEntity {
    /** Strokes of the grindstone needed to work a material down properly. */
    private static final int GRIND_TARGET = 6;

    private @Nullable InkType pending;
    private int ground;

    public InkstoneBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.INKSTONE.get(), pos, state, 20.0F);
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        // Wet ink dries out if it is left alone too long.
        if (this.pending != null && this.age % 1200 == 0 && this.ground > 0) {
            this.ground--;
            this.setChanged();
        }
    }

    public boolean interactWith(Player player, ItemStack stack) {
        if (!(this.level instanceof ServerLevel level)) return false;

        if (stack.getItem() instanceof SpiritBrushItem) {
            return this.dip(player, stack);
        }

        InkType material = materialFor(stack);
        if (material == null) return false;
        if (this.pending != null && this.pending != material) {
            Tell.overlay(player, "eotp.message.inkstone_occupied");
            return true;
        }

        this.pending = material;
        stack.shrink(1);
        this.setChanged();
        level.playSound(null, this.worldPosition, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5F, 1.2F);
        Tell.overlay(player, Component.translatable("eotp.message.inkstone_loaded",
            Component.translatable(material.translationKey())));
        return true;
    }

    /** Empty-handed work: this is the grinding itself. */
    public void grind(Player player) {
        if (!(this.level instanceof ServerLevel level)) return;
        if (this.pending == null) {
            Tell.overlay(player, "eotp.message.inkstone_empty");
            return;
        }
        this.ground = Math.min(GRIND_TARGET, this.ground + 1);
        this.setChanged();

        level.playSound(null, this.worldPosition, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.6F, 0.8F + this.ground * 0.08F);
        level.sendParticles(new DustParticleOptions(this.pending.color(), 0.8F),
            this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.25, this.worldPosition.getZ() + 0.5,
            3, 0.15, 0.02, 0.15, 0.01);

        if (this.ground >= GRIND_TARGET) {
            Tell.overlay(player, "eotp.message.ink_ready");
        }
    }

    /** Dipping a brush. A half-ground well gives fewer strokes, which is the whole penalty. */
    private boolean dip(Player player, ItemStack brush) {
        if (this.pending == null || this.ground <= 0) {
            Tell.overlay(player, "eotp.message.inkstone_empty");
            return true;
        }
        int strokes = Math.max(1, Math.round(this.pending.strokes() * (this.ground / (float) GRIND_TARGET)));
        SpiritBrushItem.load(brush, this.pending, strokes);
        Tell.overlay(player, Component.translatable("eotp.message.brush_loaded",
            Component.translatable(this.pending.translationKey()), strokes));
        this.pending = null;
        this.ground = 0;
        this.setChanged();
        return true;
    }

    /** Which ink a material makes. Everything else is not ink and the stone will not take it. */
    private static @Nullable InkType materialFor(ItemStack stack) {
        if (stack.is(Items.CHARCOAL) || stack.is(Items.COAL)) return InkType.PLAIN;
        if (stack.is(EOTPItems.CINNABAR_PIGMENT.get())) return InkType.CINNABAR;
        if (stack.is(EOTPItems.JADE_DUST.get())) return InkType.JADE;
        if (stack.is(EOTPItems.WOOD_ESSENCE.get())) return InkType.SAP;
        if (stack.is(Items.CLAY_BALL) || stack.is(EOTPItems.EARTH_ESSENCE.get())) return InkType.SLIP;
        if (stack.is(EOTPItems.MOON_LOTUS_PETAL.get())) return InkType.LOTUS;
        if (stack.is(EOTPItems.ECHO_ESSENCE.get())) return InkType.ECHO;
        return null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("pending", InkType.CODEC, this.pending);
        output.putInt("ground", this.ground);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.pending = input.read("pending", InkType.CODEC).orElse(null);
        this.ground = input.getIntOr("ground", 0);
    }
}
