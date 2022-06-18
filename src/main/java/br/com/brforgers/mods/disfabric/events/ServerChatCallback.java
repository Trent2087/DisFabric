package br.com.brforgers.mods.disfabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ServerChatCallback {
    Event<ServerChatCallback> EVENT = EventFactory.createArrayBacked(ServerChatCallback.class, callbacks -> (playerEntity, rawMessage) -> {
        for (ServerChatCallback callback : callbacks) {
            callback.onServerChat(playerEntity, rawMessage);
        }
    });

    void onServerChat(ServerPlayerEntity playerEntity, String rawMessage);
}
