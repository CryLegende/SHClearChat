package org.Cry_Legende.sHClearChat;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SHClearChat extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("clearchat").setExecutor(this);
    }

    private String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("clearchat")) {
            if (args.length == 0) {
                if (!sender.hasPermission("shclearchat.use")) {
                    sender.sendMessage(getMessage("messages.no-permission"));
                    return true;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("shclearchat.bypass")) {
                        for (int i = 0; i < 100; i++) {
                            player.sendMessage("");
                        }
                    }
                }

                Bukkit.broadcastMessage(getMessage("messages.chat-cleared-broadcast")
                    .replace("%player%", sender.getName()));
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

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(getMessage("messages.player-not-found"));
                    return true;
                }

                for (int i = 0; i < 100; i++) {
                    target.sendMessage("");
                }
                target.sendMessage(getMessage("messages.target-chat-cleared")
                    .replace("%player%", sender.getName()));
                sender.sendMessage(getMessage("messages.cleared-other-chat")
                    .replace("%target%", target.getName()));
                return true;
            }
        }
        return false;
    }
}
