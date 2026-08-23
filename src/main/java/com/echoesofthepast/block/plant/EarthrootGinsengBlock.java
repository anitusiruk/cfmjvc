package com.echoesofthepast.block.plant;

import com.echoesofthepast.fluid.SpiritSpringEffects;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.registry.EOTPTags;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A root that will not be farmed in rows. It matures according to how many <em>different</em> natural
 * blocks surround it, so a monoculture field grows nothing worth digging up and a deliberately
 * varied garden - stone, moss, water, several woods, a few flowers - grows the good stuff.
 */
public class EarthrootGinsengBlock extends Block {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
    /** How far the root feels around itself. */
    private static final int SENSE = 3;
    /** Distinct block types needed before growth is at full speed. */
    private static final int VARIETY_TARGET = 8;

    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 6.0, 13.0);

    public EarthrootGinsengBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.COARSE_DIRT)
            || below.is(Blocks.PODZOL) || below.is(Blocks.ROOTED_DIRT) || below.is(Blocks.MOSS_BLOCK);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 7;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int variety = this.varietyAround(level, pos);
        // Even a perfect garden is slow. This is the mod's long crop on purpose.
        float chance = 0.02F + 0.10F * Math.min(1.0F, variety / (float) VARIETY_TARGET);
        if (SpiritSpringEffects.nearby(level, pos, 3)) {
            chance += 0.06F;
        }
        if (random.nextFloat() < chance) {
            level.setBlockAndUpdate(pos, state.setValue(AGE, state.getValue(AGE) + 1));
        }
    }

    /** Counts distinct natural block types nearby. Repeats of the same block add nothing. */
    private int varietyAround(ServerLevel level, BlockPos pos) {
        Set<Block> seen = new HashSet<>();
        for (BlockPos nearby : BlockPos.betweenClosed(
            pos.offset(-SENSE, -1, -SENSE), pos.offset(SENSE, 2, SENSE))) {
            BlockState state = level.getBlockState(nearby);
            if (state.isAir()) continue;
            if (state.is(EOTPTags.Blocks.NATURAL_VARIETY) || state.is(net.minecraft.tags.BlockTags.DIRT)
                || state.is(net.minecraft.tags.BlockTags.LOGS) || state.is(net.minecraft.tags.BlockTags.LEAVES)
                || state.is(net.minecraft.tags.BlockTags.FLOWERS) || state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD)
                || !state.getFluidState().isEmpty()) {
                seen.add(state.getBlock());
            }
        }
        return seen.size();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < 7) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        // A mature root is dug up whole; there is no partial harvest.
        popResource(level, pos, new ItemStack(EOTPItems.GINSENG_ROOT.get(), 1 + level.getRandom().nextInt(2)));
        level.setBlockAndUpdate(pos, state.setValue(AGE, 0));
        return InteractionResult.SUCCESS;
    }
}
