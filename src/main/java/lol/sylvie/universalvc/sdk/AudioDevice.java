package lol.sylvie.universalvc.sdk;

import com.discord.Discord_String;
import lol.sylvie.universalvc.util.NativeHelper;
import net.minecraft.network.chat.Component;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.discord.cdiscord_h.Discord_AudioDevice_Id;
import static com.discord.cdiscord_h.Discord_AudioDevice_Name;

public record AudioDevice(String id, String name) {
    public Component asComponent() {
        return this.id.equals("default") ? Component.literal("Default") : Component.literal(name);
    }

    private static String splitName(String name) {
        String[] split = name.split(": ", 2);
        if (split.length != 2) return name;
        return split[1];
    }

    public static AudioDevice fromDiscord(Arena arena, MemorySegment device) {
        MemorySegment id = Discord_String.allocate(arena);
        Discord_AudioDevice_Id(device, id);
        MemorySegment name = Discord_String.allocate(arena);
        Discord_AudioDevice_Name(device, name);

        return new AudioDevice(NativeHelper.readDiscordString(id), splitName(NativeHelper.readDiscordString(name)));
    }
}
