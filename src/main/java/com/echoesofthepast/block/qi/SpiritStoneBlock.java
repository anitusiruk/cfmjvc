package com.echoesofthepast.block.qi;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.registry.EOTPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jspecify.annotations.Nullable;

/**
 * A placed block of refined spirit stone. Unlike a battery with a number on it, the charge is in the
 * blockstate and drives the light level, so a wall of spirit stone dims as the workshop drinks it.
 */
public class SpiritStoneBlock extends Block implements EntityBlock {
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 4);

    /** Grade of the stone, which sets both capacity and how brightly it burns when full. */
    private final int grade;

    public SpiritStoneBlock(Properties properties, int grade) {
        super(properties);
        this.grade = grade;
        this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, 4));
    }

    public int grade() {
        return this.grade;
    }

    /** Capacity scales with grade: low stone holds a little, high stone holds a great deal. */
    public float capacity() {
        return 200.0F * this.grade;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiritStoneBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == EOTPBlockEntities.SPIRIT_STONE_BLOCK.get() ? QiDeviceBlockEntity.ticker() : null;
    }
}
