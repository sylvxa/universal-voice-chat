package lol.sylvie.universalvc.screen.util;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.text.NumberFormat;

public class SimpleSlider extends AbstractSliderButton {
    private final String key;
    private final double min;
    private final double max;
    private final NumberFormat formatter;
    public SimpleSlider(int width, int height, String key, double min, double max, double initialValue, NumberFormat formatter) {
        super(0, 0, width, height, Component.empty(), (initialValue - min) / (max - min));
        this.key = key;
        this.min = min;
        this.max = max;
        this.formatter = formatter;
        this.updateMessage();
    }

    public SimpleSlider(int width, int height, String key, double min, double max, double initialValue) {
        this(width, height, key, min, max, initialValue, NumberFormat.getPercentInstance());
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.translatable(key, this.formatter.format(this.formatter.equals(NumberFormat.getPercentInstance()) ? this.getRealValue()  / 100 : this.getRealValue())));
    }

    @Override
    protected void applyValue() {}

    protected double convert(double value) {
        return ((max - min) * value) + min;
    }

    public float getRealValue() {
        return (float) convert(this.value);
    }
}
