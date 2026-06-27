package lol.sylvie.universalvc.screen.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;

public class PlayerNameWidget extends AbstractWidget {
    private final GameProfile profile;
    private final ResolvableProfile resolvable;
    private final Font font;
    public PlayerNameWidget(int x, int y, int width, int height, GameProfile profile, Font font) {
        super(x, y, width, height, Component.literal(profile.name()));
        this.profile = profile;
        this.resolvable = ResolvableProfile.createUnresolved(profile.id());
        this.font = font;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int size = this.getHeight();
        PlayerFaceExtractor.extractRenderState(graphics, this.resolvable, this.getX(), this.getY(), size);
        graphics.text(font, this.profile.name(), this.getX() + size + 4, this.getY() + ((size / 2) - (font.lineHeight / 2)), 0xFFFFFFFF, true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.profile.name());
    }
}
