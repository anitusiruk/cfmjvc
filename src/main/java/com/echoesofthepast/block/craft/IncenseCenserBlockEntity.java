package com.echoesofthepast.block.craft;

import com.echoesofthepast.aura.IncenseKind;
import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.imprint.ImprintAction;
import com.echoesofthepast.imprint.ImprintTarget;
import com.echoesofthepast.item.IncenseStickItem;
import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Burning is slow and generous: one stick runs for several minutes and covers a room, and Qi fed
 * into the censer makes the smoke reach further rather than making it faster.
 */
public class IncenseCenserBlockEntity extends QiDeviceBlockEntity implements ImprintTarget {
    private static final int BURN_TIME = 4800;

    private @Nullable IncenseKind burning;
    private @Nullable Phase attunement;
    private int remaining;

    public IncenseCenserBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.INCENSE_CENSER.get(), pos, state, 60.0F);
    }

    public boolean isBurning() {
        return this.burning != null && this.remaining > 0;
    }

    public @Nullable IncenseKind burning() {
        return this.burning;
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        if (!this.isBurning()) {
            if (this.getBlockState().getValue(IncenseCenserBlock.LIT)) {
                level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(IncenseCenserBlock.LIT, false));
            }
            return;
        }

        this.remaining--;
        IncenseKind kind = this.burning;

        // Qi in the censer widens the reach; it is not consumed quickly, it is breathed out.
        float fed = this.storage.fillRatio();
        double radius = kind.radius() * (1.0 + fed * 0.6);
        float strength = 1.0F + fed;
        if (this.age % 20 == 0) {
            kind.breathe(level, this.worldPosition, radius, strength, this.attunement);
            this.storage.extract(0.6F, false);
        }

        if (this.age % 4 == 0) {
            double x = this.worldPosition.getX() + 0.5;
            double y = this.worldPosition.getY() + 0.7;
            double z = this.worldPosition.getZ() + 0.5;
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 1, 0.06, 0.0, 0.06, 0.005);
            if (this.attunement != null) {
                level.sendParticles(new DustParticleOptions(this.attunement.color(), 0.7F), x, y, z, 1, 0.1, 0.05, 0.1, 0.01);
            }
        }

        if (this.remaining <= 0) {
            this.burning = null;
            this.attunement = null;
            level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(IncenseCenserBlock.LIT, false));
        }
        this.setChanged();
    }

    public boolean light(Player player, ItemStack stack) {
        if (!(this.level instanceof ServerLevel level)) return false;
        if (!(stack.getItem() instanceof IncenseStickItem stick)) return false;
        if (this.isBurning()) {
            Tell.overlay(player, "eotp.message.censer_busy");
            return true;
        }

        this.burning = stick.kind();
        this.attunement = stack.get(EOTPComponents.PHASE.get());
        this.remaining = BURN_TIME;
        stack.shrink(1);
        this.setChanged();

        level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(IncenseCenserBlock.LIT, true));
        level.playSound(null, this.worldPosition, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 1.4F);
        Tell.overlay(player, Component.translatable("eotp.message.censer_lit",
            Component.translatable(this.burning.translationKey())));
        return true;
    }

    @Override
    public boolean acceptImprint(ServerLevel level, ImprintAction action, ItemStack offered) {
        if (action != ImprintAction.FEED || this.isBurning()) return false;
        if (!(offered.getItem() instanceof IncenseStickItem stick)) return false;
        this.burning = stick.kind();
        this.attunement = offered.get(EOTPComponents.PHASE.get());
        this.remaining = BURN_TIME;
        level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(IncenseCenserBlock.LIT, true));
        this.setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("burning", IncenseKind.CODEC, this.burning);
        output.storeNullable("attunement", Phase.CODEC, this.attunement);
        output.putInt("remaining", this.remaining);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.burning = input.read("burning", IncenseKind.CODEC).orElse(null);
        this.attunement = input.read("attunement", Phase.CODEC).orElse(null);
        this.remaining = input.getIntOr("remaining", 0);
    }
}
