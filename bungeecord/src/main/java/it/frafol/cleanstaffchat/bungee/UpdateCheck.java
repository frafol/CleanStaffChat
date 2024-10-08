package it.frafol.cleanstaffchat.bungee;

import net.md_5.bungee.api.ProxyServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateCheck {

    public CleanStaffChat PLUGIN;

    public UpdateCheck(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    public void getVersion(final Consumer<String> consumer) {

        ProxyServer.getInstance().getScheduler().runAsync(PLUGIN, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=105220")
                    .openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                PLUGIN.getLogger().severe("Unable to check for updates: " + exception.getMessage());
            }
        });
    }
}