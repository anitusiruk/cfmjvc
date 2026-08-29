package com.echoesofthepast.registry;

import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

/**
 * The building blocks. These are registered as complete families - block, stairs, slab, wall, pillar,
 * panel - and given only the small mechanical roles their materials imply, so that a cultivation hall
 * built out of the right stone is quietly better than one built out of cobble.
 *
 * <p>Jade and celadon carry the {@code qi_insulating} tag, which reduces leakage from any device
 * sitting against them. Nothing here needs a texture to work; the models are for you to make.
 */
public final class EOTPDecorative {
    private EOTPDecorative() {}

    // ------------------------------------------------------------------------------------ celadon

    /** Celadon: fired stoneware, associated with water-aligned Qi and quiet rooms. */
    public static final RegistryObject<Block> CELADON_BRICKS = plain("celadon_bricks", EOTPDecorative::celadon);
    public static final RegistryObject<Block> CELADON_TILES = plain("celadon_tiles", EOTPDecorative::celadon);
    public static final RegistryObject<Block> CELADON_STAIRS = stairs("celadon_stairs", CELADON_BRICKS, EOTPDecorative::celadon);
    public static final RegistryObject<Block> CELADON_SLAB = slab("celadon_slab", EOTPDecorative::celadon);
    public static final RegistryObject<Block> CELADON_WALL = wall("celadon_wall", EOTPDecorative::celadon);
    public static final RegistryObject<Block> CELADON_PILLAR = plain("celadon_pillar", EOTPDecorative::celadon);
    public static final RegistryObject<Block> CELADON_JAR = plain("celadon_jar", () -> celadon().noOcclusion());
    public static final RegistryObject<Block> CELADON_BASIN = plain("celadon_basin", () -> celadon().noOcclusion());
    public static final RegistryObject<Block> CELADON_LAMP = plain("celadon_lamp", () -> celadon().lightLevel(state -> 12));

    // ---------------------------------------------------------------------------- lacquered wood

    /** Lacquered wood takes seal patterns directly, which is why talisman workshops are built of it. */
    public static final RegistryObject<Block> RED_LACQUERED_PLANKS = plain("red_lacquered_planks", () -> lacquer(MapColor.COLOR_RED));
    public static final RegistryObject<Block> BLACK_LACQUERED_PLANKS = plain("black_lacquered_planks", () -> lacquer(MapColor.COLOR_BLACK));
    public static final RegistryObject<Block> DARK_LACQUERED_PLANKS = plain("dark_lacquered_planks", () -> lacquer(MapColor.COLOR_BROWN));
    public static final RegistryObject<Block> LACQUERED_STAIRS = stairs("lacquered_stairs", RED_LACQUERED_PLANKS, () -> lacquer(MapColor.COLOR_RED));
    public static final RegistryObject<Block> LACQUERED_SLAB = slab("lacquered_slab", () -> lacquer(MapColor.COLOR_RED));
    public static final RegistryObject<Block> LACQUERED_PILLAR = plain("lacquered_pillar", () -> lacquer(MapColor.COLOR_RED));
    public static final RegistryObject<Block> LACQUERED_PANEL = plain("lacquered_panel", () -> lacquer(MapColor.COLOR_BLACK).noOcclusion());
    public static final RegistryObject<Block> LACQUERED_SCREEN = plain("lacquered_screen", () -> lacquer(MapColor.COLOR_BLACK).noOcclusion());
    public static final RegistryObject<Block> LACQUERED_LOW_TABLE = plain("lacquered_low_table", () -> lacquer(MapColor.COLOR_BROWN).noOcclusion());

    // ------------------------------------------------------------------------------- carved jade

    /** Jade reduces the Qi a neighbouring device leaks, so a jade workshop pays for itself. */
    public static final RegistryObject<Block> WHITE_JADE_BLOCK = plain("white_jade_block", () -> jade(MapColor.TERRACOTTA_WHITE));
    public static final RegistryObject<Block> PALE_JADE_BLOCK = plain("pale_jade_block", () -> jade(MapColor.TERRACOTTA_LIGHT_GREEN));
    public static final RegistryObject<Block> DARK_JADE_BLOCK = plain("dark_jade_block", () -> jade(MapColor.COLOR_GREEN));
    public static final RegistryObject<Block> SPIRIT_JADE_BLOCK = plain("spirit_jade_block", () -> jade(MapColor.GLOW_LICHEN).lightLevel(state -> 8));
    public static final RegistryObject<Block> JADE_STAIRS = stairs("jade_stairs", PALE_JADE_BLOCK, () -> jade(MapColor.TERRACOTTA_LIGHT_GREEN));
    public static final RegistryObject<Block> JADE_SLAB = slab("jade_slab", () -> jade(MapColor.TERRACOTTA_LIGHT_GREEN));
    public static final RegistryObject<Block> JADE_PILLAR = plain("jade_pillar", () -> jade(MapColor.TERRACOTTA_WHITE));
    public static final RegistryObject<Block> CARVED_JADE_BORDER = plain("carved_jade_border", () -> jade(MapColor.TERRACOTTA_WHITE));
    public static final RegistryObject<Block> JADE_INLAY = plain("jade_inlay", () -> jade(MapColor.TERRACOTTA_LIGHT_GREEN));

