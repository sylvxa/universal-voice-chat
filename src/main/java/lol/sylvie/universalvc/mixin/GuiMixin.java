package lol.sylvie.universalvc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.screen.VoiceChatOverlay;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;zone(Ljava/lang/String;)Lnet/minecraft/util/profiling/Zone;"), method = "extractRenderState")
	private void uvc$addOverlay(CallbackInfo info, @Local(name = "graphics") GuiGraphicsExtractor graphics) {
        if (!UniversalVoiceChat.isUnavailable()) VoiceChatOverlay.extractRenderState(graphics);
	}
}