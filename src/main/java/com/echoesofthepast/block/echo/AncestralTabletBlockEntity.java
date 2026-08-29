package com.echoesofthepast.block.echo;

import com.echoesofthepast.block.QiDeviceBlockEntity;
import com.echoesofthepast.cultivation.Cultivation;
import com.echoesofthepast.cultivation.Discovery;
import com.echoesofthepast.echo.EchoLog;
import com.echoesofthepast.imprint.ImprintAction;
import com.echoesofthepast.imprint.ImprintTarget;
import com.echoesofthepast.qi.PhaseBlend;
import com.echoesofthepast.qi.QiVisuals;
import com.echoesofthepast.registry.EOTPBlockEntities;
import com.echoesofthepast.registry.EOTPItems;
import com.echoesofthepast.util.Tell;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Echo imprinting: the mod's automation, and the place where the theme and the machinery are the
 * same thing. Your factory does not run on wires, it runs on the present being taught to imitate the
 * past.
 *
 * <p>How it works:
 * <ol>
 *   <li>the tablet watches interactions a player makes with nearby devices and writes each one down
 *       as a gesture - which device, relative to itself, and what was done to it;</li>
 *   <li>when a run of gestures repeats, the tablet recognises it as a lesson and counts the
 *       repetition. A few honest repetitions and it has the sequence;</li>
 *   <li>echo essence wakes it, and from then on it replays the lesson one gesture at a time, taking
 *       any items it needs out of a container placed against it.</li>
 * </ol>
 *
 * <p>A tablet can only operate devices that have agreed to be operated - see {@link ImprintTarget} -
 * so nothing here has to pretend to be a player.
 */
public class AncestralTabletBlockEntity extends QiDeviceBlockEntity {
    /** How far a tablet can see, and later reach. */
    public static final int REACH = 5;
    /** Gestures a lesson may be. Long enough for a real routine, short enough to learn by hand. */
    private static final int MAX_LESSON = 8;
    /** Repetitions before the tablet is confident it has understood. */
    private static final int REPETITIONS_NEEDED = 3;
    /** Ticks between replayed gestures once awake. */
    private static final int REPLAY_INTERVAL = 40;
    /** Qi each replayed gesture costs. */
    private static final float GESTURE_COST = 3.0F;

    /**
     * One remembered gesture.
     *
     * @param offset where the device is, relative to the tablet
     * @param action what was done to it
     */
    private record Gesture(BlockPos offset, ImprintAction action) {
        private boolean matches(Gesture other) {
            return this.offset.equals(other.offset) && this.action == other.action;
        }
    }

    /** What has been seen recently but not yet accepted as a lesson. */
    private final List<Gesture> observed = new ArrayList<>();
    /** The lesson the tablet believes it has learned. */
    private final List<Gesture> lesson = new ArrayList<>();

    private int repetitions;
    private boolean awakened;
    private int cursor;
    private int cooldown;

    public AncestralTabletBlockEntity(BlockPos pos, BlockState state) {
        super(EOTPBlockEntities.ANCESTRAL_TABLET.get(), pos, state, 120.0F);
    }

    @Override
    protected int idleParticleInterval() {
        return this.awakened ? 40 : 0;
    }

    // ------------------------------------------------------------------------------- watching

    /**
     * Called when a player does something to a device near this tablet.
     *
     * <p>The learning rule is deliberately forgiving: the tablet keeps the last few gestures, and as
     * soon as the tail of what it has seen repeats itself, it treats that repeating run as the
     * lesson. That means a player teaches it simply by doing the job a few times, which is exactly
     * how you would teach anything.
     */
    public void observe(BlockPos devicePos, ImprintAction action) {
        if (this.awakened) return;
        Gesture gesture = new Gesture(devicePos.subtract(this.worldPosition), action);
        this.observed.add(gesture);
        while (this.observed.size() > MAX_LESSON * 2) {
            this.observed.remove(0);
        }
        this.detectRepetition();
        this.setChanged();

        if (this.level instanceof ServerLevel level) {
            QiVisuals.echo(level, Vec3.atCenterOf(this.worldPosition).add(0.0, 0.9, 0.0), 2);
        }
    }

