package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
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
 * Hollow bamboo carrying visible Qi vapour. Deliberately imperfect plumbing: the same length of
 * flue delivers very different amounts depending on how it is routed.
 *
 * @see QiFlueBlockEntity for the loss model
 */
public class QiFlueBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    private final QiFlueBlockEntity.Material material;

    public QiFlueBlock(Properties properties, QiFlueBlockEntity.Material material) {
        super(properties);
        this.material = material;
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
    }

    public QiFlueBlockEntity.Material material() {
        return this.material;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(AXIS)) {
            case X -> Block.box(0.0, 5.0, 5.0, 16.0, 11.0, 11.0);
            case Z -> Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 16.0);
            case Y -> Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QiFlueBlockEntity(pos, state, this.material);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == EOTPBlockEntities.QI_FLUE.get() ? QiDeviceBlockEntity.ticker() : null;
    }
}
