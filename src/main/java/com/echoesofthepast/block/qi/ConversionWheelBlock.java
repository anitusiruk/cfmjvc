package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.util.Tell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Five rings that turn Qi one step along the generating cycle. Right-click turns the rings to pick
 * which phase is being worked on; sneak-right-click reverses the direction of travel, which needs to
 * have been learned and is never efficient.
 */
public class ConversionWheelBlock extends Block implements EntityBlock {
    public ConversionWheelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConversionWheelBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == EOTPBlockEntities.CONVERSION_WHEEL.get() ? QiDeviceBlockEntity.ticker() : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ConversionWheelBlockEntity wheel)) return InteractionResult.PASS;

        if (stack.is(EOTPItems.ECHO_ESSENCE.get()) && !Cultivation.knows(player, Discovery.REVERSE_CYCLE)) {
            stack.shrink(1);
            Cultivation.teach(player, Discovery.REVERSE_CYCLE);
            Tell.overlay(player, "eotp.message.wheel_remembers_reverse");
            return InteractionResult.SUCCESS;
        }
        if (wheel.acceptCatalyst(stack)) {
            Tell.overlay(player, Component.translatable("eotp.message.catalyst_added", wheel.catalystCharges()));
            return InteractionResult.SUCCESS;
        }
        return this.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ConversionWheelBlockEntity wheel)) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (!wheel.reversed() && !Cultivation.knows(player, Discovery.REVERSE_CYCLE)) {
                Tell.overlay(player, "eotp.message.dont_understand");
                return InteractionResult.SUCCESS;
            }
            Tell.overlay(player, wheel.toggleDirection()
                ? Component.translatable("eotp.message.wheel_reversed")
                : Component.translatable("eotp.message.wheel_forward"));
            return InteractionResult.SUCCESS;
        }

        Tell.overlay(player, Component.translatable("eotp.message.wheel_set",
            Component.translatable(wheel.turn().translationKey())));
        return InteractionResult.SUCCESS;
    }
}
