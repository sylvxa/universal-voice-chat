package lol.sylvie.universalvc.screen.manage;

import com.mojang.authlib.GameProfile;
import lol.sylvie.universalvc.screen.quick.QuickMenuScreen;
import lol.sylvie.universalvc.screen.util.AbstractConfigurationScreen;
import lol.sylvie.universalvc.screen.util.PlayerNameWidget;
import lol.sylvie.universalvc.screen.util.SimpleSlider;
import lol.sylvie.universalvc.voice.LobbyHandler;
import lol.sylvie.universalvc.voice.VoiceParticipant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.objects.PlayerSprite;
import net.minecraft.world.item.component.ResolvableProfile;

public class LobbyManageScreen extends AbstractConfigurationScreen {
    public LobbyManageScreen() {
        super(Component.translatable("menu.uvc.manage.long"), true);
    }

    @Override
    protected void addSettings(LinearLayout settings, int width) {
        for (VoiceParticipant participant : LobbyHandler.discordIdMap.values()) {
            if (participant.getProfile().id().equals(Minecraft.getInstance().getGameProfile().id())) continue;

            GameProfile profile = participant.getProfile();
            Component icon = Component.object(new PlayerSprite(ResolvableProfile.createUnresolved(profile.id()), true)).withoutShadow();
            MutableComponent component = Component.empty();
            component.append(icon);
            component.append(" ");
            component.append(profile.name());
            settings.addChild(new PlayerNameWidget(0, 0, width, 8, participant.getProfile(), font));

            float volume = participant.getVolume(LobbyHandler.call);
            settings.addChild(new SimpleSlider(width, 18, "menu.uvc.manage.volume", 0, 200, volume) {
                @Override
                protected void applyValue() {
                    participant.setVolume(LobbyHandler.call, this.getRealValue());
                }
            });
        }
    }

    @Override
    protected void saveAndExit() {
        this.onClose();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(new QuickMenuScreen());
    }

    public static void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() instanceof LobbyManageScreen) // refresh
            minecraft.gui.setScreen(new LobbyManageScreen());
    }
}
