package lol.sylvie.universalvc.screen;

import com.mojang.authlib.GameProfile;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.util.DistanceTracker;
import lol.sylvie.universalvc.util.ModIcons;
import lol.sylvie.universalvc.voice.LobbyHandler;
import lol.sylvie.universalvc.voice.VoiceParticipant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VoiceChatOverlay {
    private static final int MARGIN = 4;
    private static final int SKIN_SIZE = 24;
    private static final int ICON_SIZE = 16;

    public static void extractRenderState(final GuiGraphicsExtractor graphics) {
        if (!UniversalVoiceChat.MOD_SETTINGS.renderOverlay || (UniversalVoiceChat.MOD_SETTINGS.overlayOnlyInMenus && Minecraft.getInstance().level != null)) return;

        int x = graphics.guiWidth() - SKIN_SIZE - MARGIN;
        int y = MARGIN;
        List<VoiceParticipant> sortedParticipants = LobbyHandler.participantMap.entrySet().stream()
                .sorted(Comparator.comparing((v) -> v.getValue().getProfile().name()))
                .map(Map.Entry::getValue)
                .toList();
        for (VoiceParticipant participant : sortedParticipants) {
            PlayerSkinRenderCache skinCache = Minecraft.getInstance().playerSkinRenderCache();
            PlayerSkinRenderCache.RenderInfo renderInfo = skinCache.getOrDefault(ResolvableProfile.createUnresolved(participant.getProfile().id()));
            PlayerFaceExtractor.extractRenderState(graphics, renderInfo.playerSkin(), x, y, SKIN_SIZE, participant.isSpeaking() ? 0xFFFFFFFF : 0xFF909090);

            if (participant.isDeafened() || participant.isMuted()) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, participant.isDeafened() ? ModIcons.DEAFENED : ModIcons.MUTED, x + SKIN_SIZE - ICON_SIZE + 2, y + SKIN_SIZE - ICON_SIZE + 2, 16, 16);
            }

            y += SKIN_SIZE + MARGIN;
        }
    }
}
