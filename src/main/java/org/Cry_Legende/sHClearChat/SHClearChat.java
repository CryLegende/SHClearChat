package org.Cry_Legende.sHClearChat;

import org.Cry_Legende.sHClearChat.network.ClearNetwork;
import org.Cry_Legende.sHClearChat.network.ShNetworkClear;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class SHClearChat extends JavaPlugin {

    /** Les autres serveurs du réseau ; null sans SHNetwork. */
    private ClearNetwork network;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        enableNetwork();
        getCommand("clearchat").setExecutor(this);
    }

    private String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path));
    }

    /**
     * Passe en mode réseau si SHNetwork est installé et 'reseau.actif' est vrai. La classe qui touche à SHNetwork
     * n'est résolue qu'ici : sans lui, rien n'est chargé et le nettoyage reste local.
     */
    private void enableNetwork() {
        if (!getConfig().getBoolean("reseau.actif", true)) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("SHNetwork")) return;
        try {
            ClearNetwork net = ShNetworkClear.open();
            net.onClearEveryone(this::clearEveryone);
            net.onClearPlayer((uuid, by) -> {
                Player target = Bukkit.getPlayer(uuid);
                if (target != null) {
                    clearTarget(target, by);
                }
            });
            network = net;
            getLogger().info("SHNetwork détecté : /cc nettoie le chat de tout le réseau.");
        } catch (RuntimeException | LinkageError e) {
            network = null;
            getLogger().log(Level.SEVERE, "SHNetwork présent mais inutilisable, nettoyage local à ce serveur : " + e);
        }
    }

    /** Nettoie le chat des joueurs de ce serveur qui n'ont pas la permission de bypass, puis l'annonce. */
    private void clearEveryone(String by) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("shclearchat.bypass")) {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage("");
                }
            }
        }

        Bukkit.broadcastMessage(getMessage("messages.chat-cleared-broadcast").replace("%player%", by));
    }

    /** Nettoie le chat d'un joueur de ce serveur et le prévient. */
    private void clearTarget(Player target, String by) {
        for (int i = 0; i < 100; i++) {
            target.sendMessage("");
        }
        target.sendMessage(getMessage("messages.target-chat-cleared").replace("%player%", by));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("clearchat")) {
            if (args.length == 0) {
                if (!sender.hasPermission("shclearchat.use")) {
                    sender.sendMessage(getMessage("messages.no-permission"));
                    return true;
                }

                clearEveryone(sender.getName());
                if (network != null) {
                    network.clearEveryone(sender.getName());
                }
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("self")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(getMessage("messages.only-players"));
                        return true;
                    }

                    Player player = (Player) sender;
                    for (int i = 0; i < 100; i++) {
                        player.sendMessage("");
                    }
                    player.sendMessage(getMessage("messages.self-chat-cleared"));
                    return true;
                }

                if (!sender.hasPermission("clearchat.other")) {
                    sender.sendMessage(getMessage("messages.no-permission-others"));
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null && network != null) {
                    // Connecté sur un autre serveur : son serveur nettoie son chat à sa place
                    ClearNetwork.Remote remote = network.findElsewhere(args[0]);
                    if (remote != null) {
                        network.clearPlayer(remote.uuid(), sender.getName());
                        sender.sendMessage(getMessage("messages.cleared-other-chat").replace("%target%", remote.name()));
                        return true;
                    }
                }
                if (target == null) {
                    target = Bukkit.getPlayer(args[0]);
                }
                if (target == null) {
                    sender.sendMessage(getMessage("messages.player-not-found"));
                    return true;
                }

                clearTarget(target, sender.getName());
                sender.sendMessage(getMessage("messages.cleared-other-chat").replace("%target%", target.getName()));
                return true;
            }
        }
        return false;
    }
}
