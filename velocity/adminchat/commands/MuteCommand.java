package it.frafol.cleanstaffchat.velocity.adminchat.commands;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.enums.VelocityRedis;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.ADMINCHAT_MUTE_MODULE;

public class MuteCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;

    public MuteCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource commandSource = invocation.source();
        if (!(ADMINCHAT_MUTE_MODULE.get(Boolean.class))) {
            VelocityMessages.MODULE_DISABLED.send(commandSource, new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            return;
        }

        if (!commandSource.hasPermission(VelocityConfig.ADMINCHAT_MUTE_PERMISSION.get(String.class))) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            return;
        }

        if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();

            final String final_message = "set.adminchat.mute";

            if (!PlayerCache.getMuted_admin().contains("true")) {
                VelocityMessages.ADMINCHAT_MUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            } else {
                VelocityMessages.ADMINCHAT_UNMUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
            }

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-MuteAdminChat-RedisBungee", final_message);

        } else if (!PlayerCache.getMuted().contains("true")) {
            PlayerCache.getMuted().add("true");
            VelocityMessages.ADMINCHAT_MUTED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
        } else {
            PlayerCache.getMuted().remove("true");
            VelocityMessages.ADMINCHAT_UNMUTED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.ADMINPREFIX.color()));
        }
    }
}