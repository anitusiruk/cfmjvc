package com.echoesofthepast.qi;

import net.minecraft.core.Direction;

/** A block entity that reacts to discrete pulses arriving along threads, prisms or bells. */
public interface QiPulseReceiver {
    /**
     * @param from the face the pulse arrived at
     * @return true if the pulse was consumed and should not continue
     */
    boolean onQiPulse(QiPulse pulse, Direction from);
}
