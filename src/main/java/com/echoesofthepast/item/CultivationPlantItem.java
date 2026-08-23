package com.echoesofthepast.item;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A harvested root or shoot that can be planted directly without pretending to be its block item.
 * This keeps the useful alchemy ingredient and the renewable crop starter as the same object.
 */
public class CultivationPlantItem extends Item {
    private final Supplier<? extends Block> plant;

    public CultivationPlantItem(Properties properties, Supplier<? extends Block> plant) {
        super(properties);
        this.plant = plant;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clicked = context.getClickedPos();
        BlockPos target = level.getBlockState(clicked).canBeReplaced()
            ? clicked
            : clicked.relative(context.getClickedFace());
        BlockState state = this.plant.get().defaultBlockState();

        if (!level.getBlockState(target).canBeReplaced() || !state.canSurvive(level, target)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        level.setBlockAndUpdate(target, state);
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
