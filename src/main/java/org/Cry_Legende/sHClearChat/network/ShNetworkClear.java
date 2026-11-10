package org.Cry_Legende.sHClearChat.network;

import com.google.gson.JsonObject;
import org.Cry_Legende.sHNetwork.api.NetPlayer;
import org.Cry_Legende.sHNetwork.api.SHNetworkAPI;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Pont vers SHNetwork. Cette classe n'est chargée que si SHNetwork est installé. */
public final class ShNetworkClear implements ClearNetwork {

    private static final String CHANNEL_ALL = "clearchat.tous";
    private static final String CHANNEL_PLAYER = "clearchat.joueur";

    private final SHNetworkAPI api;

    private ShNetworkClear() {
        this.api = SHNetworkAPI.get();
    }

    /** Point d'entrée : le plugin ne résout cette classe qu'à cet appel, donc jamais sans SHNetwork. */
    public static ClearNetwork open() {
        return new ShNetworkClear();
    }

    @Override
    public Remote findElsewhere(String name) {
        Optional<NetPlayer> found = api.find(name).filter(p -> !p.server().equals(api.serverName()));
        return found.map(p -> new Remote(p.uuid(), p.name())).orElse(null);
    }

    @Override
    public void clearEveryone(String by) {
        JsonObject json = new JsonObject();
        json.addProperty("par", by);
        api.publish(CHANNEL_ALL, json.toString());
    }

    @Override
    public void clearPlayer(UUID player, String by) {
        JsonObject json = new JsonObject();
        json.addProperty("joueur", player.toString());
        json.addProperty("par", by);
        api.publish(CHANNEL_PLAYER, json.toString());
    }

    @Override
    public void onClearEveryone(Consumer<String> handler) {
        api.subscribe(CHANNEL_ALL, message -> {
            String by;
            try {
                by = message.object().get("par").getAsString();
            } catch (RuntimeException e) {
                return; // message illisible : le bus ne s'arrête jamais pour lui
            }
            handler.accept(by);
        }, true);
    }

    @Override
    public void onClearPlayer(BiConsumer<UUID, String> handler) {
        api.subscribe(CHANNEL_PLAYER, message -> {
            UUID player;
            String by;
            try {
                JsonObject json = message.object();
                player = UUID.fromString(json.get("joueur").getAsString());
                by = json.get("par").getAsString();
            } catch (RuntimeException e) {
                return;
            }
            handler.accept(player, by);
        }, true);
    }
}
