package it.frafol.cleanstaffchat.bungee.adminchat.commands;

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
        super(BungeeCommandsConfig.ADMINCHAT_MUTE.getStringList().get(0),"", BungeeCommandsConfig.ADMINCHAT_MUTE.getStringList().toArray(new String[0]));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(BungeeConfig.ADMINCHAT_MUTE_MODULE.get(Boolean.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.MODULE_DISABLED.color()
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            return;
        }

        if (!sender.hasPermission(BungeeConfig.ADMINCHAT_MUTE_PERMISSION.get(String.class))) {
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            return;
        }

        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null && BungeeRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
            final String final_message = "set.adminchat.mute";

            if (!PlayerCache.getMuted_admin().contains("true")) {
                sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_MUTED.color()
                        .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            } else {
                sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_UNMUTED.color()
                        .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            }

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-MuteAdminChat-RedisBungee", final_message);
            return;

        } else if (!PlayerCache.getMuted_admin().contains("true")) {
            PlayerCache.getMuted_admin().add("true");
            sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_MUTED.color()
                    .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
            return;
        }

        PlayerCache.getMuted_admin().remove("true");
        sender.sendMessage(TextComponent.fromLegacy(BungeeMessages.ADMINCHAT_UNMUTED.color()
                .replace("%prefix%", BungeeMessages.ADMINPREFIX.color())));
    }
}
