package com.echoesofthepast.block.formation;

import com.echoesofthepast.qi.Phase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A tall silk banner. A floor formation defines an area; banners give it height and a direction, so
 * the same repulsion circuit becomes a wall once it is flanked by them.
 *
 * <p>Banners are two blocks tall and only count towards a formation while both halves stand.
 */
public class FormationBannerBlock extends Block {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    /** True for the upper half of the banner. */
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    private static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public FormationBannerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, Phase.WOOD).setValue(TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE, TOP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(TOP)) {
            BlockState below = level.getBlockState(pos.below());
            return below.getBlock() instanceof FormationBannerBlock && !below.getValue(TOP);
        }
        return level.getBlockState(pos.below()).isSolidRender();
    }
}
