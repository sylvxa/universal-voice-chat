package lol.sylvie.universalvc.screen.settings;

import lol.sylvie.universalvc.util.DistanceTracker;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;

public class LobbyConfigurationScreen extends AbstractConfigurationScreen {
    protected LobbyConfigurationScreen() {
        super(Component.translatable("menu.uvc.config.long"));
    }

    private SimpleSlider range;

    @Override
    protected void addSettings(LinearLayout settings, int width) {
        this.range = new SimpleSlider(width, 20, "menu.uvc.config.range", 4, 128, DistanceTracker.maxRange, new DecimalFormat("0 blocks"));
    }

    @Override
    protected void saveAndExit() {

    }
}
