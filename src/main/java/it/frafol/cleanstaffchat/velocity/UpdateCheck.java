package it.frafol.cleanstaffchat.velocity;


import com.velocitypowered.api.event.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateCheck {

    public CleanStaffChat PLUGIN;

    public static final CleanStaffChat instance = CleanStaffChat.getInstance();

    public UpdateCheck(CleanStaffChat plugin) {
        this.PLUGIN = plugin;
    }

    @Subscribe
    public void getVersion(final Consumer<String> consumer) {
        instance.getServer().getScheduler().buildTask(PLUGIN, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=105220")
                    .openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                PLUGIN.getLogger().severe("Unable to check for updates: " + exception.getMessage());
            }
        }).schedule();
    }
}