package lol.sylvie.universalvc.voice;

import com.discord.Discord_VoiceStateHandle;
import com.mojang.authlib.GameProfile;
import lol.sylvie.universalvc.UniversalVoiceChat;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.discord.cdiscord_h.*;

public class VoiceParticipant {
    private final GameProfile profile;
    private final MemorySegment handle;
    private final long userId;
    private boolean speaking;
    private boolean muted;
    private boolean deafened;

    public VoiceParticipant(GameProfile profile, MemorySegment handle, long userId) {
        this.profile = profile;
        this.handle = handle;
        this.userId = userId;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public MemorySegment getHandle() {
        return handle;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public void setSpeaking(boolean speaking) {
        this.speaking = speaking;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isDeafened() {
        return deafened;
    }

    public void setDeafened(boolean deafened) {
        this.deafened = deafened;
    }

    public void updateVoiceState(MemorySegment call) {
        Arena arena = UniversalVoiceChat.DISCORD_HANDLER.getArena();

        MemorySegment voiceState = Discord_VoiceStateHandle.allocate(arena);
        if (Discord_Call_GetVoiceStateHandle(call, userId, voiceState)) {
            this.setMuted(Discord_VoiceStateHandle_SelfMute(voiceState));
            this.setDeafened(Discord_VoiceStateHandle_SelfDeaf(voiceState));

            Discord_VoiceStateHandle_Drop(voiceState);
        }
    }
}
