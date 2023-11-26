package it.frafol.cleanstaffchat.velocity.objects;

import it.frafol.cleanstaffchat.velocity.enums.VelocityDiscordConfig;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class JdaBuilder {

    private JDA jda;

    public JDA JdaWorker() {
        return jda;
    }

    public JDA getJda() throws LoginException {
        return JdaWorker();
    }

    @SneakyThrows
    public void startJDA() {
        try {
            jda = JDABuilder.createDefault(VelocityDiscordConfig.DISCORD_TOKEN.get(String.class)).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        } catch (Exception ignored) {
            System.out.println("§cInvalid Discord configuration, please check your discord.yml file.");
        }
    }
}