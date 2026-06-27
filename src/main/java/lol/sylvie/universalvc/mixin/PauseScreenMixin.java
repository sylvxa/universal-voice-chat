package lol.sylvie.universalvc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import lol.sylvie.universalvc.util.ModIcons;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getSingleplayerServer()Lnet/minecraft/client/server/IntegratedServer;"))
    public void createPauseMenu(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout row) {
        SpriteIconButton button = ModIcons.getMenuButton(this);
        row.addChild(button);
    }
}
