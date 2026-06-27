package lol.sylvie.universalvc.screen.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lol.sylvie.universalvc.UniversalVoiceChat;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class ModSettings {
    public int hearingRange = 24;

    public String applicationId = "";
    public boolean renderOverlay = true;
    public boolean overlayOnlyInMenus = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File file = FabricLoader.getInstance().getConfigDir().resolve(UniversalVoiceChat.MOD_ID + ".json").toFile();
    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ModSettings load() {
        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, ModSettings.class);
        } catch (IOException | JsonSyntaxException e) {
            return new ModSettings();
        }
    }

    public static boolean isLong(String string) {
        try {
            Long.parseLong(string.strip());
            return true;
        } catch (NumberFormatException e) {}
        return false;
    }
}
