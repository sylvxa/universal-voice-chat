package lol.sylvie.universalvc.screen.util;

import lol.sylvie.universalvc.UniversalVoiceChat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class AbstractConfigurationScreen extends ImageBackedScreen {
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 24, 24);
    private final boolean noSave;

    protected AbstractConfigurationScreen(Component title, boolean noSave) {
        super(title, UniversalVoiceChat.id("settings/background"), 208, 224, 256);
        this.noSave = noSave;
    }

    protected AbstractConfigurationScreen(Component title) {
        this(title, false);
    }

    protected void addHeader(LinearLayout settings, MutableComponent component) {
        settings.addChild(new StringWidget(width, 11, component.withStyle(ChatFormatting.BOLD), font));
    }

    protected abstract void addSettings(LinearLayout settings, int width);

    protected abstract void saveAndExit();

    @Override
    protected void init() {
        super.init();
        int margin = 8;
        int widthNoMargin = this.bgWidth - (margin * 2);

        int innerX = this.guiX + margin;
        int innerY = this.guiY + 20;
        LinearLayout settings = LinearLayout.vertical().spacing(2);

        addSettings(settings, widthNoMargin - 5);

        ScrollableLayout scroll = new ScrollableLayout(minecraft, settings, 170);
        scroll.setMinWidth(widthNoMargin - 4 - 6 - 2);
        scroll.setPosition(innerX - 8, innerY);
        scroll.arrangeElements();
        scroll.visitWidgets(this::addRenderableWidget);


        // Footer
        int spacing = 4;
        LinearLayout buttons = LinearLayout.horizontal().spacing(spacing);
        buttons.setPosition(this.guiX + margin, this.guiY + this.bgHeight - margin - 20);

        int buttonCount = noSave ? 1 : 2;
        int buttonWidth = ((widthNoMargin - (spacing * (buttonCount - 1))) / buttonCount);

        Button closeButton = Button.builder(CommonComponents.GUI_BACK, _ -> this.onClose()).size(buttonWidth, 20).build();
        buttons.addChild(closeButton);
        if (!noSave) {
            Button saveButton = Button.builder(CommonComponents.GUI_DONE, _ -> saveAndExit()).size(buttonWidth, 20).build();
            buttons.addChild(saveButton);
        }

        buttons.visitWidgets(this::addRenderableWidget);
        buttons.arrangeElements();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
