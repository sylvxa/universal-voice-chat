package lol.sylvie.universalvc.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.FriendsListResponse;
import lol.sylvie.universalvc.UniversalVoiceChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Services;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class ModIcons {
    public static final Identifier MUTED = UniversalVoiceChat.id("status/muted");
    public static final Identifier UNMUTED = UniversalVoiceChat.id("status/unmuted");
    public static final Identifier DEAFENED = UniversalVoiceChat.id("status/deafened");
    public static final Identifier UNDEAFENED = UniversalVoiceChat.id("status/undeafened");

    public static final Identifier EXIT = UniversalVoiceChat.id("action/exit");

    private static HashMap<UUID, PlayerSkin> skins = new HashMap<>();

    public static PlayerSkin getSkin(GameProfile profile) {
        UUID uuid = profile.id();
        if (skins.containsKey(uuid)) {
            return skins.get(uuid);
        }

        skins.put(uuid, DefaultPlayerSkin.get(uuid));
        return skins.get(uuid);
    }
}
