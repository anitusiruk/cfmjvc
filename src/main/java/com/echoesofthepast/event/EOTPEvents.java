package com.echoesofthepast.event;

/** Single place where every game bus listener is hooked up. */
public final class EOTPEvents {
    private EOTPEvents() {}

    public static void register() {
        CultivationEvents.register();
        MovementEvents.register();
        CombatEvents.register();
        EchoEvents.register();
    }
}
