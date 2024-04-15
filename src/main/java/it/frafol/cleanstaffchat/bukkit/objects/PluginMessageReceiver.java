package it.frafol.cleanstaffchat.bukkit.objects;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import it.frafol.cleanstaffchat.bukkit.CleanStaffChat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PluginMessageReceiver implements PluginMessageListener {

    private final CleanStaffChat instance = CleanStaffChat.getInstance();

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!(channel.equals("cleansc:cancel"))) {
            return;
        }

        ByteArrayDataInput dataInput = ByteStreams.newDataInput(message);
        String subChannel = dataInput.readUTF();

        if (subChannel.equals("true")) {

            String player_found = dataInput.readUTF();
            final Player final_player = instance.getServer().getPlayer(player_found);

            if (final_player == null) {
                return;
            }

            PlayerCache.getNochat().add(final_player.getUniqueId());
            return;
        }

        if (subChannel.equals("false")) {

            String player_found = dataInput.readUTF();
            final Player final_player = instance.getServer().getPlayer(player_found);

            if (final_player == null) {
                return;
            }

            PlayerCache.getNochat().remove(final_player.getUniqueId());
        }
    }
}