package lol.sylvie.universalvc.voice;

import com.google.common.util.concurrent.AtomicDouble;
import lol.sylvie.universalvc.UniversalVoiceChat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AudioFader {
    public static HashMap<UUID, Data> distances = new HashMap<>();

    private static void setPanning(AbstractClientPlayer thisPlayer, Player otherPlayer, Data data) {
        // Panning
        double dx = otherPlayer.getX() - thisPlayer.getX();
        double dz = otherPlayer.getZ() - thisPlayer.getZ();

        double angleBetween = Math.toDegrees(Math.atan2(dz, dx));
        float ourYaw = thisPlayer.getYRot();
        double dAngle = ((((angleBetween - ourYaw) % 360) + 180) / 2);
        double panRadians = Math.toRadians(dAngle);

        double leftGain = Math.abs(Math.sin(panRadians)) / Mth.SQRT_OF_TWO;
        double rightGain = Math.abs(Math.cos(panRadians)) / Mth.SQRT_OF_TWO;

        // Attenuation
        double maxRange = UniversalVoiceChat.MOD_SETTINGS.hearingRange;
        double distance = thisPlayer.distanceTo(otherPlayer);
        if (distance > maxRange) {
            data.leftPan.set(0f);
            data.rightPan.set(0f);
            return;
        }

        double distanceGain = Math.max(0f, 1f - (distance / maxRange));
        distanceGain *= distanceGain;

        float attenuatedLeft = (float) (leftGain * distanceGain);
        float attenuatedRight = (float) (rightGain * distanceGain);

        data.leftPan.set(attenuatedLeft);
        data.rightPan.set(attenuatedRight);
    }

    public static void init() {
        ClientTickEvents.END_LEVEL_TICK.register(level -> {
            Minecraft minecraft = Minecraft.getInstance();
            ClientPacketListener connection = minecraft.getConnection();
            AbstractClientPlayer thisPlayer = minecraft.player;
            if (thisPlayer == null || connection == null) {
                return;
            }

            List<UUID> players = LobbyHandler.discordIdMap.values().stream().map(p -> p.getProfile().id()).toList();
            for (UUID uuid : players) {
                Player player = level.getPlayerByUUID(uuid);
                if (player == thisPlayer) continue;

                Data data = distances.computeIfAbsent(uuid, _ -> new Data());
                if (player != null)
                    setPanning(thisPlayer, player, data);
                else {
                    data.leftPan.set(0f);
                    data.rightPan.set(0f);
                }
            }
        });

        ClientLoginConnectionEvents.DISCONNECT.register((listener, client) -> distances.clear());
    }

    public static class Data {
        public AtomicDouble leftPan = new AtomicDouble(0d);
        public AtomicDouble rightPan = new AtomicDouble(0d);

        public double smoothLeft = 0d;
        public double smoothRight = 0d;
    }
}
