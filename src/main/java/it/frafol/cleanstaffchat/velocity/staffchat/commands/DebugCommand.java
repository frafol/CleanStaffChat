package it.frafol.cleanstaffchat.velocity.staffchat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class DebugCommand implements SimpleCommand {

    private final CleanStaffChat instance;

    public DebugCommand(CleanStaffChat instance) {
        this.instance = instance;
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {

        final CommandSource source = invocation.source();

        if (invocation.arguments().length != 0) {
            return;
        }

        if (!instance.getContainer().getDescription().getVersion().isPresent()) {
            return;
        }

        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7CleanStaffChat Informations"));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Version: §d" + instance.getContainer().getDescription().getVersion().get()));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| §7Velocity: §d" + instance.getServer().getVersion().getVersion()));
        source.sendMessage(LegacyComponentSerializer.legacy('§').deserialize("§d| "));

    }
}
