package lol.sylvie.universalvc.sdk;

import com.discord.Discord_Client_LogCallback;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.util.NativeHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.foreign.MemorySegment;
import java.util.List;

public class LoggingHandler implements Discord_Client_LogCallback.Function {
    private static final List<Level> SEVERITIES = List.of(
            Level.DEBUG, // "Verbose",
            Level.INFO,  // "Info",
            Level.WARN,  // "Warning",
            Level.ERROR, // "Error",
            Level.OFF    // "None"
    );

    private static final Logger LOGGER = LogManager.getLogger(UniversalVoiceChat.MOD_NAME + " [Discord]");

    @Override
    public void apply(MemorySegment message, int severity, MemorySegment userData) {
        LOGGER.log(SEVERITIES.get(Math.clamp(severity - 1, 0, SEVERITIES.size() - 1)), NativeHelper.readDiscordString(message));
    }
}
