package org.Cry_Legende.sHClearChat.network;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Ce dont le nettoyage du chat a besoin du réseau. Seule {@link ShNetworkClear} connaît SHNetwork : sans lui,
 * aucune classe de ce paquet n'est chargée.
 */
public interface ClearNetwork {

    /** Joueur connecté sur un autre serveur du réseau : son identifiant et son pseudo, ou null s'il n'y en a pas. */
    Remote findElsewhere(String name);

    /** Demande aux autres serveurs de nettoyer le chat de tous leurs joueurs. */
    void clearEveryone(String by);

    /** Demande au serveur du joueur de nettoyer son chat. */
    void clearPlayer(UUID player, String by);

    /** Reçoit un nettoyage général demandé par {@code by}, sur le fil principal. */
    void onClearEveryone(Consumer<String> handler);

    /** Reçoit le nettoyage du chat d'un joueur demandé par {@code by}, sur le fil principal. */
    void onClearPlayer(BiConsumer<UUID, String> handler);

    /** Joueur d'un autre serveur. */
    record Remote(UUID uuid, String name) {
    }
}
