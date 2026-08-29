package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Silk and powdered jade laid flat against a surface. Unlike a flue it does not carry a current at
 * all; it carries single pulses, one block per tick, which is what makes it the wiring of choice for
 * detectors, timers and compact formations.
 */
public class MeridianThreadBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACE = BlockStateProperties.FACING;

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
        Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),   // attached to the floor, hanging DOWN
        Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0), // attached to the ceiling
        Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0),
        Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0),
        Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };

    public MeridianThreadBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACE, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACE, context.getClickedFace().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACE).ordinal()];
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeridianThreadBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == EOTPBlockEntities.MERIDIAN_THREAD.get() ? QiDeviceBlockEntity.ticker() : null;
    }
}
