package lol.sylvie.universalvc.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.screen.quick.QuickMenuScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Shadow
    protected abstract int getHorizontalPosition(int currentButton, int numberOfButtons, int buttonWidth);

    @WrapMethod(method = "getHorizontalPosition")
    public int uvc$addRoomForButton(int currentButton, int numberOfButtons, int buttonWidth, Operation<Integer> original) {
        return original.call(currentButton, numberOfButtons + 1, buttonWidth);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/SpriteIconButton;setPosition(II)V", ordinal = 1))
    public void uvc$addVcButton(CallbackInfo ci, @Local(name = "currentButton") int currentButton, @Local(name = "topPos") int topPos) {
        SpriteIconButton button = SpriteIconButton.builder(Component.translatable("uvc.name.short"), _ -> QuickMenuScreen.tryOpen(this), true)
                .sprite(UniversalVoiceChat.id("status/unmuted"), 16, 16)
                .size(20, 20)
                .build();
        ++currentButton;
        button.setPosition(getHorizontalPosition(currentButton, 3, 20), topPos);
        addRenderableWidget(button);
    }
}
