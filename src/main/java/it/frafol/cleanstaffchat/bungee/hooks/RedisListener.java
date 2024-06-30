package it.frafol.cleanstaffchat.bungee.hooks;

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class RedisListener implements Listener {

    public final CleanStaffChat PLUGIN;

    public RedisListener(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRedisBungeeMessage(PubSubMessageEvent event) {

        if (event.getChannel().equals("CleanStaffChat-StaffMessage-RedisBungee")) {

            final String player_message = event.getMessage();

            CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                            (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacy(player_message)));

            PLUGIN.getProxy().getConsole().sendMessage(TextComponent.fromLegacy(player_message));

        }

        if (event.getChannel().equals("CleanStaffChat-DonorMessage-RedisBungee")) {

            final String player_message = event.getMessage();

            CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                            (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacy(player_message)));

            PLUGIN.getProxy().getConsole().sendMessage(TextComponent.fromLegacy(player_message));

        }

        if (event.getChannel().equals("CleanStaffChat-AdminMessage-RedisBungee")) {

            final String player_message = event.getMessage();

            CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                            (players -> players.hasPermission(BungeeConfig.ADMINCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacy(player_message)));

            PLUGIN.getProxy().getConsole().sendMessage(TextComponent.fromLegacy(player_message));

        }

        if (event.getChannel().equals("CleanStaffChat-StaffOtherMessage-RedisBungee")) {

            final String player_message = event.getMessage();

            CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                            (players -> players.hasPermission(BungeeConfig.STAFFCHAT_USE_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacy(player_message)));

        }

        if (event.getChannel().equals("CleanStaffChat-StaffAFKMessage-RedisBungee")) {

            final String player_message = event.getMessage();

            CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                            (players -> players.hasPermission(BungeeConfig.STAFFCHAT_AFK_PERMISSION.get(String.class))
                                    && !(PlayerCache.getToggled().contains(players.getUniqueId())))
                    .forEach(players -> players.sendMessage(TextComponent.fromLegacy(player_message)));

        }

        if (event.getChannel().equals("CleanStaffChat-MuteStaffChat-RedisBungee")) {

            if (PlayerCache.getMuted().contains("true")) {

                PlayerCache.getMuted().remove("true");

            } else {

                PlayerCache.getMuted().add("true");

            }
        }

        if (event.getChannel().equals("CleanStaffChat-MuteAdminChat-RedisBungee")) {

            if (PlayerCache.getMuted_admin().contains("true")) {

                PlayerCache.getMuted_admin().remove("true");

            } else {

                PlayerCache.getMuted_admin().add("true");

            }
        }

        if (event.getChannel().equals("CleanStaffChat-MuteDonorChat-RedisBungee")) {

            if (PlayerCache.getMuted_donor().contains("true")) {

                PlayerCache.getMuted_donor().remove("true");

            } else {

                PlayerCache.getMuted_donor().add("true");

            }
        }
    }
}
