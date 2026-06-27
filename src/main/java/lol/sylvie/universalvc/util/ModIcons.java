package lol.sylvie.universalvc.util;

import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.screen.quick.QuickMenuScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

public class ModIcons {
    public static final Identifier MUTED = UniversalVoiceChat.id("status/muted");
    public static final Identifier UNMUTED = UniversalVoiceChat.id("status/unmuted");
    public static final Identifier DEAFENED = UniversalVoiceChat.id("status/deafened");
    public static final Identifier UNDEAFENED = UniversalVoiceChat.id("status/undeafened");

    public static final Identifier EXIT = UniversalVoiceChat.id("action/exit");


    public static SpriteIconButton getMenuButton(Screen previous) {
        return SpriteIconButton.builder(Component.translatable("uvc.name.short"), _ -> QuickMenuScreen.tryOpen(previous), true)
                .sprite(UniversalVoiceChat.id("status/unmuted"), 16, 16)
                .size(20, 20)
                .withTootip()
                .build();
    }
}
