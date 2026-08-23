package com.echoesofthepast.cultivation;

import com.echoesofthepast.item.EchoScrollItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Reads mastered Verse Scrolls that have been laid out around a Formation Core, either dropped on
 * the circle or kept in a container standing on it.
 *
 * <p>Placing the scrolls physically is the point: a Golden Core ritual is the player arranging the
 * three relationships they understand around themselves, not choosing three entries from a menu.
 */
public final class VerseScrolls {
    private static final double RADIUS = 6.0;

    private VerseScrolls() {}

    public static List<Verse> readAround(ServerLevel level, BlockPos corePos) {
        List<Verse> found = new ArrayList<>();

        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(corePos).inflate(RADIUS))) {
            collect(entity.getItem(), found);
        }

        for (BlockPos pos : BlockPos.betweenClosed(
            corePos.offset((int) -RADIUS, -2, (int) -RADIUS),
            corePos.offset((int) RADIUS, 2, (int) RADIUS)
        )) {
            if (!(level.getBlockEntity(pos) instanceof Container container)) continue;
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                collect(container.getItem(slot), found);
            }
        }
        return List.copyOf(found);
    }

    private static void collect(ItemStack stack, List<Verse> into) {
        Verse verse = EchoScrollItem.masteredVerseOf(stack);
        if (verse == null) return;
        if (into.stream().anyMatch(known -> known.key().equals(verse.key()))) return;
        into.add(verse);
    }
}
