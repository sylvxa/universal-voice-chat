package lol.sylvie.universalvc.util;

import com.discord.Discord_String;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.discord.cdiscord_h.Discord_ClientResult_Error;
import static com.discord.cdiscord_h.Discord_ClientResult_Successful;

public record Result(boolean success, String message) {
    public static Result fromDiscord(Arena arena, MemorySegment result, String success) {
        if (Discord_ClientResult_Successful(result)) {
            return new Result(true, success);
        } else {
            MemorySegment error = Discord_String.allocate(arena);
            Discord_ClientResult_Error(result, error);
             return new Result(false, NativeHelper.readDiscordString(error));
        }
    }
}
