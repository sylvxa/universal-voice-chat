package lol.sylvie.universalvc.sdk;

import com.discord.*;
import com.mojang.authlib.GameProfile;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.util.NativeHelper;
import lol.sylvie.universalvc.util.Result;
import net.minecraft.client.Minecraft;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.discord.cdiscord_h.*;

public class DiscordHandler {
    private final long applicationId;
    private long userId;
    private final Arena arena;
    public final AtomicBoolean running = new AtomicBoolean(false);
    public final AtomicBoolean ready = new AtomicBoolean(false);

    private final Consumer<Result> loadingCallback;
    private Runnable closeCallback;

    public DiscordHandler(long applicationId, Consumer<Result> loadingCallback) {
        this.applicationId = applicationId;
        this.arena = Arena.ofShared();
        this.loadingCallback = loadingCallback;
    }

    private MemorySegment client;

    public boolean isReady() {
        return ready.get();
    }

    public Arena getArena() {
        return arena;
    }

    public MemorySegment getClient() {
        return client;
    }

    public long getUserId() {
        return userId;
    }

    protected void authenticate() {
        UniversalVoiceChat.LOGGER.info("Starting authentication");
        MemorySegment verifier = Discord_AuthorizationCodeVerifier.allocate(arena);
        Discord_Client_CreateAuthorizationCodeVerifier(client, verifier);

        MemorySegment args = arena.allocate(Discord_AuthorizationArgs.layout());
        Discord_AuthorizationArgs_Init(args);
        Discord_AuthorizationArgs_SetClientId(args, this.applicationId);

        MemorySegment segment = Discord_String.allocate(arena);
        Discord_Client_GetDefaultCommunicationScopes(segment);
        Discord_AuthorizationArgs_SetScopes(args, segment);

        MemorySegment challenge = Discord_AuthorizationCodeChallenge.allocate(arena);
        Discord_AuthorizationCodeVerifier_Challenge(verifier, challenge);
        Discord_AuthorizationArgs_SetCodeChallenge(args, challenge);

        Discord_Client_AuthorizationCallback.Function authCallback = (authResultRaw, code, redirectUri, _) -> {
            Result authResult = Result.fromDiscord(arena, authResultRaw, null);
            if (!authResult.success()) {
                loadingCallback.accept(authResult);
                return;
            }

            UniversalVoiceChat.LOGGER.info("Exchanging token");
            Discord_Client_TokenExchangeCallback.Function tokenCallback = (tokenResultRaw, accessToken, _, _, _, _, _) -> {
                Result tokenResult = Result.fromDiscord(arena, tokenResultRaw, null);
                if (!tokenResult.success()) {
                    loadingCallback.accept(tokenResult);
                    return;
                }

                UniversalVoiceChat.LOGGER.info("Updating with token");

                Discord_Client_UpdateTokenCallback.Function updateCallback = (updateResultRaw, _) -> {
                    Result updateResult = Result.fromDiscord(arena, updateResultRaw, null);
                    if (!updateResult.success()) {
                        loadingCallback.accept(updateResult);
                        return;
                    }

                    UniversalVoiceChat.LOGGER.info("Connecting to Discord");
                    Discord_Client_Connect(client);
                };

                MemorySegment updateCallbackStub = Discord_Client_UpdateTokenCallback.allocate(updateCallback, arena);
                Discord_Client_UpdateToken(client, Discord_AuthorizationTokenType_Bearer(), accessToken, updateCallbackStub, MemorySegment.NULL, MemorySegment.NULL);
            };
            MemorySegment tokenCallbackStub = Discord_Client_TokenExchangeCallback.allocate(tokenCallback, arena);

            MemorySegment verifierString = Discord_String.allocate(arena);
            Discord_AuthorizationCodeVerifier_Verifier(verifier, verifierString);
            Discord_Client_GetToken(client, this.applicationId, code, verifierString, redirectUri, tokenCallbackStub, MemorySegment.NULL, MemorySegment.NULL);

        };

        MemorySegment authCallbackStub = Discord_Client_AuthorizationCallback.allocate(authCallback, arena);
        Discord_Client_Authorize(client, args, authCallbackStub, MemorySegment.NULL, MemorySegment.NULL);
    }

