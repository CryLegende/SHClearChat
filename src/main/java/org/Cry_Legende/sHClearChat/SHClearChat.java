package org.Cry_Legende.sHClearChat;

import org.Cry_Legende.sHClearChat.network.ClearNetwork;
import org.Cry_Legende.sHClearChat.network.ShNetworkClear;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class SHClearChat extends JavaPlugin implements TabExecutor {

    private static final String RELOAD_PERMISSION = "shclearchat.admin.reload";

    /** Les autres serveurs du réseau ; null sans SHNetwork. */
    private ClearNetwork network;
    /** Textes de messages.yml, remplacés d'un bloc au rechargement. */
    private volatile YamlConfiguration messages = new YamlConfiguration();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            messages = loadMessages();
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "messages.yml illisible, textes du jar utilisés : " + e.getMessage());
            messages = jarMessages();
        }
        enableNetwork();
        getCommand("clearchat").setExecutor(this);
        getCommand("shclearchat").setExecutor(this);
        getCommand("shclearchat").setTabCompleter(this);
    }

    private String getMessage(String key) {
        String text = messages.getString(key);
        return ChatColor.translateAlternateColorCodes('&', text != null ? text : key);
    }

    // ------------------------------------------------------------------ messages.yml

    /** Textes du messages.yml du jar : valeurs par défaut des clés absentes du fichier du serveur. */
    private YamlConfiguration jarMessages() {
        YamlConfiguration jar = new YamlConfiguration();
        try (var in = getResource("messages.yml")) {
            if (in != null) {
                jar.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, "messages.yml du jar illisible : " + e.getMessage());
        }
        return jar;
    }

    /**
     * Lit messages.yml en mode strict. S'il n'existe pas, il est créé à partir de l'ancienne section « messages » de
     * config.yml (textes déjà personnalisés gardés), complétée par les textes du jar. Les clés absentes du fichier
     * prennent la valeur du jar.
     */
    private YamlConfiguration loadMessages() throws IOException, InvalidConfigurationException {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            YamlConfiguration created = jarMessages();
            File configFile = new File(getDataFolder(), "config.yml");
            if (configFile.exists()) {
                var legacy = YamlConfiguration.loadConfiguration(configFile).getConfigurationSection("messages");
                if (legacy != null) {
                    for (String key : legacy.getKeys(false)) {
                        if (legacy.isString(key)) {
                            created.set(key, legacy.getString(key));
                        }
                    }
                    getLogger().info("Messages repris de config.yml dans messages.yml.");
                }
            }
            getDataFolder().mkdirs();
            created.save(file);
        }
        YamlConfiguration loaded = new YamlConfiguration();
        loaded.load(file);
        loaded.setDefaults(jarMessages());
        return loaded;
    }

    /** Nom du fichier invalide et cause, ou null si config.yml et messages.yml se lisent. */
    private String checkFiles() {
        for (String name : new String[] {"config.yml", "messages.yml"}) {
            File file = new File(getDataFolder(), name);
            if (!file.exists()) {
                continue;
            }
            try {
                new YamlConfiguration().load(file);
            } catch (IOException | InvalidConfigurationException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                String text = String.valueOf(cause.getMessage()).replaceAll("\\s+", " ").trim();
                return name + " : " + (text.length() > 200 ? text.substring(0, 200) + "..." : text);
            }
        }
        return null;
    }

    /** Relit config.yml et messages.yml ; rend null si tout est relu, sinon le fichier refusé et la cause. */
    private String reloadFiles() {
        String error = checkFiles();
        if (error != null) {
            getLogger().warning("Rechargement refusé, configuration précédente gardée (" + error + ").");
            return error;
        }
        reloadConfig();
        try {
            messages = loadMessages();
        } catch (IOException | InvalidConfigurationException e) {
            return "messages.yml : " + e.getMessage();
        }
        return null;
    }

    // ------------------------------------------------------------------ réseau

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
            getLogger().info("SHNetwork détecté : /clearchat nettoie le chat de tout le réseau.");
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

        Bukkit.broadcastMessage(getMessage("chat-cleared-broadcast").replace("%player%", by));
    }

    /** Nettoie le chat d'un joueur de ce serveur et le prévient. */
    private void clearTarget(Player target, String by) {
        for (int i = 0; i < 100; i++) {
            target.sendMessage("");
        }
        target.sendMessage(getMessage("target-chat-cleared").replace("%player%", by));
    }

    // ------------------------------------------------------------------ commandes

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("shclearchat")) {
            return onAdminCommand(sender, args);
        }
        if (cmd.getName().equalsIgnoreCase("clearchat")) {
            if (args.length == 0) {
                if (!sender.hasPermission("shclearchat.use")) {
                    sender.sendMessage(getMessage("no-permission"));
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
                        sender.sendMessage(getMessage("only-players"));
                        return true;
                    }

                    Player player = (Player) sender;
                    for (int i = 0; i < 100; i++) {
                        player.sendMessage("");
                    }
                    player.sendMessage(getMessage("self-chat-cleared"));
                    return true;
                }

                if (!sender.hasPermission("shclearchat.other")) {
                    sender.sendMessage(getMessage("no-permission-others"));
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null && network != null) {
                    // Connecté sur un autre serveur : son serveur nettoie son chat à sa place
                    ClearNetwork.Remote remote = network.findElsewhere(args[0]);
                    if (remote != null) {
                        network.clearPlayer(remote.uuid(), sender.getName());
                        sender.sendMessage(getMessage("cleared-other-chat").replace("%target%", remote.name()));
                        return true;
                    }
                }
                if (target == null) {
                    target = Bukkit.getPlayer(args[0]);
                }
                if (target == null) {
                    sender.sendMessage(getMessage("player-not-found"));
                    return true;
                }

                clearTarget(target, sender.getName());
                sender.sendMessage(getMessage("cleared-other-chat").replace("%target%", target.getName()));
                return true;
            }
        }
        return false;
    }

    /** /shclearchat : seule sous-commande, reload. */
    private boolean onAdminCommand(CommandSender sender, String[] args) {
        List<String> usable = usableSubcommands(sender);
        if (usable.isEmpty()) {
            sender.sendMessage(getMessage("no-permission"));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && usable.contains("reload")) {
            String error = reloadFiles();
            if (error == null) {
                sender.sendMessage(getMessage("reload-success"));
            } else {
                sender.sendMessage(getMessage("reload-error").replace("%error%", error));
            }
            return true;
        }
        sender.sendMessage(getMessage("admin-usage").replace("%subcommands%", String.join("|", usable)));
        return true;
    }

    private List<String> usableSubcommands(CommandSender sender) {
        List<String> usable = new ArrayList<>();
        if (sender.hasPermission(RELOAD_PERMISSION)) {
            usable.add("reload");
        }
        return usable;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("shclearchat")) {
            // /clearchat : noms des joueurs en ligne, complétion par défaut de Bukkit
            return null;
        }
        if (args.length != 1) {
            return List.of();
        }
        String typed = args[0].toLowerCase(Locale.ROOT);
        return usableSubcommands(sender).stream().filter(name -> name.startsWith(typed)).toList();
    }
}
