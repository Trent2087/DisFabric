package br.com.brforgers.mods.disfabric.listeners;

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.utils.DiscordCommandOutput;
import br.com.brforgers.mods.disfabric.utils.Utils;
import dev.gegy.mdchat.TextStyler;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
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
    private static final Style LINK = Style.EMPTY.withFormatting(Formatting.AQUA, Formatting.UNDERLINE);

    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        MinecraftServer server = getServer();
        if (server != null && !e.getAuthor().isBot() && e.getChannel().getId().equals(DisFabric.config.channelId)) {
            String raw = e.getMessage().getContentRaw();
            if (raw.startsWith("!")) {
                int space = raw.indexOf(' ', 1);
                switch (space == -1 ? raw.substring(1) : raw.substring(1, space)) {
                    case "console" -> {
                        if (!Arrays.asList(DisFabric.config.adminsIds).contains(e.getAuthor().getId())) return;
                        String command = raw.substring(space + 1);
                        server.execute(() -> server.getCommandManager().execute(getDiscordCommandSource(e), command));
                    }
                    case "whitelist" -> {
                        String username = raw.substring(space + 1);
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
                        StringBuilder playerList = new StringBuilder("```ansi\n=============== \u001B[32;1mOnline Players\u001B[0m (\u001B[34m")
                                .append(onlinePlayers.size()).append("\u001B[0m) ===============\n");
                        for (ServerPlayerEntity player : onlinePlayers) {
                            playerList.append("\n - \u001B[36m").append(player.getEntityName()).append("\u001B[0m");
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
                            ```ansi
                            =============== \u001B[32;1mCommands\u001B[0m ===============
                                                    
                            To whitelist yourself on this server use:
                            !\u001B[33mwhitelist\u001B[0m <\u001B[30mminecraft username\u001B[0m>
                                                    
                            !\u001B[33monline\u001B[0m: list server online players
                            !\u001B[33mtps\u001B[0m: shows loaded dimensions tps's
                            !\u001B[33mspawn\u001B[0m: shows the location of spawn
                            !\u001B[33mconsole\u001B[0m <\u001B[30mcommand\u001B[0m>: executes commands in the server console (admins only)
                            ```""").queue();
                    default -> e.getChannel().sendMessage("Unknown command. Run `!help` for available commands.").queue();
                }
            } else if (!DisFabric.config.commandsOnly) {
                var username = Utils.convertMemberToFormattedText(e.getAuthor(), e.getMember(), "");

                /*/ New potential method for consistency with in-game chat?
                var message = raw.isBlank() ? Text.empty() :
                        server.getMessageDecorator().decorate(null, Text.of(raw)).join().copy();
                /*/
                var styledMessage = TextStyler.INSTANCE.apply(raw);

                var message = styledMessage == null ? Text.empty() : styledMessage.copy();
                /**/

                for (var attachment : e.getMessage().getAttachments()) {
                    var style = LINK.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, attachment.getUrl()));
                    message.append(" <").append(Text.literal(attachment.getFileName()).fillStyle(style)).append(">");
                }

                server.getPlayerManager().broadcast(Utils.createFakeChatMessage(username, message), MessageType.SYSTEM);
            }
        }
    }

    public ServerCommandSource getDiscordCommandSource(@NotNull MessageReceivedEvent e){
        ServerWorld serverWorld = Objects.requireNonNull(getServer()).getOverworld();

        User author = e.getAuthor();
        String username = author.getName() + '#' + author.getDiscriminator();

        return new ServerCommandSource(new DiscordCommandOutput(), serverWorld == null ? Vec3d.ZERO : Vec3d.of(serverWorld.getSpawnPos()), Vec2f.ZERO, serverWorld, 4, username, Text.of(username), getServer(), null);
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