package com.echoesofthepast;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Knobs for the parts of the mod that a server owner is most likely to want to slow down or speed
 * up. Everything else is deliberately tuned in code so that the mechanics stay legible.
 */
public final class EOTPConfig {
    public static final ForgeConfigSpec SPEC;
    private static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        SPEC = pair.getRight();
    }

    private EOTPConfig() {}

    public static double cultivationRate() {
        return COMMON.cultivationRate.get();
    }

    public static double qiRegenRate() {
        return COMMON.qiRegenRate.get();
    }

    public static boolean tribulationBreaksBlocks() {
        return COMMON.tribulationBreaksBlocks.get();
    }

    public static boolean heartDemonEnabled() {
        return COMMON.heartDemonEnabled.get();
    }

    public static int echoMemoryTicks() {
        return COMMON.echoMemoryTicks.get();
    }

    private static final class Common {
        private final ForgeConfigSpec.DoubleValue cultivationRate;
        private final ForgeConfigSpec.DoubleValue qiRegenRate;
        private final ForgeConfigSpec.BooleanValue tribulationBreaksBlocks;
        private final ForgeConfigSpec.BooleanValue heartDemonEnabled;
        private final ForgeConfigSpec.IntValue echoMemoryTicks;

        private Common(ForgeConfigSpec.Builder builder) {
            builder.push("cultivation");
            this.cultivationRate = builder
                .comment("Multiplier on how quickly meridians open and realms fill.")
                .defineInRange("cultivationRate", 1.0D, 0.05D, 20.0D);
            this.qiRegenRate = builder
                .comment("Multiplier on how quickly a cultivator's personal Qi refills.")
                .defineInRange("qiRegenRate", 1.0D, 0.05D, 20.0D);
            this.heartDemonEnabled = builder
                .comment("Whether dangerous breakthroughs can summon a heart demon.")
                .define("heartDemonEnabled", true);
            builder.pop();

            builder.push("tribulation");
            this.tribulationBreaksBlocks = builder
                .comment("Whether tribulation lightning is allowed to set fires and break blocks.")
                .define("tribulationBreaksBlocks", true);
            builder.pop();

            builder.push("echoes");
            this.echoMemoryTicks = builder
                .comment("How long the world remembers an event for the Echo Mirror to find, in ticks.")
                .defineInRange("echoMemoryTicks", 24000, 1200, 240000);
            builder.pop();
        }
    }
}
