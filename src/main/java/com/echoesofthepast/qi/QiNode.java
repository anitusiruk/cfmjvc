package com.echoesofthepast.qi;

import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

/**
 * Implemented by every block entity that Qi can move through. There is no separate "pipe" concept:
 * reservoirs, flues, cauldrons and formation cores are all nodes with different manners.
 */
public interface QiNode {
    /**
     * @param side the face Qi is arriving at or leaving from, or null for the node's own interior.
     * @return the storage exposed on that face, or null if the face is blind.
     */
    @Nullable QiStorage qiStorage(@Nullable Direction side);

    /** How much Qi this node is willing to move through a face each tick. */
    default float qiTransferRate() {
        return 8.0F;
    }

    default boolean acceptsQiFrom(Direction side) {
        return this.qiStorage(side) != null;
    }

    default boolean emitsQiTo(Direction side) {
        return this.qiStorage(side) != null;
    }

    /**
     * How much of the Qi passing through survives. Bends, vertical climbs and poor materials push
     * this below one; jade joints and dragon veins push it back up.
     */
    default float qiEfficiency() {
        return 1.0F;
    }

    /**
     * Told to a node after Qi arrives, so that plumbing can remember which way the current is
     * running. This is what lets a flue know it is being asked to turn a corner.
     */
    default void onQiArrived(Direction side, float amount) {}
}
