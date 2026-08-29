package com.echoesofthepast.registry;

import com.echoesofthepast.EchoesOfThePast;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class EOTPTags {
    private EOTPTags() {}

    public static final class Blocks {
        private Blocks() {}

        /** Jade, celadon and other materials that keep Qi from bleeding out of a device. */
        public static final TagKey<Block> QI_INSULATING = tag("qi_insulating");
        /** Surfaces formation ink can be drawn on. */
        public static final TagKey<Block> INK_SURFACE = tag("ink_surface");
        /** Counts towards the variety a ginseng root needs around it. */
        public static final TagKey<Block> NATURAL_VARIETY = tag("natural_variety");
        /** Conducts tribulation lightning into machinery instead of into the cultivator. */
        public static final TagKey<Block> TRIBULATION_CONDUCTOR = tag("tribulation_conductor");

        private static TagKey<Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(EchoesOfThePast.MODID, path));
        }
    }

    public static final class Items {
        private Items() {}

        /** Anything that can be ground on an inkstone. */
        public static final TagKey<Item> INK_MATERIAL = tag("ink_material");
        /** Anything a censer will burn for an aura. */
        public static final TagKey<Item> INCENSE = tag("incense");
        /** Herbs the drying rack accepts. */
        public static final TagKey<Item> DRYABLE = tag("dryable");
        /** Materials the seal-carving table can cut a rule into. */
        public static final TagKey<Item> SEAL_BLANK = tag("seal_blank");

        private static TagKey<Item> tag(String path) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(EchoesOfThePast.MODID, path));
        }
    }
}
