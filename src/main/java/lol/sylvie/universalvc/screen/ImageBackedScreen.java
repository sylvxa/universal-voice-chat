package lol.sylvie.universalvc.screen;

import lol.sylvie.universalvc.UniversalVoiceChat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ImageBackedScreen extends Screen {
    private final Identifier sprite;

    protected final int bgWidth;
    protected final int bgHeight;
    protected final int imageSize;
    protected int guiX = 0;
    protected int guiY = 0;

    protected static final int TEXT_COLOR = -12566464;


    protected ImageBackedScreen(Component title, Identifier sprite, int bgWidth, int bgHeight, int imageSize) {
        super(title);
        this.sprite = sprite;
        this.bgWidth = bgWidth;
        this.bgHeight = bgHeight;
        this.imageSize = imageSize; // Sprites should be square powers of two iirc
    }

    @Override
    protected void init() {
        super.init();

        this.guiX = (this.width / 2) - (this.bgWidth / 2);
        this.guiY = (this.height / 2) - (this.bgHeight / 2);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, this.guiX, this.guiY, this.imageSize, this.imageSize);
        graphics.text(this.font, this.getTitle(), this.guiX + (this.bgWidth / 2) - (font.width(this.title) / 2), this.guiY + 6, TEXT_COLOR, false);
    }

}
