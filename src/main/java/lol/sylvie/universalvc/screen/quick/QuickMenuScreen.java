package lol.sylvie.universalvc.screen.quick;

import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.screen.manage.LobbyManageScreen;
import lol.sylvie.universalvc.screen.util.ImageBackedScreen;
import lol.sylvie.universalvc.screen.util.LoadingScreen;
import lol.sylvie.universalvc.screen.settings.VoiceSettingsScreen;
import lol.sylvie.universalvc.screen.setup.SetupScreen;
import lol.sylvie.universalvc.util.ModIcons;
import lol.sylvie.universalvc.util.Result;
import lol.sylvie.universalvc.voice.LobbyHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class QuickMenuScreen extends ImageBackedScreen {
    public QuickMenuScreen() {
        super(Component.translatable("uvc.name.short"), UniversalVoiceChat.id("menu/background"), 128, 105, 128);
    }

    private static Identifier getMuteIcon() {
        return LobbyHandler.isMuted() ? ModIcons.MUTED : ModIcons.UNMUTED;
    }

    private static Identifier getDeafIcon() {
        return LobbyHandler.isDeafened() ? ModIcons.DEAFENED : ModIcons.UNDEAFENED;
    }

    private void loadCallback(Result result) {
        minecraft.execute(() -> {
            Screen screen = minecraft.gui.screen();
            if (screen instanceof LoadingScreen | screen instanceof QuickMenuScreen) minecraft.gui.setScreen(new QuickMenuScreen());
        });
    }

    public static void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() instanceof QuickMenuScreen) // refresh
            minecraft.gui.setScreen(new QuickMenuScreen());
        else if (minecraft.gui.screen() instanceof LobbyManageScreen)
            LobbyManageScreen.refresh();
    }

    @Override
    protected void init() {
        super.init();
        int guiWithMargin = this.guiX + 7;

        int lobbyX = this.guiX + 10;
        int lobbyY = this.guiY + 20;

        boolean isInCall = LobbyHandler.call != null;
        if (isInCall) {
            StringWidget title = new StringWidget(lobbyX, lobbyY, 107, 9, Component.translatable("menu.uvc.lobby.title", LobbyHandler.secret).withStyle(ChatFormatting.WHITE), font);
            int users = LobbyHandler.participantMap.size();
            StringWidget status = new StringWidget(lobbyX, lobbyY + 10, 107, 9, Component.translatable("menu.uvc.lobby.status.connected", users, users == 1 ? "" : "s").withStyle(ChatFormatting.GRAY), font);
            addRenderableWidget(title);
            addRenderableWidget(status);

            Button manageButton = Button.builder(Component.translatable("menu.uvc.manage"), _ -> {
                minecraft.gui.setScreen(new LobbyManageScreen());
            }).bounds(lobbyX, this.guiY + 40, 108, 14).build();

            addRenderableWidget(manageButton);
        } else {
            EditBox editBox = new EditBox(font, 107, 15, Component.translatable("menu.uvc.lobby.secret"));
            editBox.setPosition(lobbyX, lobbyY);
            editBox.setHint(Component.translatable("menu.uvc.lobby.secret"));
            addRenderableWidget(editBox);

            Button joinButton = Button.builder(Component.translatable("menu.uvc.lobby.join"), _ -> {
                String value = editBox.getValue();
                if (value.isEmpty()) return;
                minecraft.gui.setScreen(new LoadingScreen(Component.translatable("text.uvc.joining")));
                LobbyHandler.createOrJoin(value, this::loadCallback);
            }).bounds(lobbyX, this.guiY + 38, 108, 16).build();
            addRenderableWidget(joinButton);
        }

        Button settingsButton = Button.builder(Component.translatable("menu.uvc.settings"), _ -> {
           VoiceSettingsScreen.open();
        }).bounds(guiWithMargin, this.guiY + 60, 114, 14).build();
        addRenderableWidget(settingsButton);

        // Small buttons
        int iconsY = this.guiY + 78;
        Button toggleMuteButton = SpriteIconButton.builder(Component.translatable("text.uvc.mute"), _ -> {
            LobbyHandler.toggleMute();
            this.rebuildWidgets();
        }, true).sprite(getMuteIcon(), 16, 16).size(18, 18).withTootip().build();
        toggleMuteButton.setPosition(guiWithMargin, iconsY);
        toggleMuteButton.active = isInCall;
        addRenderableWidget(toggleMuteButton);

        Button toggleDeafButton = SpriteIconButton.builder(Component.translatable("text.uvc.deaf"), _ -> {
            LobbyHandler.toggleDeafen();
            this.rebuildWidgets();
        }, true).sprite(getDeafIcon(), 16, 16).size(18, 18).withTootip().build();
        toggleDeafButton.setPosition(guiWithMargin + 22, iconsY);
        toggleDeafButton.active = isInCall;
        addRenderableWidget(toggleDeafButton);

        Button exitButton = SpriteIconButton.builder(Component.translatable("text.uvc.exit"), _ -> {
            minecraft.gui.setScreen(new LoadingScreen(Component.translatable("text.uvc.leaving")));
            LobbyHandler.leave(this::loadCallback);
        }, true).sprite(ModIcons.EXIT, 16, 16).size(18, 18).withTootip().build();
        exitButton.setPosition(this.guiX + this.bgWidth - 7 - 18, iconsY);
        exitButton.active = isInCall;
        addRenderableWidget(exitButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // client.gui.toastManager().addToast(new SystemToast(SystemToast.SystemToastId.CHUNK_LOAD_FAILURE, Component.translatable("error.uvc.not_ready"), null));
    private static void open() {
        Minecraft.getInstance().gui.setScreen(new QuickMenuScreen());
    }

    public static void tryOpen(Screen previous) {
        Minecraft client = Minecraft.getInstance();
        if (UniversalVoiceChat.isUnavailable()) {
            Consumer<Result> resultHandler = result -> {
                client.execute(() -> {
                    if (result.success()) {
                        open();
                    } else {
                        client.gui.setScreen(new ConfirmScreen(retry -> {
                            if (retry) {
                                client.gui.setScreen(previous instanceof SetupScreen setup ? setup : new SetupScreen(previous));
                            } else {
                                client.gui.setScreen(previous);
                            }
                        }, Component.translatable("error.uvc.no_connect"), Component.translatable("error.uvc.no_connect.message"), CommonComponents.GUI_YES, CommonComponents.GUI_NO));
                        client.gui.toastManager().addToast(new SystemToast(SystemToast.SystemToastId.PACK_LOAD_FAILURE, Component.translatable("error.uvc.details"), Component.literal(result.message())));
                    }
                });
            };
            client.gui.setScreen(new LoadingScreen(Component.translatable("text.uvc.connecting"), () -> {
                resultHandler.accept(new Result(false, "Timed out! Check your application ID."));
                client.gui.setScreen(new SetupScreen(previous));
            }, 10000L));

            boolean attempted = UniversalVoiceChat.initDiscord(resultHandler);
            if (!attempted) {
                client.gui.setScreen(new SetupScreen(previous));
            }
        } else {
            open();
        }
    }
}
