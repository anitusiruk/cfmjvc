package com.echoesofthepast.seal;

import com.echoesofthepast.qi.Phase;
import com.echoesofthepast.talisman.TalismanType;
import org.jspecify.annotations.Nullable;

/**
 * How a stamped rule and an ink meet to make a specific talisman. The rule says what should happen;
 * the ink says in what manner. Fire ink under a binding seal does not bind, it burns.
 */
public final class SealCarving {
    private SealCarving() {}

    public static TalismanType talismanFor(SealRule rule, @Nullable Phase inkPhase) {
        return switch (rule) {
            case REPEL, DIVIDE -> TalismanType.REPULSION;
            case BIND -> inkPhase == Phase.FIRE ? TalismanType.EMBER : TalismanType.BINDING;
            case PRESERVE -> TalismanType.PRESERVATION;
            case SILENCE -> inkPhase == Phase.WATER ? TalismanType.CLEAR_HEART : TalismanType.SILENCE;
            case GATHER -> TalismanType.GATHER;
            case RETURN -> TalismanType.RETURN;
        };
    }

    /** How many stampings a seal cut from a given material survives. */
    public static int durabilityOf(SealMaterial material) {
        return material.stampings();
    }
}
