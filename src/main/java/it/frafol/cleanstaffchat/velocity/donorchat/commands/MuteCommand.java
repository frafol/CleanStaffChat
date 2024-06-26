package it.frafol.cleanstaffchat.velocity.donorchat.commands;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.enums.VelocityRedis;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.PlayerCache;

import static it.frafol.cleanstaffchat.velocity.enums.VelocityConfig.DONORCHAT_MUTE_MODULE;

public class MuteCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;

    public MuteCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource commandSource = invocation.source();
        if (!(DONORCHAT_MUTE_MODULE.get(Boolean.class))) {
            VelocityMessages.MODULE_DISABLED.send(commandSource, new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
            return;
        }

        if (!commandSource.hasPermission(VelocityConfig.DONORCHAT_MUTE_PERMISSION.get(String.class))) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
            return;
        }

        if (PLUGIN.getServer().getPluginManager().isLoaded("redisbungee") && VelocityRedis.REDIS_ENABLE.get(Boolean.class)) {

            final RedisBungeeAPI redisBungeeAPI = RedisBungeeAPI.getRedisBungeeApi();
            final String final_message = "set.donorchat.mute";

            if (!PlayerCache.getMuted_donor().contains("true")) {
                VelocityMessages.DONORCHAT_MUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
            } else {
                VelocityMessages.DONORCHAT_UNMUTED.send(commandSource,
                        new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
            }

            redisBungeeAPI.sendChannelMessage("CleanStaffChat-MuteDonorChat-RedisBungee", final_message);

        } else if (!PlayerCache.getMuted_donor().contains("true")) {

            PlayerCache.getMuted_donor().add("true");
            VelocityMessages.DONORCHAT_MUTED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));

        } else {

            PlayerCache.getMuted_donor().remove("true");
            VelocityMessages.DONORCHAT_UNMUTED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.DONORPREFIX.color()));
        }
    }
}