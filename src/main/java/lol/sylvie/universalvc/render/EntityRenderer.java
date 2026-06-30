package lol.sylvie.universalvc.render;

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EntityTypes;

public class EntityRenderer {
    @SuppressWarnings("unchecked")
    public static void init() {
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (!(entityType.equals(EntityTypes.PLAYER))) return;
            registrationHelper.register(new VoiceIndicatorLayer((RenderLayerParent<AvatarRenderState, PlayerModel>) entityRenderer));
        });
    }
}