    // ------------------------------------------------------------------------- paper and silk

    /** Thin screens. Painted panels are the intended canvas for landscapes and calligraphy. */
    public static final RegistryObject<Block> RICE_PAPER_SCREEN = plain("rice_paper_screen", EOTPDecorative::screen);
    public static final RegistryObject<Block> SILK_SCREEN = plain("silk_screen", EOTPDecorative::screen);
    public static final RegistryObject<Block> PAINTED_MOUNTAIN_SCREEN = plain("painted_mountain_screen", EOTPDecorative::screen);
    public static final RegistryObject<Block> PAINTED_CLOUD_SCREEN = plain("painted_cloud_screen", EOTPDecorative::screen);
    public static final RegistryObject<Block> PAINTED_RIVER_SCREEN = plain("painted_river_screen", EOTPDecorative::screen);

    /** Hanging scrolls, one per character. Decorative until an awakened seal is stamped on one. */
    public static final RegistryObject<Block> SCROLL_MOUNTAIN = plain("hanging_scroll_mountain", EOTPDecorative::scroll);
    public static final RegistryObject<Block> SCROLL_RIVER = plain("hanging_scroll_river", EOTPDecorative::scroll);
    public static final RegistryObject<Block> SCROLL_SILENCE = plain("hanging_scroll_silence", EOTPDecorative::scroll);
    public static final RegistryObject<Block> SCROLL_RETURN = plain("hanging_scroll_return", EOTPDecorative::scroll);
    public static final RegistryObject<Block> SCROLL_FLAME = plain("hanging_scroll_flame", EOTPDecorative::scroll);
    public static final RegistryObject<Block> SCROLL_GATE = plain("hanging_scroll_gate", EOTPDecorative::scroll);
    public static final RegistryObject<Block> SCROLL_STILLNESS = plain("hanging_scroll_stillness", EOTPDecorative::scroll);
    public static final RegistryObject<Block> SCROLL_SWORD = plain("hanging_scroll_sword", EOTPDecorative::scroll);

    // ------------------------------------------------------------------------------------ helpers

    private static BlockBehaviour.Properties celadon() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_CYAN)
            .strength(1.6F, 3.0F)
            .sound(SoundType.DECORATED_POT)
            .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties jade(MapColor color) {
        return BlockBehaviour.Properties.of()
            .mapColor(color)
            .strength(1.8F, 4.0F)
            .sound(SoundType.CALCITE)
            .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties lacquer(MapColor color) {
        return BlockBehaviour.Properties.of()
            .mapColor(color)
            .strength(1.6F, 2.5F)
            .sound(SoundType.WOOD)
            .ignitedByLava();
    }

    private static BlockBehaviour.Properties screen() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW)
            .strength(0.4F)
            .sound(SoundType.WOOL)
            .noOcclusion();
    }

    private static BlockBehaviour.Properties scroll() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.SNOW)
            .strength(0.3F)
            .sound(SoundType.WOOL)
            .noOcclusion()
            .noCollision();
    }

    private static RegistryObject<Block> plain(String name, Supplier<BlockBehaviour.Properties> properties) {
        return EOTPBlocks.register(name, Block::new, properties);
    }

    private static RegistryObject<Block> stairs(String name, RegistryObject<Block> base, Supplier<BlockBehaviour.Properties> properties) {
        return EOTPBlocks.register(name, props -> new StairBlock(base.get().defaultBlockState(), props), properties);
    }

    private static RegistryObject<Block> slab(String name, Supplier<BlockBehaviour.Properties> properties) {
        return EOTPBlocks.register(name, SlabBlock::new, properties);
    }

    private static RegistryObject<Block> wall(String name, Supplier<BlockBehaviour.Properties> properties) {
        return EOTPBlocks.register(name, WallBlock::new, () -> properties.get().forceSolidOn());
    }

    /** Touched during mod construction so that all of the above actually register. */
    public static void init() {
    }
}
