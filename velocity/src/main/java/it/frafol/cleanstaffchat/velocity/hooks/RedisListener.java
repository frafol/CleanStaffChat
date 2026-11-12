package it.frafol.cleanstaffchat.velocity.hooks;

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import it.frafol.cleanstaffchat.velocity.CleanStaffChat;
import it.frafol.cleanstaffchat.velocity.channel.ChannelMessenger;
import it.frafol.cleanstaffchat.velocity.channel.ChatChannel;

/** Receives RedisBungee pub/sub messages and applies them on this proxy. */
public class RedisListener {

    public static final String STAFF_OTHER_CHANNEL = "CleanStaffChat-StaffOtherMessage-RedisBungee";
    public static final String STAFF_AFK_CHANNEL = "CleanStaffChat-StaffAFKMessage-RedisBungee";

    private final CleanStaffChat plugin;

    public RedisListener(CleanStaffChat plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onRedisBungeeMessage(PubSubMessageEvent event) {
        final String name = event.getChannel();
        for (ChatChannel channel : ChatChannel.values()) {
            if (name.equals(channel.redisMessageChannel)) {
                ChannelMessenger.broadcastRedisLine(channel, event.getMessage());
                plugin.getServer().getConsoleCommandSource().sendMessage(ChannelMessenger.legacy(event.getMessage()));
                return;
            }
            if (name.equals(channel.redisMuteChannel)) {
                channel.setMuted(!channel.isMuted());
                return;
            }
        }
        if (name.equals(STAFF_OTHER_CHANNEL) || name.equals(STAFF_AFK_CHANNEL)) ChannelMessenger.broadcastRedisLine(ChatChannel.STAFF, event.getMessage());
    }
}
