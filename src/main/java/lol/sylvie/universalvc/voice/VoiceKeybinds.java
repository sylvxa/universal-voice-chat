package lol.sylvie.universalvc.voice;

import com.mojang.blaze3d.platform.InputConstants;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.screen.quick.QuickMenuScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class VoiceKeybinds {
    public static void init() {
        KeyMapping.Category CATEGORY = KeyMapping.Category.register(
                UniversalVoiceChat.id("keys")
        );

        KeyMapping menuKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.universalvc.menu",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        CATEGORY
                ));

        KeyMapping muteKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "text.uvc.mute",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_M,
                        CATEGORY
                ));

        KeyMapping deafenKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "text.uvc.deaf",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_N,
                        CATEGORY
                ));

        ClientTickEvents.END_CLIENT_TICK.register(_ -> {
            while (menuKey.consumeClick()) {
                QuickMenuScreen.tryOpen(null);
            }

            while (muteKey.consumeClick()) {
                LobbyHandler.toggleMute();
            }

            while (deafenKey.consumeClick()) {
                LobbyHandler.toggleDeafen();
            }
        });
    }
}
