package com.echoesofthepast.block.formation;

import com.echoesofthepast.qi.Phase;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A line painted onto a floor with a loaded brush. Ink cannot be crafted or placed from the hand;
 * it only exists where somebody drew it, and it wears out where people walk.
 *
 * <p>The colour of a line is its phase, which is what makes a formation readable from a doorway.
 */
public class FormationInkBlock extends Block {
    public static final EnumProperty<Phase> PHASE = EnumProperty.create("phase", Phase.class);
    public static final EnumProperty<Stroke> STROKE = EnumProperty.create("stroke", Stroke.class);
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 0.25, 16.0);

    public enum Stroke implements StringRepresentable {
        STRAIGHT("straight"),
        CORNER("corner"),
        CROSS("cross"),
        CURVE("curve"),
        GLYPH("glyph");

        public static final Codec<Stroke> CODEC = StringRepresentable.fromEnum(Stroke::values);
        private static final Stroke[] VALUES = values();

        private final String name;

        Stroke(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Stroke next() {
            return VALUES[(this.ordinal() + 1) % VALUES.length];
        }
    }

    public FormationInkBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(PHASE, Phase.EARTH)
            .setValue(STROKE, Stroke.STRAIGHT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PHASE, STROKE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolidRender();
    }
}
