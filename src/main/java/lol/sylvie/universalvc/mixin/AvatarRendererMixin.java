package lol.sylvie.universalvc.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.util.ModIcons;
import lol.sylvie.universalvc.voice.LobbyHandler;
import lol.sylvie.universalvc.voice.VoiceParticipant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
    @Unique
    private static final int ICON_SIZE = 8;

    private static void uvc$textureVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v) {
        buffer.addVertex(pose.pose(), x, y, 0)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(255)
                .setNormal(pose, 0F, 0F, -1F);
    }

    @Inject(
            method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("TAIL"))
    public void uvc$submitVoiceIndicator(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        Vec3 nameTagAttachment = state.nameTagAttachment;
        if (nameTagAttachment == null || UniversalVoiceChat.MOD_SETTINGS.nametagIndicators) return;

        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.level.getEntity(state.id);
        if (!(entity instanceof AbstractClientPlayer player)) return;
        VoiceParticipant participant = LobbyHandler.minecraftIdMap.get(player.getUUID());
        if (participant == null) return;

        Identifier texture = participant.isDeafened() ? ModIcons.DEAFENED :
                             participant.isMuted() ? ModIcons.MUTED :
                             participant.isSpeaking() ? ModIcons.UNMUTED :
                             null;
        if (texture == null) return;

        TextureAtlas guiAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
        TextureAtlasSprite sprite = guiAtlas.getSprite(texture);

        poseStack.pushPose();

        poseStack.translate(nameTagAttachment.x, nameTagAttachment.y + (double) 0.75F, nameTagAttachment.z);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(0.025F, -0.025F, 0.025F);

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(sprite.atlasLocation()), (pose, buffer) -> {
            uvc$textureVertex(buffer, pose, -ICON_SIZE, ICON_SIZE, sprite.getU0(), sprite.getV1());
            uvc$textureVertex(buffer, pose, ICON_SIZE, ICON_SIZE, sprite.getU1(), sprite.getV1());
            uvc$textureVertex(buffer, pose, ICON_SIZE, -ICON_SIZE, sprite.getU1(), sprite.getV0());
            uvc$textureVertex(buffer, pose, -ICON_SIZE, -ICON_SIZE, sprite.getU0(), sprite.getV0());
        });

        poseStack.popPose();
    }
}
