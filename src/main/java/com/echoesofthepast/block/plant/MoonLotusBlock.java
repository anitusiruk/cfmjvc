package com.echoesofthepast.block.plant;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A lotus that opens at night and gathers Qi out of moonlight. It can be automated, but daylight
 * closes it no matter what you do, so a moon lotus farm is really a storage and timing problem.
 */
public class MoonLotusBlock extends Block implements EntityBlock {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    /** Petals ready to be picked. */
    public static final IntegerProperty PETALS = IntegerProperty.create("petals", 0, 3);

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0);

    public MoonLotusBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false).setValue(PETALS, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, PETALS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.WATER) || below.is(net.minecraft.world.level.block.Blocks.MUD)
            || below.is(Blocks.CLAY) || below.getFluidState().isSource();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MoonLotusBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == EOTPBlockEntities.MOON_LOTUS.get() ? QiDeviceBlockEntity.ticker() : null;
    }
}
