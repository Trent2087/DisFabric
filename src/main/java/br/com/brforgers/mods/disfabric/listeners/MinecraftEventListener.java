package br.com.brforgers.mods.disfabric.listeners;

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.events.PlayerAdvancementCallback;
import br.com.brforgers.mods.disfabric.events.PlayerDeathCallback;
import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import br.com.brforgers.mods.disfabric.utils.Utils;
import dev.gegy.mdchat.TextStyler;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.TranslatableText;

import java.util.Optional;

public class MinecraftEventListener {
    public void init() {
        if (!DisFabric.config.commandsOnly) {
            ServerChatCallback.EVENT.register((playerEntity, rawMessage, message) -> {
                if (!DisFabric.stop) {
                    String convertedString = Utils.convertMentionsFromNames(rawMessage);
                    if (DisFabric.config.isWebhookEnabled) {
                        JSONObject body = new JSONObject();
                        body.put("username", playerEntity.getEntityName());
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
                        DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.playerMessage.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName())).replace("%playermessage%", convertedString)).queue();
                    }
                    // If it returns itself, there was no new mention.
                    // If it doesn't return itself, it's always mutated.
                    // So: why bother doing .equals when it's either it'll either waste cycles or short circuits.
                    //noinspection StringEquality
                    if (DisFabric.config.modifyChatMessages && rawMessage != convertedString && message instanceof TranslatableText translatableText) {
                        translatableText.getArgs()[1] = TextStyler.INSTANCE.apply(convertedString);
                        return Optional.of(translatableText);
                    }
                }
                return Optional.empty();
            });

            PlayerAdvancementCallback.EVENT.register((playerEntity, advancement) -> {
                if (DisFabric.config.announceAdvancements && advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && playerEntity.getAdvancementTracker().getProgress(advancement).isDone() && !DisFabric.stop) {
                    switch (advancement.getDisplay().getFrame()) {
                        case GOAL -> DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.advancementGoal.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName())).replace("%advancement%", MarkdownSanitizer.escape(advancement.getDisplay().getTitle().getString()))).queue();
                        case TASK -> DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.advancementTask.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName())).replace("%advancement%", MarkdownSanitizer.escape(advancement.getDisplay().getTitle().getString()))).queue();
                        case CHALLENGE -> DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.advancementChallenge.replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName())).replace("%advancement%", MarkdownSanitizer.escape(advancement.getDisplay().getTitle().getString()))).queue();
                    }
                }
            });

            PlayerDeathCallback.EVENT.register((playerEntity, damageSource) -> {
                if (DisFabric.config.announceDeaths && !DisFabric.stop) {
                    DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.deathMessage.replace("%deathmessage%", MarkdownSanitizer.escape(damageSource.getDeathMessage(playerEntity).getString())).replace("%playername%", MarkdownSanitizer.escape(playerEntity.getEntityName()))).queue();
                }
            });

            ServerPlayConnectionEvents.JOIN.register((handler, $2, $3) -> {
                if (DisFabric.config.announcePlayers && !DisFabric.stop) {
                    DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.joinServer.replace("%playername%", MarkdownSanitizer.escape(handler.player.getEntityName()))).queue();
                }
            });

            ServerPlayConnectionEvents.DISCONNECT.register((handler, $2) -> {
                if (DisFabric.config.announcePlayers && !DisFabric.stop) {
                    DisFabric.bridgeChannel.sendMessage(DisFabric.config.texts.leftServer.replace("%playername%", MarkdownSanitizer.escape(handler.player.getEntityName()))).queue();
                }
            });
        }
    }
}
