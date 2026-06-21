package lol.sylvie.universalvc.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.UUID;

public class DistanceTracker {
    public static HashMap<UUID, Float> distances = new HashMap<>();

    public static int maxRange = 48;
    public static volatile boolean isInGame = false;

    public static void init() {
        ClientTickEvents.END_LEVEL_TICK.register(level -> {
            int maxRangeSqr = maxRange * maxRange;

            AbstractClientPlayer thisPlayer = Minecraft.getInstance().player;
            if (thisPlayer == null) {
                isInGame = false;
                return;
            }
            isInGame = true;

            for (AbstractClientPlayer player : level.players()) {
                double dist = thisPlayer.distanceToSqr(player);
                float factor = dist > maxRangeSqr ? 0f : (float) (1 - (dist / maxRangeSqr));
                distances.put(player.getUUID(), factor * factor);
            }
        });

        ClientLoginConnectionEvents.DISCONNECT.register((listener, client) -> distances.clear());
    }
}
