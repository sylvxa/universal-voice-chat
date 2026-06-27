
package lol.sylvie.universalvc.voice;

import com.discord.*;
import com.mojang.authlib.GameProfile;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.screen.quick.QuickMenuScreen;
import lol.sylvie.universalvc.sdk.DiscordHandler;
import lol.sylvie.universalvc.util.NativeHelper;
import lol.sylvie.universalvc.util.Result;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

import java.lang.foreign.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.discord.cdiscord_h.*;

public class LobbyHandler {
    private static Long id;
    public static String secret;

    public static MemorySegment call;
    private static MemorySegment lobbyHandle;

    public static Map<Long, VoiceParticipant> participantMap = new HashMap<>();

    private static void addMember(MemorySegment member) {
        Arena arena = UniversalVoiceChat.DISCORD_HANDLER.getArena();
        MemorySegment memberMeta = arena.allocate(Discord_Properties.layout());
        Discord_LobbyMemberHandle_Metadata(member, memberMeta);

        long userId = Discord_LobbyMemberHandle_Id(member);
        Map<String, String> metadata = NativeHelper.readDiscordProperties(memberMeta);

        GameProfile profile = new GameProfile(UUID.fromString(metadata.get("uuid")), metadata.get("name"));
        VoiceParticipant participant = new VoiceParticipant(profile, member, userId);
        participantMap.put(userId, participant);

        participant.updateVoiceState(call);
    }

    public static void createOrJoin(String secret, Consumer<Result> callback) {
        DiscordHandler discord = UniversalVoiceChat.DISCORD_HANDLER;
        discord.createOrJoinLobby(secret, (resultMem, lobbyId, userData) -> {
            Arena arena = discord.getArena();
            Result result = Result.fromDiscord(arena, resultMem, secret);
            Minecraft minecraft = Minecraft.getInstance();
            if (result.success()) {
                LobbyHandler.id = lobbyId;
                LobbyHandler.secret = secret;
                LobbyHandler.call = Discord_Call.allocate(arena);

                // Fade audio
                Discord_Client_UserAudioReceivedCallback.Function modifyAudio = new AudioHandler();

                MemorySegment client = discord.getClient();
                MemorySegment receiveCallback = Discord_Client_UserAudioReceivedCallback.allocate(modifyAudio, arena);
                Discord_Client_StartCallWithAudioCallbacks(
                        client,
                        lobbyId,
                        receiveCallback,
                        MemorySegment.NULL,
                        MemorySegment.NULL,
                        Discord_Client_UserAudioCapturedCallback.allocate((_, _, _, _, _) -> {}, arena),
                        MemorySegment.NULL,
                        MemorySegment.NULL,
                        call);

                if (LobbyHandler.call.equals(MemorySegment.NULL)) {
                    // Already in call
                    Discord_Client_GetCall(client, lobbyId, call);
                }

                // Get participants
                lobbyHandle = Discord_LobbyHandle.allocate(arena);
                Discord_Client_GetLobbyHandle(client, lobbyId, lobbyHandle);

                MemorySegment span = arena.allocate(Discord_LobbyMemberHandleSpan.layout());
                Discord_LobbyHandle_LobbyMembers(lobbyHandle, span);

                long members = Discord_LobbyMemberHandleSpan.size(span);
                MemorySegment memberSpan = Discord_LobbyMemberHandleSpan.ptr(span).reinterpret(Discord_LobbyMemberHandle.layout().byteSize() * members);

                long memberSize = Discord_LobbyMemberHandle.layout().byteSize();
                for (long i = 0; i < members; i++) {
                    MemorySegment member = memberSpan.asSlice(i * memberSize, memberSize);
                    addMember(member);
                }

                // Callbacks
                Discord_Client_LobbyMemberAddedCallback.Function addedFunction = (_, memberId, _) -> {
                    MemorySegment member = Discord_LobbyMemberHandle.allocate(arena);
                    Discord_LobbyHandle_GetLobbyMemberHandle(lobbyHandle, memberId, member);
                    addMember(member);
                    QuickMenuScreen.refresh();
                };
                Discord_Client_SetLobbyMemberAddedCallback(client, Discord_Client_LobbyMemberAddedCallback.allocate(addedFunction, arena), MemorySegment.NULL, MemorySegment.NULL);

                Discord_Client_LobbyMemberRemovedCallback.Function removedFunction = (_, memberId, _) -> {
                    Discord_LobbyMemberHandle_Drop(participantMap.get(memberId).getHandle());
                    participantMap.remove(memberId);
                    QuickMenuScreen.refresh();
                };
                Discord_Client_SetLobbyMemberRemovedCallback(client, Discord_Client_LobbyMemberRemovedCallback.allocate(removedFunction, arena), MemorySegment.NULL, MemorySegment.NULL);

                // Voice state updates
                Discord_Call_SetSpeakingStatusChangedCallback(call, Discord_Call_OnSpeakingStatusChanged.allocate((userId, isPlayingSound, _) -> {
                    VoiceParticipant participant = participantMap.get(userId);
                    if (participant != null) participant.setSpeaking(isPlayingSound);
                }, arena), MemorySegment.NULL, MemorySegment.NULL);

                Discord_Call_SetOnVoiceStateChangedCallback(call, Discord_Call_OnVoiceStateChanged.allocate((userId, _) -> {
                    VoiceParticipant participant = participantMap.get(userId);
                    if (participant != null) participant.updateVoiceState(call);
                    if (userId == UniversalVoiceChat.DISCORD_HANDLER.getUserId())
                        QuickMenuScreen.refresh();

                }, arena), MemorySegment.NULL, MemorySegment.NULL);
            }

            minecraft.execute(() -> {
                ToastManager manager = minecraft.gui.toastManager();
                manager.addToast(result.success() ?
                        new SystemToast(SystemToast.SystemToastId.WORLD_BACKUP, Component.translatable("toast.uvc.join"), null) :
                        new SystemToast(SystemToast.SystemToastId.CHUNK_LOAD_FAILURE, Component.translatable("toast.uvc.join.error"), Component.literal(result.message())));
            });

            callback.accept(result);
        });
    }

    public static boolean isMuted() {
        return call != null && Discord_Call_GetSelfMute(call);
    }

    public static boolean isDeafened() {
        return call != null && Discord_Call_GetSelfDeaf(call);
    }


    public static boolean toggleMute() {
        if (call == null) return false;
        boolean newState = !isMuted();
        Discord_Call_SetSelfMute(call, newState);
        return newState;
    }

    public static boolean toggleDeafen() {
        if (call == null) return false;
        boolean newState = !isDeafened();
        Discord_Call_SetSelfDeaf(call, newState);
        return newState;
    }

    public static void cleanup() {
        id = null;
        secret = null;
        call = null;
        participantMap.clear();

        Discord_LobbyHandle_Drop(lobbyHandle);
        lobbyHandle = null;
    }

    public static void leave(Consumer<Result> callback) {
        DiscordHandler discord = UniversalVoiceChat.DISCORD_HANDLER;
        discord.leaveLobby(id, (resultMem, userData) -> {
            Result result = Result.fromDiscord(discord.getArena(), resultMem, null);
            if (result.success()) {
                cleanup();
            }
            callback.accept(result);
        });
    }
}
