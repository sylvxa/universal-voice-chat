package lol.sylvie.universalvc.voice;

import com.google.common.util.concurrent.AtomicDouble;
import lol.sylvie.universalvc.UniversalVoiceChat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.UUID;

public class AudioFader {
    public static HashMap<UUID, Data> distances = new HashMap<>();
    public static volatile boolean isInGame = false;

    private static void setPanning(AbstractClientPlayer thisPlayer, AbstractClientPlayer otherPlayer, Data data) {
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
            AbstractClientPlayer thisPlayer = Minecraft.getInstance().player;
            if (thisPlayer == null) {
                isInGame = false;
                return;
            }
            isInGame = true;

            for (AbstractClientPlayer player : level.players()) {
                if (player == thisPlayer) continue;
                Data data = distances.computeIfAbsent(player.getUUID(), _ -> new Data());
                setPanning(thisPlayer, player, data);
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
