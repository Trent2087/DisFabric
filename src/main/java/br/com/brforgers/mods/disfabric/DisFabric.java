package br.com.brforgers.mods.disfabric;

import br.com.brforgers.mods.disfabric.commands.ShrugCommand;
import br.com.brforgers.mods.disfabric.listeners.DiscordEventListener;
import br.com.brforgers.mods.disfabric.listeners.MinecraftEventListener;
import kong.unirest.Unirest;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.Objects;

public class DisFabric implements DedicatedServerModInitializer {

    public static final String MOD_ID = "disfabric";
    public static Logger logger = LogManager.getLogger(MOD_ID);
    public static Configuration config;
    public static JDA jda;
    public static Guild guild;
    public static TextChannel textChannel;

    public static boolean stop = false;

    @Override
    public void onInitializeServer() {
        AutoConfig.register(Configuration.class, JanksonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(Configuration.class).getConfig();
        if(config.isWebhookEnabled && (config.webhookURL == null || config.webhookURL.isBlank())) {
            logger.error("Webhook is not set. Falling back to a regular message. Please set a webhook URL in ~/config/disfabric.json5");
            config.isWebhookEnabled = false;
        }
        try {
            if(config.botToken == null || config.botToken.isBlank()) {
                logger.error("Unable to login. Please setup the config at ~/config/disfabric.json5");
            } else {
                JDABuilder builder = JDABuilder.createDefault(config.botToken).setHttpClient(new OkHttpClient.Builder()
                                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                                .build())
                        .setChunkingFilter(ChunkingFilter.ALL)
                        .addEventListeners(new DiscordEventListener())
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .enableIntents(GatewayIntent.GUILD_MEMBERS);
                DisFabric.jda = builder.build();
                DisFabric.jda.awaitReady();
                DisFabric.textChannel = Objects.requireNonNull(DisFabric.jda.getTextChannelById(config.channelId), "No such Text Channel");
                DisFabric.guild = DisFabric.textChannel.getGuild();
            }
        } catch (LoginException | InterruptedException ex) {
            jda = null;
            DisFabric.logger.error("Unable to login!", ex);
        }
        if(jda != null) {
            if (!config.botGameStatus.isEmpty())
                jda.getPresence().setActivity(Activity.playing(config.botGameStatus));

            if (!config.commandsOnly) {
                ServerLifecycleEvents.SERVER_STARTED.register((server) -> textChannel.sendMessage(DisFabric.config.texts.serverStarted).queue());
            }

            ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
                stop = true;
                if (!config.commandsOnly) {
                    textChannel.sendMessage(DisFabric.config.texts.serverStopped).complete();
                }
                Unirest.shutDown();
                DisFabric.jda.shutdownNow();
            });
            new MinecraftEventListener().init();

        }
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (dedicated) {
                ShrugCommand.register(dispatcher);
            }
        });
    }
}
