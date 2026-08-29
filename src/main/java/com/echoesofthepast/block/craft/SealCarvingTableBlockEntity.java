package com.echoesofthepast.block.craft;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.ink.InkType;
import com.echoesofthepast.item.CarvedSealItem;
import com.echoesofthepast.item.SpiritBrushItem;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPComponents;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.seal.SealCarving;
import com.echoesofthepast.seal.SealMaterial;
import com.echoesofthepast.seal.SealRule;
import com.echoesofthepast.talisman.TalismanType;
import com.echoesofthepast.util.Tell;
import com.echoesofthepast.qi.QiStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class SealCarvingTableBlockEntity extends QiDeviceBlockEntity {
    private SealRule chisel = SealRule.BIND;
    /** True when a sheet of talisman paper is lying on the bench. */
    private boolean paperOnBench;
    /** The ink brushed onto that sheet, if any. */
    private @Nullable InkType paperInk;

    public SealCarvingTableBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.SEAL_CARVING_TABLE.get(), pos, state, 0.0F);
    }

    @Override
    public @Nullable QiStorage qiStorage(@Nullable Direction side) {
        return null;
    }

    @Override
    protected int idleParticleInterval() {
        return 0;
    }

    @Override
    protected void deviceTick(ServerLevel level) {
        // Nothing happens on its own here; carving is entirely a hand craft.
    }

    public SealRule chisel() {
        return this.chisel;
    }

    public void turnChisel(Player player) {
        this.chisel = this.chisel.next();
        this.setChanged();
        Tell.overlay(player, Component.translatable("eotp.message.chisel_set",
            Component.translatable(this.chisel.translationKey())));
    }

    public boolean interactWith(Player player, ItemStack stack) {
        if (!(this.level instanceof ServerLevel level)) return false;

        if (stack.is(EOTPItems.TALISMAN_PAPER.get()) && !this.paperOnBench) {
            this.paperOnBench = true;
            this.paperInk = null;
            stack.shrink(1);
            this.setChanged();
            Tell.overlay(player, "eotp.message.paper_laid");
            return true;
        }

        if (stack.getItem() instanceof SpiritBrushItem) {
            return this.brushPaper(player, stack);
        }

        if (stack.getItem() instanceof CarvedSealItem) {
            return this.stamp(level, player, stack);
        }

        SealMaterial material = SealMaterial.of(stack);
        if (material != null) {
            return this.carve(level, player, stack, material);
        }
        return false;
    }

    private boolean brushPaper(Player player, ItemStack brush) {
        if (!this.paperOnBench) {
            Tell.overlay(player, "eotp.message.no_paper");
            return true;
        }
        InkType ink = SpiritBrushItem.inkOf(brush);
        int strokes = SpiritBrushItem.strokesOf(brush);
        if (ink == null || strokes <= 0) {
            Tell.overlay(player, "eotp.message.brush_dry");
            return true;
        }
        this.paperInk = ink;
        brush.set(EOTPComponents.INK_STROKES.get(), strokes - 1);
        if (strokes - 1 <= 0) {
            brush.remove(EOTPComponents.INK_TYPE.get());
        }
        this.setChanged();
        Tell.overlay(player, Component.translatable("eotp.message.paper_inked",
            Component.translatable(ink.translationKey())));
        return true;
    }

    private boolean carve(ServerLevel level, Player player, ItemStack blank, SealMaterial material) {
        ItemStack seal = new ItemStack(EOTPItems.CARVED_SEAL.get());
        seal.set(EOTPComponents.SEAL_RULE.get(), this.chisel);
        seal.set(EOTPComponents.CHARGES.get(), SealCarving.durabilityOf(material));
        blank.shrink(1);
        player.getInventory().placeItemBackInInventory(seal);
        level.playSound(null, this.worldPosition, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 0.8F, 1.0F);
        Tell.overlay(player, Component.translatable("eotp.message.seal_carved",
            Component.translatable(this.chisel.translationKey())));
        return true;
    }

    private boolean stamp(ServerLevel level, Player player, ItemStack seal) {
        if (!this.paperOnBench) {
            Tell.overlay(player, "eotp.message.no_paper");
            return true;
        }
        if (this.paperInk == null) {
            Tell.overlay(player, "eotp.message.paper_not_inked");
            return true;
        }
        SealRule rule = seal.get(EOTPComponents.SEAL_RULE.get());
        if (rule == null) {
            Tell.overlay(player, "eotp.message.seal_blank");
            return true;
        }

        TalismanType type = SealCarving.talismanFor(rule, this.paperInk.phase());
        ItemStack talisman = new ItemStack(EOTPItems.talisman(type).get());
        talisman.set(EOTPComponents.SEAL_RULE.get(), rule);
        player.getInventory().placeItemBackInInventory(talisman);

        Integer charges = seal.get(EOTPComponents.CHARGES.get());
        int remaining = (charges == null ? 1 : charges) - 1;
        if (remaining <= 0) {
            seal.shrink(1);
            Tell.overlay(player, "eotp.message.seal_worn");
        } else {
            seal.set(EOTPComponents.CHARGES.get(), remaining);
        }

        this.paperOnBench = false;
        this.paperInk = null;
        this.setChanged();
        level.playSound(null, this.worldPosition, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.9F, 0.9F);
        Tell.overlay(player, Component.translatable("eotp.message.talisman_made",
            Component.translatable(type.translationKey())));
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("chisel", SealRule.CODEC, this.chisel);
        output.putBoolean("paper", this.paperOnBench);
        output.storeNullable("paper_ink", InkType.CODEC, this.paperInk);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.chisel = input.read("chisel", SealRule.CODEC).orElse(SealRule.BIND);
        this.paperOnBench = input.getBooleanOr("paper", false);
        this.paperInk = input.read("paper_ink", InkType.CODEC).orElse(null);
    }
}
