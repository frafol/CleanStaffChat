package it.frafol.cleanstaffchat.bungee.donorchat.commands;

import it.frafol.cleanstaffchat.bungee.CleanStaffChat;
import it.frafol.cleanstaffchat.bungee.enums.BungeeConfig;
import it.frafol.cleanstaffchat.bungee.objects.PlayerCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class DonorChatCommand extends Command {

    public final CleanStaffChat plugin;

    public DonorChatCommand(CleanStaffChat plugin) {

        super("dc","","donorchat","donor");

        this.plugin = plugin;

    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length == 0) {

            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORARGUMENTS.color()
                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));
                return;
            }

            ProxiedPlayer player = (ProxiedPlayer) sender;

            if (sender.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {

                if (PlayerCache.getCooldown().contains(player.getUniqueId())) {
                    player.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_COOLDOWN_MESSAGE.color()
                            .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));
                    return;
                }

                if (!PlayerCache.getToggled_2_donor().contains(player.getUniqueId())) {
                    if (!(BungeeConfig.DONORCHAT_TALK_MODULE.get(Boolean.class))) {
                        sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.MODULE_DISABLED.color()
                                .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));
                        return;
                    }
                    if (!PlayerCache.getMuted_donor().contains("true")) {
                        PlayerCache.getToggled_2_donor().add(player.getUniqueId());
                        sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_TALK_ENABLED.color()
                                .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));
                        return;
                    } else {
                        sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORARGUMENTS.color()
                                .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));
                    }
                } else if (PlayerCache.getToggled_2_donor().contains(player.getUniqueId())) {
                    PlayerCache.getToggled_2_donor().remove(player.getUniqueId());
                    sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_TALK_DISABLED.color()
                            .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));
                    return;
                }

            } else {

                sender.sendMessage(TextComponent.fromLegacyText("§7This server is using §dCleanStaffChat §7by §dfrafol§7."));

            }

            return;

        }

        final String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        final String commandsender = !(sender instanceof ProxiedPlayer) ? BungeeConfig.CONSOLE_PREFIX.get(String.class) :
                sender.getName();

        if (sender.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))) {
            if (!PlayerCache.getMuted_donor().contains("true")) {
                if (sender instanceof ProxiedPlayer) {

                    if (BungeeConfig.PREVENT_COLOR_CODES.get(Boolean.class)) {
                        if (message.contains("&0") ||
                                message.contains("&1") ||
                                message.contains("&2") ||
                                message.contains("&3") ||
                                message.contains("&4") ||
                                message.contains("&5") ||
                                message.contains("&6") ||
                                message.contains("&7") ||
                                message.contains("&8") ||
                                message.contains("&9") ||
                                message.contains("&a") ||
                                message.contains("&b") ||
                                message.contains("&c") ||
                                message.contains("&d") ||
                                message.contains("&e") ||
                                message.contains("&f") ||
                                message.contains("&k") ||
                                message.contains("&l") ||
                                message.contains("&m") ||
                                message.contains("&n") ||
                                message.contains("&o") ||
                                message.contains("&r")) {

                            sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.COLOR_CODES.color()
                                    .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                                    .replace("&", "§")));

                            return;

                        }
                    }

                    if (PlayerCache.getCooldown().contains(((ProxiedPlayer) sender).getUniqueId())) {
                        sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_COOLDOWN_MESSAGE.color()
                                .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));
                        return;
                    }

                    if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {

                        final LuckPerms api = LuckPermsProvider.get();

                        final User user = api.getUserManager().getUser(((ProxiedPlayer) sender).getUniqueId());

                        assert user != null;
                        final String prefix = user.getCachedData().getMetaData().getPrefix();
                        final String suffix = user.getCachedData().getMetaData().getSuffix();
                        final String user_prefix = prefix == null ? "" : prefix;
                        final String user_suffix = suffix == null ? "" : suffix;

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%message%", message)
                                        .replace("%displayname%", user_prefix + commandsender + user_suffix)
                                        .replace("%userprefix%", user_prefix)
                                        .replace("%usersuffix%", user_suffix)
                                        .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                                        .replace("&", "§"))));

                    } else {

                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%userprefix%", "")
                                        .replace("%displayname%", commandsender)
                                        .replace("%usersuffix%", "")
                                        .replace("%message%", message)
                                        .replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName())
                                        .replace("&", "§"))));
                    }

                    PlayerCache.getCooldown().add(((ProxiedPlayer) sender).getUniqueId());

                    ProxyServer.getInstance().getScheduler().schedule(plugin, () ->
                            PlayerCache.getCooldown().remove(((ProxiedPlayer) sender).getUniqueId()), BungeeConfig.DONOR_TIMER.get(Integer.class), TimeUnit.SECONDS);


                } else if (BungeeConfig.CONSOLE_CAN_TALK.get(Boolean.class)) {

                    if (!PlayerCache.getMuted_donor().contains("true")) {
                        CleanStaffChat.getInstance().getProxy().getPlayers().stream().filter
                                        (players -> players.hasPermission(BungeeConfig.DONORCHAT_USE_PERMISSION.get(String.class))
                                                && !(PlayerCache.getToggled_donor().contains(players.getUniqueId())))
                                .forEach(players -> players.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_FORMAT.color()
                                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                                        .replace("%user%", commandsender)
                                        .replace("%userprefix%", "")
                                        .replace("%usersuffix%", "")
                                        .replace("%displayname%", commandsender)
                                        .replace("%server%", "")
                                        .replace("%message%", message))));

                    } else {

                        sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_MUTED_ERROR.color()
                                .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));

                    }

                    sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_FORMAT.color()
                            .replace("%prefix%", BungeeConfig.DONORPREFIX.color())
                            .replace("%user%", commandsender)
                            .replace("%displayname%", commandsender)
                            .replace("%userprefix%", "")
                            .replace("%usersuffix%", "")
                            .replace("%server%", "")
                            .replace("%message%", message)));

                } else {

                    sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.PLAYER_ONLY.color()
                            .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));

                }
            } else {

                sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.DONORCHAT_MUTED_ERROR.color()
                        .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));

            }

        } else {

            sender.sendMessage(TextComponent.fromLegacyText(BungeeConfig.NO_PERMISSION.color()
                    .replace("%prefix%", BungeeConfig.DONORPREFIX.color())));

        }
    }
}