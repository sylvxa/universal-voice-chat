package lol.sylvie.universalvc.util;

import com.discord.Discord_AudioDevice;
import com.discord.Discord_AudioDeviceSpan;
import com.discord.Discord_Properties;
import com.discord.Discord_String;
import com.sun.jna.Platform;
import lol.sylvie.universalvc.UniversalVoiceChat;
import lol.sylvie.universalvc.sdk.AudioDevice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static com.discord.cdiscord_h.Discord_AudioDevice_Id;
import static com.discord.cdiscord_h.Discord_AudioDevice_Name;

public class NativeHelper {
    public static Path copyResourceDirToTemp(String resourcePath) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL url = classLoader.getResource(resourcePath);
            if (url == null) {
                throw new IOException("Resource directory not found on classpath: " + resourcePath);
            }

            Path tempDir = Files.createTempDirectory("uvc-");
            URI uri = url.toURI();

            if ("jar".equals(uri.getScheme())) {
                // Resource lives inside a JAR — mount it as a filesystem to walk it.
                try (FileSystem jarFs = FileSystems.newFileSystem(uri, Map.of())) {
                    Path source = jarFs.getPath(resourcePath);
                    copyDirectory(source, tempDir);
                }
            } else {
                // Resource lives on the regular filesystem (exploded classes dir).
                Path source = Path.of(uri);
                copyDirectory(source, tempDir);
            }

            return tempDir;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            paths.forEach(src -> {
                try {
                    Path relative = source.relativize(src);
                    Path dest = target.resolve(relative.toString());
                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.createDirectories(dest.getParent());
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private static List<String> getLibraryNames() {
        String arch = System.getProperty("os.arch");
        boolean arm = arch.equals("aarch64") || arch.equals("arm64");
        if (Platform.isWindows()) {
            String armPrefix = (arm ? "arm64/" : "");
            String sdkName = armPrefix + "discord_partner_sdk.dll";
            //String krispName = armPrefix + "discord_krisp.dll";
            return List.of(sdkName);
        } else if (Platform.isMac()) {
            return List.of("libdiscord_partner_sdk.dylib", "libdiscord_krisp.dylib");
        } else if (Platform.isLinux()) {
            return List.of("libdiscord_partner_sdk.so");
        }
        return null;
    }

    public static void load() {
        Path path = copyResourceDirToTemp("natives");
        List<String> libraryNames = getLibraryNames();
        if (libraryNames == null) {
            throw new RuntimeException("This platform is not supported!");
        }

        UniversalVoiceChat.LOGGER.info("Loading Discord libraries: {}", String.join(", ", libraryNames));
        libraryNames.forEach(libraryName -> {
            System.load(path.resolve(libraryName).toAbsolutePath().toString());
        });

    }

    // Strings
    public static String readDiscordString(MemorySegment discordString) {
        MemorySegment ptr = Discord_String.ptr(discordString);
        long size = Discord_String.size(discordString);
        byte[] bytes = ptr.reinterpret(size).toArray(ValueLayout.JAVA_BYTE);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static MemorySegment writeDiscordString(String string, Arena arena) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

        MemorySegment buffer = arena.allocate(bytes.length);
        MemorySegment.copy(bytes, 0, buffer, ValueLayout.JAVA_BYTE, 0, bytes.length);

        MemorySegment discordString = arena.allocate(Discord_String.layout());
        Discord_String.ptr(discordString, buffer);
        Discord_String.size(discordString, bytes.length);

        return discordString;
    }

    // Maps
    public static MemorySegment writeDiscordProperties(Map<String, String> map, Arena arena) {
        int count = map.size();

        long stringLayout = Discord_String.layout().byteSize();
        MemorySegment keysArray = arena.allocate(stringLayout * count);
        MemorySegment valuesArray = arena.allocate(stringLayout * count);

        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            MemorySegment keySlice = keysArray.asSlice(i * stringLayout, stringLayout);
            byte[] keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            MemorySegment keyBuf = arena.allocate(keyBytes.length);
            MemorySegment.copy(keyBytes, 0, keyBuf, ValueLayout.JAVA_BYTE, 0, keyBytes.length);
            Discord_String.ptr(keySlice, keyBuf);
            Discord_String.size(keySlice, keyBytes.length);

            MemorySegment valSlice = valuesArray.asSlice(i * stringLayout, stringLayout);
            byte[] valBytes = entry.getValue().getBytes(StandardCharsets.UTF_8);
            MemorySegment valBuf = arena.allocate(valBytes.length);
            MemorySegment.copy(valBytes, 0, valBuf, ValueLayout.JAVA_BYTE, 0, valBytes.length);
            Discord_String.ptr(valSlice, valBuf);
            Discord_String.size(valSlice, valBytes.length);

            i++;
        }

        MemorySegment props = arena.allocate(Discord_Properties.layout());
        Discord_Properties.size(props, count);
        Discord_Properties.keys(props, keysArray);
        Discord_Properties.values(props, valuesArray);

        return props;
    }

    public static Map<String, String> readDiscordProperties(MemorySegment props) {
        long count = Discord_Properties.size(props);
        long stringLayout = Discord_String.layout().byteSize();

        MemorySegment keys = Discord_Properties.keys(props).reinterpret(stringLayout * count);
        MemorySegment values = Discord_Properties.values(props).reinterpret(stringLayout * count);

        Map<String, String> map = new LinkedHashMap<>();
        for (long i = 0; i < count; i++) {
            String key = readDiscordString(keys.asSlice(i * stringLayout, stringLayout));
            String val = readDiscordString(values.asSlice(i * stringLayout, stringLayout));
            map.put(key, val);
        }
        return map;
    }

    // Misc
    public static List<AudioDevice> readAudioDevices(MemorySegment span, Arena arena) {
        long deviceCount = Discord_AudioDeviceSpan.size(span);
        long deviceSize = Discord_AudioDevice.layout().byteSize();
        MemorySegment devicesArray = Discord_AudioDeviceSpan.ptr(span).reinterpret(deviceSize * deviceCount);
        ArrayList<AudioDevice> audioDevices = new ArrayList<>();
        for (long i = 0; i < deviceCount; i++) {
            MemorySegment device = devicesArray.asSlice(i * deviceSize, deviceSize);

            MemorySegment id = Discord_String.allocate(arena);
            Discord_AudioDevice_Id(device, id);
            MemorySegment name = Discord_String.allocate(arena);
            Discord_AudioDevice_Name(device, name);

            audioDevices.add(new AudioDevice(NativeHelper.readDiscordString(id), NativeHelper.readDiscordString(name)));
        }
        return audioDevices;
    }
}
