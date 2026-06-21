package lol.sylvie.universalvc.sdk;

import net.minecraft.network.chat.Component;

public enum NoiseDampenMode {
    WEBRTC(Component.literal("WebRTC")),
    KRISP(Component.literal("Krisp")),
    NONE(Component.literal("None"));

    private final Component component;
    NoiseDampenMode(Component component) {
        this.component = component;
    }

    public Component asComponent() {
        return component;
    }
}
