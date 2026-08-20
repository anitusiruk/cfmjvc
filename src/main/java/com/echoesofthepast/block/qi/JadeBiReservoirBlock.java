package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * A ritual jade disc that stores Qi. Its capacity is not fixed: discs stacked or ringed around each
 * other resonate, so a tidy concentric arrangement holds far more than the same discs scattered
 * about. That is the whole point - the pretty layout is the upgrade.
 */
public class JadeBiReservoirBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public JadeBiReservoirBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JadeBiReservoirBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == EOTPBlockEntities.JADE_BI_RESERVOIR.get() ? QiDeviceBlockEntity.ticker() : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof JadeBiReservoirBlockEntity reservoir) {
            if (!level.isClientSide()) {
                Tell.overlay(player, reservoir.describe());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
