package lol.sylvie.universalvc.screen.setup;

import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.screen.quick.QuickMenuScreen;
import lol.sylvie.universalvc.screen.settings.ModSettings;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.net.URI;

public class SetupScreen extends Screen {
    private final Screen parent;
    public SetupScreen(Screen parent) {
        super(Component.translatable("menu.uvc.setup"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();


        int width = (int) (this.width / 1.5);
        HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 20, 24);
        layout.addToHeader(new StringWidget(this.getTitle(), font));

        LinearLayout items = LinearLayout.vertical();

        items.addChild(new MultiLineTextWidget(Component.translatable("menu.uvc.setup.friend"), font).setMaxWidth(width));

        items.addChild(SpacerElement.height(8));

        Component link = Component.translatable("menu.uvc.settings.discord")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://docs.discord.com/developers/discord-social-sdk/getting-started/using-c++"))));
        items.addChild(new MultiLineTextWidget(Component.translatable("menu.uvc.setup.alone", link), font).setMaxWidth(width));

        items.addChild(SpacerElement.height(8));

        Component text = Component.translatable("menu.uvc.settings.application_id");
        EditBox appId = new EditBox(font, width, 14, text);
        appId.setHint(text);
        items.addChild(appId);

        layout.addToContents(items);

        int buttonWidth = (width / 2) - 4;
        LinearLayout buttons = LinearLayout.horizontal().spacing(4);
        buttons.setX(this.width / 2 - (width / 2));

        buttons.addChild(Button.builder(CommonComponents.GUI_BACK, _ -> onClose()).width(buttonWidth).build());
        buttons.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> {
            if (!ModSettings.isLong(appId.getValue())) return;
            UniversalVoiceChat.MOD_SETTINGS.applicationId = appId.getValue();
            QuickMenuScreen.tryOpen(parent);
            UniversalVoiceChat.MOD_SETTINGS.save();
        }).width(buttonWidth).build());
        layout.addToFooter(buttons);

        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
        items.arrangeElements();
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
