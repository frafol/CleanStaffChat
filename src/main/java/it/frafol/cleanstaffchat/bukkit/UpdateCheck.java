package it.frafol.cleanstaffchat.bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class UpdateCheck {

    public final CleanStaffChat PLUGIN;

    public UpdateCheck(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    public void getVersion(final Consumer<String> consumer) {
        CompletableFuture.runAsync(() -> {
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