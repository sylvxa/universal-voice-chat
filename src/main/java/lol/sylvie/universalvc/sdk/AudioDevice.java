package lol.sylvie.universalvc.sdk;

import net.minecraft.network.chat.Component;

public record AudioDevice(String id, String name) {
    public Component asComponent() {
        return this.id.equals("default") ? Component.literal("Default") : Component.literal(name);
    }
}
