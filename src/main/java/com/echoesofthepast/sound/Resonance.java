package com.echoesofthepast.sound;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Sound is a first class signal in this mod. Bells, struck stone, thunder and impacts all broadcast
 * a tone, and devices tuned to that tone answer. This is what lets a workshop be wired with sound
 * instead of with redstone hidden under the floor.
 */
public final class Resonance {
    /** How far a tone carries. */
    private static final int RANGE = 16;

    private Resonance() {}

    public enum Tone implements StringRepresentable {
        /** A deep bell: the sound of a nearly empty pulse. */
        DEEP("deep"),
        LOW("low"),
        MIDDLE("middle"),
        HIGH("high"),
        /** A bright bell, made by a pulse strong enough to hurt. */
        BRIGHT("bright"),
        /** Struck stone or an anvil: sharp and tuneless. */
        STRIKE("strike"),
        /** Wind through bamboo. */
        BREATH("breath"),
        /** Thunder, including a tribulation bolt. */
        THUNDER("thunder");

        public static final Tone[] VALUES = values();
        public static final Codec<Tone> CODEC = StringRepresentable.fromEnum(Tone::values);

        private final String name;

        Tone(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Tone next() {
            return VALUES[(this.ordinal() + 1) % VALUES.length];
        }

        /** Pitch to play a vanilla bell or chime at, so each tone is audibly distinct. */
        public float pitch() {
            return switch (this) {
                case DEEP -> 0.6F;
                case LOW -> 0.8F;
                case MIDDLE -> 1.0F;
                case HIGH -> 1.3F;
                case BRIGHT -> 1.7F;
                case STRIKE -> 1.1F;
                case BREATH -> 0.9F;
                case THUNDER -> 0.5F;
            };
        }

        public String translationKey() {
            return "eotp.tone." + this.name;
        }

        /** Maps a pulse's strength onto the bell scale. */
        public static Tone ofStrength(int step) {
            return switch (Math.max(0, Math.min(5, step))) {
                case 0 -> DEEP;
                case 1 -> LOW;
                case 2 -> MIDDLE;
                case 3 -> HIGH;
                default -> BRIGHT;
            };
        }
    }

    /** A block entity that listens for tones. */
    public interface Listener {
        void onResonance(Tone tone, BlockPos source, float strength);
    }

    /**
     * Broadcasts a tone. Only the nine chunks around the source are searched, and only their block
     * entity maps, so ringing a bell is cheap even in a dense workshop.
     */
    public static void emit(ServerLevel level, BlockPos source, Tone tone, float strength) {
        List<Listener> listeners = new ArrayList<>();
        int chunkX = source.getX() >> 4;
        int chunkZ = source.getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!level.hasChunk(chunkX + dx, chunkZ + dz)) continue;
                LevelChunk chunk = level.getChunk(chunkX + dx, chunkZ + dz);
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity blockEntity = entry.getValue();
                    if (!(blockEntity instanceof Listener listener)) continue;
                    if (entry.getKey().distSqr(source) > (double) RANGE * RANGE) continue;
                    listeners.add(listener);
                }
            }
        }
        // Collected first so that a listener which places or breaks blocks cannot disturb the sweep.
        for (Listener listener : listeners) {
            listener.onResonance(tone, source, strength);
        }
    }
}
