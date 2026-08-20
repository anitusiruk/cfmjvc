package com.echoesofthepast.ink;

import com.echoesofthepast.qi.Phase;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

/**
 * What was ground on the inkstone. Ink decides what a brush can draw: plain ink draws formation
 * lines, cinnabar draws lines that carry fire, echo ink draws lines that remember.
 */
public enum InkType implements StringRepresentable {
    /** Charcoal and water. Draws lines that conduct a trickle of Qi of no particular character. */
    PLAIN("plain", null, 24, 0x1B1B1F),
    /** Cinnabar. Fire aligned, and the only ink an ember talisman will take. */
    CINNABAR("cinnabar", Phase.FIRE, 16, 0xB3402C),
    /** Ground jade. Metal aligned, precise, and needed for anything that must not smudge. */
    JADE("jade", Phase.METAL, 12, 0xBFD8C8),
    /** Pine sap and leaf. Wood aligned, favoured for growing things. */
    SAP("sap", Phase.WOOD, 20, 0x5E8C4A),
    /** Clay slip. Earth aligned, heavy, good for anchoring and storage. */
    SLIP("slip", Phase.EARTH, 20, 0xA98F63),
    /** Moon lotus and spring water. Water aligned, the ink of quiet rooms. */
    LOTUS("lotus", Phase.WATER, 16, 0x4F7FB8),
    /** Ink mixed with echo essence. Draws lines that can hold an imprint of the past. */
    ECHO("echo", null, 8, 0x8E7FC4);

    public static final InkType[] VALUES = values();
    public static final Codec<InkType> CODEC = StringRepresentable.fromEnum(InkType::values);

    private final String name;
    private final @Nullable Phase phase;
    private final int strokes;
    private final int color;

    InkType(String name, @Nullable Phase phase, int strokes, int color) {
        this.name = name;
        this.phase = phase;
        this.strokes = strokes;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public @Nullable Phase phase() {
        return this.phase;
    }

    /** How many strokes a full brush holds. */
    public int strokes() {
        return this.strokes;
    }

    public int color() {
        return this.color;
    }

    public String translationKey() {
        return "eotp.ink." + this.name;
    }
}