    /** Looks for the longest run at the end of the observations that repeats immediately before it. */
    private void detectRepetition() {
        int size = this.observed.size();
        for (int length = Math.min(MAX_LESSON, size / 2); length >= 1; length--) {
            boolean same = true;
            for (int index = 0; index < length; index++) {
                Gesture recent = this.observed.get(size - length + index);
                Gesture earlier = this.observed.get(size - length * 2 + index);
                if (!recent.matches(earlier)) {
                    same = false;
                    break;
                }
            }
            if (!same) continue;

            List<Gesture> candidate = new ArrayList<>(this.observed.subList(size - length, size));
            if (this.lesson.equals(candidate)) {
                this.repetitions++;
            } else {
                this.lesson.clear();
                this.lesson.addAll(candidate);
                this.repetitions = 1;
            }
            return;
        }
    }

    public boolean hasLesson() {
        return !this.lesson.isEmpty() && this.repetitions >= REPETITIONS_NEEDED;
    }

    // -------------------------------------------------------------------------------- waking

    /** Echo essence wakes a tablet that has understood something. */
    public boolean offer(Player player, ItemStack stack) {
        if (!(this.level instanceof ServerLevel level)) return false;
        if (!stack.is(EOTPItems.ECHO_ESSENCE.get())) return false;

        if (this.awakened) {
            Tell.overlay(player, "eotp.message.tablet_already_awake");
            return true;
        }
        if (!this.hasLesson()) {
            Tell.chat(player, Component.translatable("eotp.message.tablet_not_ready",
                this.lesson.size(), this.repetitions, REPETITIONS_NEEDED));
            return true;
        }

        stack.shrink(1);
        this.awakened = true;
        this.cursor = 0;
        this.setChanged();
        level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(AncestralTabletBlock.AWAKENED, true));
        level.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.7F, 1.2F);
        QiVisuals.bloom(level, Vec3.atCenterOf(this.worldPosition).add(0.0, 1.0, 0.0), PhaseBlend.BALANCED);

        Tell.chat(player, Component.translatable("eotp.message.tablet_awakened", this.lesson.size()));
        Cultivation.teach(player, Discovery.ECHO_IMPRINTING);
        EchoLog.record(level, this.worldPosition, EchoLog.Kind.RITUAL,
            Component.translatable("eotp.echo.tablet_awakened", player.getName()));
        return true;
    }

    /** Empty-handed: the tablet says back what it thinks it has been taught. */
    public void recite(Player player) {
        if (this.lesson.isEmpty()) {
            Tell.chat(player, Component.translatable("eotp.message.tablet_watching", this.observed.size()));
            return;
        }
        Tell.chat(player, Component.translatable("eotp.message.tablet_lesson",
            this.lesson.size(), this.repetitions, this.awakened ? 1 : 0));
        for (Gesture gesture : this.lesson) {
            Tell.chat(player, Component.translatable("eotp.message.tablet_gesture",
                Component.translatable(gesture.action().translationKey()),
                gesture.offset().getX(), gesture.offset().getY(), gesture.offset().getZ()));
        }
    }

    // ------------------------------------------------------------------------------ replaying

    @Override
    protected void deviceTick(ServerLevel level) {
        if (!this.awakened || this.lesson.isEmpty()) return;
        if (this.cooldown > 0) {
            this.cooldown--;
            return;
        }
        this.cooldown = REPLAY_INTERVAL;

        if (!this.storage.tryConsume(GESTURE_COST)) {
            // Out of Qi: the hands fade rather than the tablet forgetting.
            if (this.age % 200 == 0) {
                QiVisuals.leak(level, this.worldPosition, PhaseBlend.EMPTY, 0.5F);
            }
            return;
        }

        Gesture gesture = this.lesson.get(this.cursor % this.lesson.size());
        this.cursor = (this.cursor + 1) % this.lesson.size();

        BlockPos target = this.worldPosition.offset(gesture.offset());
        BlockEntity device = level.getBlockEntity(target);
        if (!(device instanceof ImprintTarget imprintable)) {
            // Whatever used to be there is gone. The tablet keeps trying, visibly, which is the cue
            // to go and look at what changed.
            QiVisuals.echo(level, Vec3.atCenterOf(target), 3);
            return;
        }

        ItemStack offered = gesture.action() == ImprintAction.FEED ? this.takeFromStore(level) : ItemStack.EMPTY;
        boolean worked = imprintable.acceptImprint(level, gesture.action(), offered);

        if (worked) {
            // The translucent hands: a line from the tablet to whatever it just touched.
            QiVisuals.line(level,
                Vec3.atCenterOf(this.worldPosition).add(0.0, 0.8, 0.0),
                Vec3.atCenterOf(target).add(0.0, 0.5, 0.0),
                0x9F8FD8, 4);
            QiVisuals.echo(level, Vec3.atCenterOf(target).add(0.0, 0.5, 0.0), 2);
        } else if (!offered.isEmpty()) {
            // Give back anything the gesture did not manage to use.
            this.returnToStore(level, offered);
        }
        this.setChanged();
    }

    /**
     * Pulls one item out of a container touching the tablet. A tablet with nothing to work from
     * simply mimes the gesture, which is a readable failure rather than a silent one.
     */
    private ItemStack takeFromStore(ServerLevel level) {
        for (Direction side : Direction.values()) {
            if (!(level.getBlockEntity(this.worldPosition.relative(side)) instanceof Container container)) continue;
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (stack.isEmpty()) continue;
                return container.removeItem(slot, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    private void returnToStore(ServerLevel level, ItemStack stack) {
        for (Direction side : Direction.values()) {
            if (!(level.getBlockEntity(this.worldPosition.relative(side)) instanceof Container container)) continue;
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                if (container.getItem(slot).isEmpty()) {
                    container.setItem(slot, stack);
                    return;
                }
            }
        }
        level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
            level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5, stack));
    }

    /** Finds tablets that should be watching a given interaction. */
    public static List<AncestralTabletBlockEntity> watching(ServerLevel level, BlockPos devicePos) {
        List<AncestralTabletBlockEntity> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
            devicePos.offset(-REACH, -REACH, -REACH), devicePos.offset(REACH, REACH, REACH))) {
            if (level.getBlockEntity(pos) instanceof AncestralTabletBlockEntity tablet && !tablet.awakened) {
                found.add(tablet);
            }
        }
        return found;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("awakened", this.awakened);
        output.putInt("repetitions", this.repetitions);
        output.putInt("cursor", this.cursor);
        writeGestures(output, "lesson", this.lesson);
        writeGestures(output, "observed", this.observed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.awakened = input.getBooleanOr("awakened", false);
        this.repetitions = input.getIntOr("repetitions", 0);
        this.cursor = input.getIntOr("cursor", 0);
        this.lesson.clear();
        this.observed.clear();
        readGestures(input, "lesson", this.lesson);
        readGestures(input, "observed", this.observed);
    }

    private static void writeGestures(ValueOutput output, String key, List<Gesture> gestures) {
        var list = output.childrenList(key);
        for (Gesture gesture : gestures) {
            ValueOutput child = list.addChild();
            child.putInt("x", gesture.offset().getX());
            child.putInt("y", gesture.offset().getY());
            child.putInt("z", gesture.offset().getZ());
            child.store("action", ImprintAction.CODEC, gesture.action());
        }
    }

    private static void readGestures(ValueInput input, String key, List<Gesture> into) {
        input.childrenList(key).ifPresent(list -> {
            for (ValueInput child : list) {
                ImprintAction action = child.read("action", ImprintAction.CODEC).orElse(null);
                if (action == null) continue;
                into.add(new Gesture(new BlockPos(
                    child.getIntOr("x", 0), child.getIntOr("y", 0), child.getIntOr("z", 0)), action));
            }
        });
    }

    public boolean isAwakened() {
        return this.awakened;
    }

    /** Used by the compass to explain a tablet without opening anything. */
    public @Nullable Component summary() {
        if (this.lesson.isEmpty()) return null;
        return Component.translatable("eotp.message.tablet_lesson",
            this.lesson.size(), this.repetitions, this.awakened ? 1 : 0);
    }
}
