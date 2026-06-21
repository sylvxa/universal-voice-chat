package lol.sylvie.universalvc.screen.settings;

import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.screen.quick.QuickMenuScreen;
import lol.sylvie.universalvc.sdk.AudioDevice;
import lol.sylvie.universalvc.sdk.DiscordHandler;
import lol.sylvie.universalvc.sdk.NoiseDampenMode;
import lol.sylvie.universalvc.util.DistanceTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VoiceSettingsScreen extends AbstractConfigurationScreen {
    private List<AudioDevice> inputs;
    private String currentInput;
    private List<AudioDevice> outputs;
    private String currentOutput;
    protected VoiceSettingsScreen(List<AudioDevice> inputs, String currentInput, List<AudioDevice> outputs, String currentOutput) {
        super(Component.translatable("menu.uvc.settings.long"));
        this.inputs = inputs;
        this.currentInput = currentInput;
        this.outputs = outputs;
        this.currentOutput = currentOutput;
    }

    public static void open() {
        if (UniversalVoiceChat.isUnavailable()) {
            return;
        }

        CompletableFuture<List<AudioDevice>> inputs = UniversalVoiceChat.DISCORD_HANDLER.getInputs();
        CompletableFuture<String> currentInput = UniversalVoiceChat.DISCORD_HANDLER.getCurrentInput();
        CompletableFuture<List<AudioDevice>> outputs = UniversalVoiceChat.DISCORD_HANDLER.getOutputs();
        CompletableFuture<String> currentOutput = UniversalVoiceChat.DISCORD_HANDLER.getCurrentOutput();


        CompletableFuture.allOf(inputs, currentInput, outputs, currentOutput).join();

        try {
            Minecraft.getInstance().gui.setScreen(new VoiceSettingsScreen(inputs.get(), currentInput.get(), outputs.get(), currentOutput.get()));
        } catch (InterruptedException | ExecutionException e) {
            Minecraft.getInstance().gui.setScreen(new ErrorScreen(Component.literal("evil uvc error :("), Component.literal(e.toString())));
            UniversalVoiceChat.LOGGER.error(e);
        }
    }

    private AudioDevice search(String id, List<AudioDevice> devices) {
        return devices.stream().filter(d -> d.id().equals(id)).findAny().orElseThrow();
    }

    private CycleButton<AudioDevice> inputCycler;
    private SimpleSlider inputVolume;
    private CycleButton<AudioDevice> outputCycler;
    private SimpleSlider outputVolume;
    private CycleButton<NoiseDampenMode> noiseDampen;
    private SimpleSlider range;

    private CycleButton<Boolean> overlayToggle;
    private CycleButton<Boolean> inGameToggle;
    private EditBox appId;

    private boolean closing = false;
    protected void addSettings(LinearLayout settings, int width) {
        addHeader(settings, Component.translatable("menu.uvc.settings.audio"));

        this.inputCycler = CycleButton.builder(AudioDevice::asComponent, search(currentInput, inputs))
                .withValues(inputs)
                .create(0, 0, width, 20, Component.translatable("menu.uvc.setting.input"));
        settings.addChild(this.inputCycler);

        this.inputVolume = new SimpleSlider(width, 20, "menu.uvc.setting.input_volume", 0, 200, UniversalVoiceChat.DISCORD_HANDLER.getInputVolume());
        settings.addChild(this.inputVolume);

        this.outputCycler = CycleButton.builder(AudioDevice::asComponent, search(currentOutput, outputs))
                .withValues(outputs)
                .create(0, 0, width, 20, Component.translatable("menu.uvc.setting.output"));
        settings.addChild(this.outputCycler);

        this.outputVolume = new SimpleSlider(width, 20, "menu.uvc.setting.output_volume", 0, 200, UniversalVoiceChat.DISCORD_HANDLER.getOutputVolume());
        settings.addChild(this.outputVolume);

        this.noiseDampen = CycleButton.builder(NoiseDampenMode::asComponent, UniversalVoiceChat.DISCORD_HANDLER.getNoiseDampenMode())
                .withValues(NoiseDampenMode.values())
                .create(0, 0, width, 20, Component.translatable("menu.uvc.setting.noise_dampening"));
        settings.addChild(this.noiseDampen);

        this.range = new SimpleSlider(width, 20, "menu.uvc.config.range", 4, 128, DistanceTracker.maxRange, new DecimalFormat("0 blocks"));
        settings.addChild(this.range);

        addHeader(settings, Component.translatable("menu.uvc.settings.visual"));

        this.overlayToggle = CycleButton.booleanBuilder(CommonComponents.OPTION_ON, CommonComponents.OPTION_OFF, UniversalVoiceChat.MOD_SETTINGS.renderOverlay).create(
                Component.translatable("menu.uvc.settings.overlay"),
                (_, value) -> {
                    this.inGameToggle.active = value;
                }
        );
        this.overlayToggle.setWidth(width);
        settings.addChild(this.overlayToggle);

        this.inGameToggle = CycleButton.booleanBuilder(CommonComponents.OPTION_ON, CommonComponents.OPTION_OFF, UniversalVoiceChat.MOD_SETTINGS.overlayOnlyInMenus).create(
                Component.translatable("menu.uvc.settings.hide_in_game"),
                (_, _) -> {}
        );
        this.inGameToggle.setWidth(width);
        this.inGameToggle.active = UniversalVoiceChat.MOD_SETTINGS.renderOverlay;
        settings.addChild(this.inGameToggle);

        addHeader(settings, Component.translatable("menu.uvc.settings.discord"));

        settings.addChild(new StringWidget(Component.translatable("menu.uvc.settings.application_id"), font));

        Component appIdText = Component.translatable("menu.uvc.settings.application_id");
        this.appId = new EditBox(font, 0, 0, width, 13, appIdText);
        this.appId.setValue(UniversalVoiceChat.MOD_SETTINGS.applicationId);
        this.appId.setHint(appIdText);

        settings.addChild(this.appId);
    }

    protected void saveAndExit() {
        if (closing) return;
        DiscordHandler handler = UniversalVoiceChat.DISCORD_HANDLER;
        handler.setCurrentInput(this.inputCycler.getValue());
        handler.setInputVolume(this.inputVolume.getRealValue());
        handler.setCurrentOutput(this.outputCycler.getValue());
        handler.setOutputVolume(this.outputVolume.getRealValue());
        handler.setNoiseDampening(this.noiseDampen.getValue());

        DistanceTracker.maxRange = (int) this.range.getRealValue();

        ModSettings settings = UniversalVoiceChat.MOD_SETTINGS;

        settings.renderOverlay = overlayToggle.getValue();
        settings.overlayOnlyInMenus = inGameToggle.getValue();

        String newId = this.appId.getValue().strip();
        if (!newId.equals(settings.applicationId) && ModSettings.isLong(newId)) {
            settings.applicationId = newId;

            UniversalVoiceChat.DISCORD_HANDLER.stop(() -> {
                Minecraft.getInstance().execute(() -> QuickMenuScreen.tryOpen(null));
            });
            UniversalVoiceChat.DISCORD_HANDLER = null;

            closing = true;
        } else {
            this.onClose();
        }

        settings.save();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(new QuickMenuScreen());
    }
}
