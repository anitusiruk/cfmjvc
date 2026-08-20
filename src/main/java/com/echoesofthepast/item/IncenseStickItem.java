package com.echoesofthepast.item;

import com.echoesofthepast.aura.IncenseKind;
import net.minecraft.world.item.Item;

/** A stick of prepared incense. Only useful in a censer. */
public class IncenseStickItem extends Item {
    private final IncenseKind kind;

    public IncenseStickItem(Properties properties, IncenseKind kind) {
        super(properties);
        this.kind = kind;
    }

    public IncenseKind kind() {
        return this.kind;
    }
}
