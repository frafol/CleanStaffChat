package it.frafol.cleanstaffchat.bungee.hooks;

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.channel.ChannelMessenger;
import it.frafol.cleanstaffchat.bungee.channel.ChatChannel;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/** Receives RedisBungee pub/sub messages and applies them on this proxy. */
public class RedisListener implements Listener {

    public static final String STAFF_OTHER_CHANNEL = "CleanStaffChat-StaffOtherMessage-RedisBungee";
    public static final String STAFF_AFK_CHANNEL = "CleanStaffChat-StaffAFKMessage-RedisBungee";

    private final CleanStaffChat plugin;

    public RedisListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRedisBungeeMessage(PubSubMessageEvent event) {
        final String name = event.getChannel();
        for (ChatChannel channel : ChatChannel.values()) {
            if (name.equals(channel.redisMessageChannel)) {
                ChannelMessenger.broadcastLocalIgnoringServers(channel, event.getMessage());
                plugin.getProxy().getConsole().sendMessage(TextComponent.fromLegacy(event.getMessage()));
                return;
            }
            if (name.equals(channel.redisMuteChannel)) {
                channel.setMuted(!channel.isMuted());
                return;
            }
        }

        if (name.equals(STAFF_OTHER_CHANNEL) || name.equals(STAFF_AFK_CHANNEL)) ChannelMessenger.broadcastLocalIgnoringServers(ChatChannel.STAFF, event.getMessage());
    }
}
