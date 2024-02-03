package it.frafol.cleanstaffchat.velocity.staffchat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.enums.VelocityConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityDiscordConfig;
import it.frafol.cleanstaffchat.velocity.enums.VelocityMessages;
import it.frafol.cleanstaffchat.velocity.objects.Placeholder;
import it.frafol.cleanstaffchat.velocity.objects.TextFile;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements SimpleCommand {

    public final CleanStaffChat PLUGIN;

    public ReloadCommand(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {

        final CommandSource commandSource = invocation.source();

        if (!commandSource.hasPermission(VelocityConfig.STAFFCHAT_RELOAD_PERMISSION.get(String.class))) {
            VelocityMessages.NO_PERMISSION.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));
            return;
        }

        TextFile.reloadAll();
        VelocityMessages.RELOADED.send(commandSource,
                    new Placeholder("prefix", VelocityMessages.PREFIX.color()));

        if (PLUGIN.getJda() == null && VelocityDiscordConfig.DISCORD_ENABLED.get(Boolean.class)) {
            PLUGIN.startJDA();
        }
    }
}