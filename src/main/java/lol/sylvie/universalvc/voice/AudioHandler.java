package lol.sylvie.universalvc.voice;

import com.discord.Discord_Client_UserAudioReceivedCallback;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static lol.sylvie.universalvc.voice.LobbyHandler.discordIdMap;

public class AudioHandler implements Discord_Client_UserAudioReceivedCallback.Function {
    @Override
    public void apply(long userId, MemorySegment data, long samplesPerChannel, int sampleRate, long channels, MemorySegment outShouldMute, MemorySegment userData) {
        if (Minecraft.getInstance().getConnection() == null) {
            return;
        }

        if (channels != 2) return;

        long totalSamples = samplesPerChannel * channels;
        MemorySegment audioBuffer = data.reinterpret(totalSamples * ValueLayout.JAVA_SHORT.byteSize());

        VoiceParticipant participant = discordIdMap.get(userId);
        if (participant == null) return;
        GameProfile gameProfile = participant.getProfile();
        AudioFader.Data panData = AudioFader.distances.get(gameProfile.id());
        if (panData == null) return;

        double leftPan = panData.smoothLeft += (panData.leftPan.get() - panData.smoothLeft) * 0.05f;
        double rightPan = panData.smoothRight += (panData.rightPan.get() - panData.smoothRight) * 0.05f;

        if ((leftPan == 0 && rightPan == 0)) {
            outShouldMute.reinterpret(ValueLayout.JAVA_BOOLEAN.byteSize()).set(ValueLayout.JAVA_BOOLEAN, 0, true);
            return;
        }

        for (long i = 0; i < totalSamples; i += 2) {
            short sampleLeft = audioBuffer.getAtIndex(ValueLayout.JAVA_SHORT, i);
            short sampleRight = audioBuffer.getAtIndex(ValueLayout.JAVA_SHORT, i + 1);
            audioBuffer.setAtIndex(ValueLayout.JAVA_SHORT, i, (short)(sampleLeft * leftPan));
            audioBuffer.setAtIndex(ValueLayout.JAVA_SHORT, i + 1, (short)(sampleRight * rightPan));
        }
    }
}
