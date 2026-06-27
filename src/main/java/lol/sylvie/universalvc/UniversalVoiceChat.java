package lol.sylvie.universalvc;

import lol.sylvie.universalvc.command.VoiceChatCommand;
import lol.sylvie.universalvc.screen.settings.ModSettings;
import lol.sylvie.universalvc.sdk.DiscordHandler;
import lol.sylvie.universalvc.voice.AudioFader;
import lol.sylvie.universalvc.util.NativeHelper;
import lol.sylvie.universalvc.util.Result;
import lol.sylvie.universalvc.voice.LobbyHandler;
import lol.sylvie.universalvc.voice.VoiceKeybinds;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class UniversalVoiceChat implements ClientModInitializer {
	public static final String MOD_ID = "universalvc";
	public static final String MOD_NAME = "Universal Voice Chat";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static DiscordHandler DISCORD_HANDLER;
	public static ModSettings MOD_SETTINGS;

	public static boolean initDiscord(Consumer<Result> callback) {
		// Parse app ID
		if (MOD_SETTINGS.applicationId.isEmpty() || !ModSettings.isLong(MOD_SETTINGS.applicationId)) {
			return false;
		}

		if (DISCORD_HANDLER != null) {
			DISCORD_HANDLER.stop(() -> {
				initDiscord(callback);
			});
			DISCORD_HANDLER = null;
			return true;
		}

		// Run
		DISCORD_HANDLER = new DiscordHandler(Long.parseLong(MOD_SETTINGS.applicationId), callback);
        new Thread(() -> {
            DISCORD_HANDLER.run();
        }).start();
		return true;
	}

	@Override
	public void onInitializeClient() {
		NativeHelper.load();
		MOD_SETTINGS = ModSettings.load();

		AudioFader.init();
		VoiceChatCommand.init();
		VoiceKeybinds.init();

		ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> {
            if (DISCORD_HANDLER != null) {
                if (LobbyHandler.call != null) {
                    LobbyHandler.leave((_) -> {
                        DISCORD_HANDLER.stop(() -> {});
                    });
                } else {
                    DISCORD_HANDLER.stop(() -> {});
                }
            }
        });
	}

	public static boolean isUnavailable() {
		return DISCORD_HANDLER == null || !DISCORD_HANDLER.isReady();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}