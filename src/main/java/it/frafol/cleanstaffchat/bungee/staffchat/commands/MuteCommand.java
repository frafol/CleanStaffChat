package it.frafol.cleanstaffchat.bungee.staffchat.commands;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import it.frafol.cleanstaffchat.bungee.enums.BungeeCommandsConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.enums.BungeeMessages;
import it.frafol.cleanstaffchat.bungee.enums.BungeeRedis;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class MuteCommand extends Command {

    public MuteCommand() {
        super(BungeeCommandsConfig.STAFFCHAT_MUTE.getStringList().get(0),"",BungeeCommandsConfig.STAFFCHAT_MUTE.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(BungeeConfig.STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (!sender.hasPermission(BungeeConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
            return;
        }

        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

            final String final_message = "set.staffchat.mute";

            if (!PlayerCache.getMuted().contains("true")) {
                sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFFCHAT_MUTED.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())));
            } else {
                sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFFCHAT_UNMUTED.color()
                        .replace("%prefix%", BungeeMessages.PREFIX.color())));
            }

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-MuteStaffChat-RedisBungee", final_message);

        } else if (!PlayerCache.getMuted().contains("true")) {
            PlayerCache.getMuted().add("true");
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFFCHAT_MUTED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
        } else {
            PlayerCache.getMuted().remove("true");
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.STAFFCHAT_UNMUTED.color()
                    .replace("%prefix%", BungeeMessages.PREFIX.color())));
        }
    }
}
