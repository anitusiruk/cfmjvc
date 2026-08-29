package com.echoesofthepast.block.formation;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A seal painted so thin it is nearly invisible. It does not weigh what crosses it - it reads
 * <em>how</em> it crossed. Walking, running, sneaking, jumping and cloudstepping each produce a
 * different signal, which turns wuxia footwork itself into an input device.
 */
public class FootworkSealBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 0.25, 16.0);

    public FootworkSealBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolidRender();
    }

    @Override
    protected void entityInside(
        BlockState state, Level level, BlockPos pos, Entity entity,
        InsideBlockEffectApplier effectApplier, boolean isPrecise
    ) {
        if (level.isClientSide()) return;
        if (level.getBlockEntity(pos) instanceof FootworkSealBlockEntity seal) {
            seal.readGait(entity);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FootworkSealBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == EOTPBlockEntities.FOOTWORK_SEAL.get() ? QiDeviceBlockEntity.ticker() : null;
    }
}
