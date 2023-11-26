package it.frafol.cleanstaffchat.velocity.staffchat.commands;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.enums.VelocityRedis;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.STAFFCHAT_MUTE_MODULE;

public class MuteCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;

    public MuteCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource commandSource = invocation.source();

        if (!(STAFFCHAT_MUTE_MODULE.get(Boolean.class))) {
            VelocityMessages.MODULE_DISABLED.send(commandSource, new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        if (!commandSource.hasPermission(VelocityConfig.STAFFCHAT_MUTE_PERMISSION.get(String.class))) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
            final String final_message = "set.staffchat.mute";

            if (!PlayerCache.getMuted().contains("true")) {
                VelocityMessages.STAFFCHAT_MUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            } else {
                VelocityMessages.STAFFCHAT_UNMUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            }

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-MuteStaffChat-RedisBungee", final_message);

        } else if (!PlayerCache.getMuted().contains("true")) {
            PlayerCache.getMuted().add("true");
            VelocityMessages.STAFFCHAT_MUTED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));

        } else {
            PlayerCache.getMuted().remove("true");
            VelocityMessages.STAFFCHAT_UNMUTED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));

        }
    }
}