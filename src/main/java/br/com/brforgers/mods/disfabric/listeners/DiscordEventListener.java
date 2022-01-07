package br.com.brforgers.mods.disfabric.listeners;

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.utils.DiscordCommandOutput;
import br.com.brforgers.mods.disfabric.utils.MarkdownParser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DiscordEventListener extends ListenerAdapter {

    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        MinecraftServer server = getServer();
        if(e.getAuthor() != e.getJDA().getSelfUser() && !e.getAuthor().isBot() && e.getChannel().getId().equals(DisFabric.config.channelId) && server != null) {
            String raw = e.getMessage().getContentRaw();
            if(raw.startsWith("!")) {
                int space = raw.indexOf(' ', 1);
                switch (space == -1 ? raw.substring(1) : raw.substring(1, space)) {
                    case "console" -> {
                        if(!Arrays.asList(DisFabric.config.adminsIds).contains(e.getAuthor().getId())) return;
                        String command = raw.substring(space);
                        server.execute(() -> server.getCommandManager().execute(getDiscordCommandSource(e), command));
                    }
                    case "whitelist" -> {
                        String username = raw.substring(space);
                        String command = "whitelist add " + username;

                        try {
                            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            if (connection.getResponseCode() / 100 != 2) {
                                e.getChannel().sendMessage("Invalid username.").queue();
                                return;
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        e.getChannel().sendMessage(e.getAuthor().getAsMention() + " ➠ `" + command + '`').queue();
                        server.execute(() -> server.getCommandManager().execute(getDiscordCommandSource(e), command));
                    }
                    case "spawn" -> {
                        ServerWorld serverWorld = Objects.requireNonNull(getServer()).getOverworld();
                        e.getChannel().sendMessage("Spawn location: " + Vec3d.of(serverWorld.getSpawnPos())).queue();
                    }
                    case "online" -> {
                        List<ServerPlayerEntity> onlinePlayers = server.getPlayerManager().getPlayerList();
                        StringBuilder playerList = new StringBuilder("```\n=============== Online Players (" + onlinePlayers.size() + ") ===============\n");
                        for (ServerPlayerEntity player : onlinePlayers) {
                            playerList.append("\n").append(player.getEntityName());
                        }
                        playerList.append("```");
                        e.getChannel().sendMessage(playerList.toString()).queue();
                    }
                    case "tps" -> {
                        StringBuilder tps = new StringBuilder("Server TPS: ");
                        double serverTickTime = MathHelper.average(server.lastTickLengths) * 1.0E-6D;
                        tps.append(Math.min(1000.0 / serverTickTime, 20));
                        e.getChannel().sendMessage(tps.toString()).queue();
                    }
                    case "help" -> e.getChannel().sendMessage("""
                        ```
                        =============== Commands ===============
                        
                        To whitelist yourself on this server use:
                        !whitelist <minecraft username>
                        
                        !online: list server online players
                        !tps: shows loaded dimensions tps´s
                        !spawn: shows the location of spawn
                        !console <command>: executes commands in the server console (admins only)
                        ```""").queue();
                    default -> e.getChannel().sendMessage("Unknown command. Run `!help` for available commands.").queue();
                }
            } else if(!DisFabric.config.commandsOnly){
                LiteralText discord = new LiteralText(DisFabric.config.texts.coloredText.replace("%discordname%", Objects.requireNonNull(e.getMember()).getEffectiveName()).replace("%message%",e.getMessage().getContentDisplay().replace("§", DisFabric.config.texts.removeVanillaFormattingFromDiscord ? "&" : "§").replace("\n", DisFabric.config.texts.removeLineBreakFromDiscord ? " " : "\n") + ((e.getMessage().getAttachments().size() > 0) ? " <att>" : "") + ((e.getMessage().getEmbeds().size() > 0) ? " <embed>" : "")));
                discord.setStyle(discord.getStyle().withColor(TextColor.fromRgb(Objects.requireNonNull(e.getMember()).getColorRaw())));
                LiteralText msg = new LiteralText(DisFabric.config.texts.colorlessText.replace("%discordname%", Objects.requireNonNull(e.getMember()).getEffectiveName()).replace("%message%", MarkdownParser.parseMarkdown(e.getMessage().getContentDisplay().replace("§", DisFabric.config.texts.removeVanillaFormattingFromDiscord ? "&" : "§").replace("\n", DisFabric.config.texts.removeLineBreakFromDiscord ? " " : "\n") + ((e.getMessage().getAttachments().size() > 0) ? " <att>" : "") + ((e.getMessage().getEmbeds().size() > 0) ? " <embed>" : ""))));
                msg.setStyle(msg.getStyle().withColor(TextColor.fromFormatting(Formatting.WHITE)));
                server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> serverPlayerEntity.sendMessage(new LiteralText("").append(discord).append(msg),false));
            }
        }
    }

    public ServerCommandSource getDiscordCommandSource(@NotNull MessageReceivedEvent e){
        ServerWorld serverWorld = Objects.requireNonNull(getServer()).getOverworld();

        User author = e.getAuthor();
        String username = author.getName() + '#' + author.getDiscriminator();

        return new ServerCommandSource(new DiscordCommandOutput(), serverWorld == null ? Vec3d.ZERO : Vec3d.of(serverWorld.getSpawnPos()), Vec2f.ZERO, serverWorld, 4, username, new LiteralText(username), getServer(), null);
    }

    private MinecraftServer getServer(){
        @SuppressWarnings("deprecation")
        Object gameInstance = FabricLoader.getInstance().getGameInstance();
        if (gameInstance instanceof MinecraftServer) {
            return (MinecraftServer) gameInstance;
        }else {
            return null;
        }
    }
}