    @SuppressWarnings("BusyWait")
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.running.set(false)));

        this.client = arena.allocate(Discord_Client.layout());
        Discord_Client_Init(client);

        // Logging
        Discord_Client_LogCallback.Function logger = new LoggingHandler();
        MemorySegment loggerStub = Discord_Client_LogCallback.allocate(logger, arena);
        Discord_Client_AddLogCallback(client, loggerStub, MemorySegment.NULL, MemorySegment.NULL, Discord_LoggingSeverity_Verbose());

        // Setup callbacks
        Discord_Client_OnStatusChanged.Function statusCallback = (status, error, _, _) -> {
            MemorySegment statusStrSeg = arena.allocate(Discord_String.layout());
            Discord_Client_StatusToString(status, statusStrSeg);

            if (status == Discord_Client_Status_Ready()) {
                MemorySegment handle = Discord_UserHandle.allocate(arena);
                Discord_Client_GetCurrentUser(client, handle);
                userId = Discord_UserHandle_Id(handle);

                this.ready.set(true);
                loadingCallback.accept(new Result(true, null));
            } else if (error != Discord_Client_Error_None()) {
                MemorySegment errorStrSeg = arena.allocate(Discord_String.layout());
                Discord_Client_ErrorToString(error, errorStrSeg);
                String errorStr = NativeHelper.readDiscordString(errorStrSeg);

                loadingCallback.accept(new Result(false, errorStr));
            }
        };
        MemorySegment statusStub = Discord_Client_OnStatusChanged.allocate(statusCallback, arena);
        Discord_Client_SetStatusChangedCallback(client, statusStub, MemorySegment.NULL, MemorySegment.NULL);

        // Call out to Discord
        this.authenticate();

        // Main loop
        Discord_RunCallbacks runCallbacks = Discord_RunCallbacks.makeInvoker();
        running.set(true);
        while (running.get()) {
            try {
                Thread.sleep(10);
                runCallbacks.apply();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (this.closeCallback != null) {
            this.closeCallback.run();
        } else {
            stop(null); // stopped unexpectedly
        }

    }

    public void stop(Runnable onClose) {
        this.closeCallback = () -> {
            try {
                arena.close();
                onClose.run();
            } catch (IllegalStateException ignored) {}
        };
        this.ready.set(false);
        this.running.set(false);
    }

    public void createOrJoinLobby(String secret, Discord_Client_CreateOrJoinLobbyCallback.Function callback) {
        GameProfile profile = Minecraft.getInstance().getGameProfile();
        MemorySegment userProperties = NativeHelper.writeDiscordProperties(Map.of(
                "name", profile.name(),
                "uuid", profile.id().toString()
        ), arena);

        MemorySegment lobbyProperties = NativeHelper.writeDiscordProperties(Map.of(
                //"range", String.valueOf(DistanceTracker.maxRange)
        ), arena);


        Discord_Client_CreateOrJoinLobbyWithMetadata(this.client, NativeHelper.writeDiscordString(secret, arena), lobbyProperties, userProperties, Discord_Client_CreateOrJoinLobbyCallback.allocate(callback, arena), MemorySegment.NULL, MemorySegment.NULL);
    }

    public void leaveLobby(long id, Discord_Client_LeaveLobbyCallback.Function callback) {
        Discord_Client_LeaveLobby(this.client, id, Discord_Client_LeaveLobbyCallback.allocate(callback, arena), MemorySegment.NULL, MemorySegment.NULL);
    }

    public CompletableFuture<List<AudioDevice>> getInputs() {
        CompletableFuture<List<AudioDevice>> future = new CompletableFuture<>();

        Discord_Client_GetInputDevices(client, Discord_Client_GetInputDevicesCallback.allocate((devices, _) -> future.complete(NativeHelper.readAudioDevices(devices, arena)), arena), MemorySegment.NULL, MemorySegment.NULL);

        return future;
    }

    public CompletableFuture<String> getCurrentInput() {
        CompletableFuture<String> future = new CompletableFuture<>();

        Discord_Client_GetCurrentInputDevice(client, Discord_Client_GetCurrentInputDeviceCallback.allocate((device, _) -> {
            MemorySegment id = Discord_String.allocate(arena);
            Discord_AudioDevice_Id(device, id);
            future.complete(NativeHelper.readDiscordString(id));
        }, arena), MemorySegment.NULL, MemorySegment.NULL);

        return future;
    }

    public void setCurrentInput(AudioDevice device) {
        Discord_Client_SetInputDevice(client, NativeHelper.writeDiscordString(device.id(), arena), Discord_Client_SetInputDeviceCallback.allocate((_, _) -> {}, arena), MemorySegment.NULL, MemorySegment.NULL);
    }

    public void setInputVolume(float volume) {
        Discord_Client_SetInputVolume(client, volume);
    }

    public float getInputVolume() {
        return Discord_Client_GetInputVolume(client);
    }

    public CompletableFuture<List<AudioDevice>> getOutputs() {
        CompletableFuture<List<AudioDevice>> future = new CompletableFuture<>();

        Discord_Client_GetOutputDevices(client, Discord_Client_GetOutputDevicesCallback.allocate((devices, _) -> future.complete(NativeHelper.readAudioDevices(devices, arena)), arena), MemorySegment.NULL, MemorySegment.NULL);

        return future;
    }

    public CompletableFuture<String> getCurrentOutput() {
        CompletableFuture<String> future = new CompletableFuture<>();

        Discord_Client_GetCurrentOutputDevice(client, Discord_Client_GetCurrentOutputDeviceCallback.allocate((device, _) -> {
            MemorySegment id = Discord_String.allocate(arena);
            Discord_AudioDevice_Id(device, id);
            future.complete(NativeHelper.readDiscordString(id));
        }, arena), MemorySegment.NULL, MemorySegment.NULL);

        return future;
    }

    public void setCurrentOutput(AudioDevice device) {
        Discord_Client_SetOutputDevice(client, NativeHelper.writeDiscordString(device.id(), arena), Discord_Client_SetOutputDeviceCallback.allocate((_, _) -> {}, arena), MemorySegment.NULL, MemorySegment.NULL);
    }

    public void setOutputVolume(float volume) {
        Discord_Client_SetOutputVolume(client, volume);
    }

    public float getOutputVolume() {
        return Discord_Client_GetOutputVolume(client);
    }

    private NoiseDampenMode noiseDampen = NoiseDampenMode.WEBRTC;
    public NoiseDampenMode getNoiseDampenMode() {
        return noiseDampen;
    }

    public void setNoiseDampening(NoiseDampenMode mode) {
        noiseDampen = mode;
        Discord_Client_SetNoiseCancellation(client, mode == NoiseDampenMode.KRISP);
        boolean webRtc = mode == NoiseDampenMode.WEBRTC;
        Discord_Client_SetNoiseSuppression(client, webRtc);
        Discord_Client_SetEchoCancellation(client, webRtc);
    }
}
