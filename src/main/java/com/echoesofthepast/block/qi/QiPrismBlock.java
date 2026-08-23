package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A hanging piece of cut jade. Right-click re-cuts the facet, sneak-right-click turns it to a
 * different phase, which only matters for the filtering cut.
 */
public class QiPrismBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);

    public QiPrismBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QiPrismBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == EOTPBlockEntities.QI_PRISM.get() ? QiDeviceBlockEntity.ticker() : null;
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hit
    ) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!stack.is(EOTPItems.ECHO_ESSENCE.get()) || Cultivation.knows(player, Discovery.PRISM_FACETS)) {
            return this.useWithoutItem(state, level, pos, player, hit);
        }

        stack.shrink(1);
        Cultivation.teach(player, Discovery.PRISM_FACETS);
        Tell.overlay(player, "eotp.message.prism_remembers_facets");
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof QiPrismBlockEntity prism)) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            Tell.overlay(player, Component.translatable("eotp.message.prism_phase",
                Component.translatable(prism.turnPhase().translationKey())));
            return InteractionResult.SUCCESS;
        }

        PrismFacet next = prism.peekNextFacet();
        if (next.advanced() && !Cultivation.knows(player, Discovery.PRISM_FACETS)) {
            Tell.overlay(player, "eotp.message.cut_beyond_skill");
            return InteractionResult.SUCCESS;
        }
        Tell.overlay(player, Component.translatable("eotp.message.prism_cut",
            Component.translatable(prism.recut().translationKey())));
        return InteractionResult.SUCCESS;
    }
}
