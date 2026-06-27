package lol.sylvie.universalvc.screen.quick;

import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.util.ModIcons;
import lol.sylvie.universalvc.voice.LobbyHandler;
import lol.sylvie.universalvc.voice.VoiceParticipant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        for (VoiceParticipant participant : sortedParticipants) {
            boolean isInGame = connection == null || connection.getOnlinePlayerIds().contains(participant.getProfile().id());
            PlayerFaceExtractor.extractRenderState(graphics, participant.getSkin(), x, y, SKIN_SIZE, participant.isSpeaking() && isInGame ? 0xFFFFFFFF : 0xFF909090);
            if (!isInGame) {
                graphics.blit(Identifier.withDefaultNamespace("textures/item/barrier.png"), x, y, x + SKIN_SIZE, y + SKIN_SIZE, 0f, 1f, 0f, 1f);
            }

            if (participant.isDeafened() || participant.isMuted()) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, participant.isDeafened() ? ModIcons.DEAFENED : ModIcons.MUTED, x + SKIN_SIZE - ICON_SIZE + 2, y + SKIN_SIZE - ICON_SIZE + 2, 16, 16);
            }

            y += SKIN_SIZE + MARGIN;
        }
    }
}
