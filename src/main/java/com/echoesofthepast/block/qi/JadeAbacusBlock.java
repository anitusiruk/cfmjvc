package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The mod's controller. Beads set a threshold, the frame sets what is being counted, and when the
 * count is reached the abacus sends a pulse. No screen, no numbers on a display - the answer is
 * literally the position of the beads.
 */
public class JadeAbacusBlock extends Block implements EntityBlock {
    public static final IntegerProperty BEADS = IntegerProperty.create("beads", 1, 9);

    public JadeAbacusBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BEADS, 5));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BEADS);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new JadeAbacusBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == EOTPBlockEntities.JADE_ABACUS.get() ? QiDeviceBlockEntity.ticker() : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) instanceof JadeAbacusBlockEntity abacus) {
                Tell.overlay(player, Component.translatable("eotp.message.abacus_counting",
                    Component.translatable(abacus.cycleCondition().translationKey())));
            }
            return InteractionResult.SUCCESS;
        }

        int beads = state.getValue(BEADS) % 9 + 1;
        level.setBlockAndUpdate(pos, state.setValue(BEADS, beads));
        if (level.getBlockEntity(pos) instanceof JadeAbacusBlockEntity abacus) {
            abacus.resetCount();
        }
        Tell.overlay(player, Component.translatable("eotp.message.abacus_beads", beads));
        return InteractionResult.SUCCESS;
    }
}
