package lol.sylvie.universalvc.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public class VoiceIndicatorLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public VoiceIndicatorLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector node, int lightCoords, AvatarRenderState state, float yRot, float xRot) {

    }
}
