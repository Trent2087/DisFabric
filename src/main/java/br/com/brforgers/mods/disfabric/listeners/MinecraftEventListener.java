package br.com.brforgers.mods.disfabric.listeners;

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.events.PlayerAdvancementCallback;
import br.com.brforgers.mods.disfabric.events.PlayerDeathCallback;
import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import br.com.brforgers.mods.disfabric.markdown.SpecialStringType;
import br.com.brforgers.mods.disfabric.utils.Utils;
import br.com.brforgers.mods.disfabric.utils.VanishService;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public final class MinecraftEventListener {
    private static final Identifier DISFABRIC_CHAT = Identifier.of("disfabric", "decorator");

    public static void init() {
        if (!DisFabric.config.commandsOnly) {
            ServerMessageDecoratorEvent.EVENT.addPhaseOrdering(DISFABRIC_CHAT, Event.DEFAULT_PHASE);
            ServerMessageDecoratorEvent.EVENT.register(DISFABRIC_CHAT, (sender, message) ->
                    CompletableFuture.completedFuture(DisFabric.stop ? message : Text.of(Utils.convertMentionsFromNames(SpecialStringType.preprocess(message.getString())))));
            ServerChatCallback.EVENT.register((playerEntity, rawMessage) -> {
                if (DisFabric.stop || VanishService.isChatDisabled(playerEntity)) return;

                String convertedString = Utils.convertMentionsFromNames(rawMessage);
                if (DisFabric.config.isWebhookEnabled) {
                    JSONObject body = new JSONObject();
                    // TODO: Verify if this is applicable to all nickname mods
                    //  If not, add some detection logic for getName and getDisplayName against getEntityName.
                    body.put("username", Utils.playerName(playerEntity));
                    body.put("avatar_url", Utils.playerAvatarUrl(playerEntity));
                    JSONObject allowed_mentions = new JSONObject();
                    allowed_mentions.put("parse", new String[]{"users"});
                    body.put("allowed_mentions", allowed_mentions);
                    body.put("content", convertedString);
                    try {
                        Unirest.post(DisFabric.config.webhookURL).header("Content-Type", "application/json").body(body).asJsonAsync();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.playerMessage.replace("%playername%", MarkdownSanitizer.escape(Utils.playerName(playerEntity))).replace("%playermessage%", convertedString)).queue();
                }
            });

            PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
                if (DisFabric.config.announceAdvancements && advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && playerEntity.getAdvancementTracker().getProgress(advancement).isDone() && !DisFabric.stop) {
                    switch (advancement.getDisplay().getFrame()) {
                        case GOAL ->
                                DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.advancementGoal.replace("%playername%", MarkdownSanitizer.escape(Utils.playerName(playerEntity))).replace("%advancement%", MarkdownSanitizer.escape(advancement.getDisplay().getTitle().getString()))).queue();
                        case TASK ->
                                DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.advancementTask.replace("%playername%", MarkdownSanitizer.escape(Utils.playerName(playerEntity))).replace("%advancement%", MarkdownSanitizer.escape(advancement.getDisplay().getTitle().getString()))).queue();
                        case CHALLENGE ->
                                DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.advancementChallenge.replace("%playername%", MarkdownSanitizer.escape(Utils.playerName(playerEntity))).replace("%advancement%", MarkdownSanitizer.escape(advancement.getDisplay().getTitle().getString()))).queue();
                    }
                }
            });

            PlayerDeathCallback.EVENT.register((playerEntity, damageSource) -> {
                if (DisFabric.config.announceDeaths && !DisFabric.stop) {
                    DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.deathMessage.replace("%deathmessage%", MarkdownSanitizer.escape(damageSource.getDeathMessage(playerEntity).getString())).replace("%playername%", MarkdownSanitizer.escape(Utils.playerName(playerEntity)))).queue();
                }
            });

            ServerPlayConnectionEvents.JOIN.register((handler, $2, $3) -> {
                if (announcePlayers() && !VanishService.isPlayerVanished(handler.player)) {
                    DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.joinServer.replace("%playername%", MarkdownSanitizer.escape(Utils.playerName(handler.player)))).queue();
                }
            });

            ServerPlayConnectionEvents.DISCONNECT.register((handler, $2) -> {
                if (announcePlayers() && !VanishService.isPlayerVanished(handler.player)) {
                    DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.leftServer.replace("%playername%", MarkdownSanitizer.escape(Utils.playerName(handler.player)))).queue();
                }
            });
            VanishService.listen();
        }
    }

    private static boolean announcePlayers() {
        return DisFabric.config.announcePlayers && !DisFabric.stop;
    }

    /**
     * @author Octal
     */
    public static void onPlayerVanishChange(ServerPlayerEntity player, boolean vanish) {
        if (announcePlayers()) {
            DisFabric.bridgeChannel.sendMessage((vanish ? DisFabric.config.texts.leftServer : DisFabric.config.texts.joinServer).replace("%playername%", MarkdownSanitizer.escape(Utils.playerName(player)))).queue();
        }
    }
}
