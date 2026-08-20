package com.echoesofthepast.block.plant;

import com.echoesofthepast.registry.EOTPBlocks;
import com.echoesofthepast.world.DragonVeins;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bamboo that only grows where there is Qi to grow on. Ambient Qi from a dragon vein or a gathering
 * formation makes it climb; drown a stalk in Qi and it shoots up but comes out hollow and brittle,
 * which is a real trade-off rather than a punishment - hollow bamboo is what cheap flues are made of.
 */
public class SpiritBambooBlock extends Block {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    /** Tallest a stalk will grow. */
    private static final int MAX_HEIGHT = 5;
    /** Ambient Qi past which growth turns brittle. */
    private static final float GLUT = 1.4F;

    private static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);

    public SpiritBambooBlock(Properties properties) {
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
        return below.getBlock() instanceof SpiritBambooBlock
            || below.is(Blocks.DIRT)
            || below.is(Blocks.GRASS_BLOCK)
            || below.is(Blocks.COARSE_DIRT)
            || below.is(Blocks.ROOTED_DIRT)
            || below.is(Blocks.SAND)
            || below.is(Blocks.MOSS_BLOCK);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        float ambient = DragonVeins.ambientQi(level, pos) + this.formationSupport(level, pos);
        if (ambient < 0.25F) return;

        int age = state.getValue(AGE);
        if (age < 3) {
            // Thickening happens quietly; a young shoot is not worth harvesting.
            if (random.nextFloat() < ambient * 0.4F) {
                level.setBlockAndUpdate(pos, state.setValue(AGE, age + 1));
            }
            return;
        }

        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir()) return;
        if (this.heightOf(level, pos) >= MAX_HEIGHT) return;
        if (random.nextFloat() > ambient * 0.35F) return;

        // Too much Qi and the new section comes up hollow.
        BlockState grown = ambient > GLUT
            ? EOTPBlocks.HOLLOW_SPIRIT_BAMBOO.get().defaultBlockState()
            : this.defaultBlockState().setValue(AGE, 0);
        level.setBlockAndUpdate(above, grown);
    }

    /** Qi that a gathering formation or a charged reservoir is spilling into the soil. */
    private float formationSupport(ServerLevel level, BlockPos pos) {
        float support = 0.0F;
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-3, -2, -3), pos.offset(3, 2, 3))) {
            var node = com.echoesofthepast.qi.QiNet.nodeAt(level, nearby);
            if (node == null) continue;
            var storage = node.qiStorage(null);
            if (storage == null) continue;
            support += storage.fillRatio() * 0.35F;
        }
        return Math.min(1.6F, support);
    }

    private int heightOf(ServerLevel level, BlockPos pos) {
        int height = 1;
        BlockPos cursor = pos.below();
        while (level.getBlockState(cursor).getBlock() instanceof SpiritBambooBlock) {
            height++;
            cursor = cursor.below();
        }
        cursor = pos.above();
        while (level.getBlockState(cursor).getBlock() instanceof SpiritBambooBlock) {
            height++;
            cursor = cursor.above();
        }
        return height;
    }
}
