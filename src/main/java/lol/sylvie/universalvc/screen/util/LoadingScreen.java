package lol.sylvie.universalvc.screen.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class LoadingScreen extends Screen {
    private final Runnable onCancel;
    private final long timeout;
    public LoadingScreen(Component title, Runnable onCancel, long timeout) {
        super(title);
        this.onCancel = onCancel;
        this.timeout = timeout;
    }

    public LoadingScreen(Component title) {
        this(title, null, 10000);
    }

    private final Long added = System.currentTimeMillis();
    private Button exit;

    @Override
    protected void init() {
        super.init();
        exit = Button.builder(CommonComponents.GUI_CANCEL, _ -> {
            minecraft.gui.setScreen(null);
            if (onCancel != null) onCancel.run();
        }).bounds(this.width / 2 - 98, this.height - 24, 196, 20).build();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int center = this.width / 2;
        int middle = this.height / 2;
        graphics.centeredText(font, this.title, center, middle - 6, 0xFFFFFFFF);
        graphics.centeredText(font, LoadingDotsText.get(Util.getMillis()), center, middle + 6, 0xFFAAAAAA);
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() > this.added + timeout && !this.children().contains(exit)) {
            addRenderableWidget(exit);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
