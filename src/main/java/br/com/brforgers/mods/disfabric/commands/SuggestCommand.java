package br.com.brforgers.mods.disfabric.commands;// Created 2022-28-05T08:57:53

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * @author Ampflower
 * @since 1.3.5
 **/
public final class SuggestCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var suggestionChannel = DisFabric.suggestionChannel;
        if (suggestionChannel != null) {
            var suggest = literal("suggest")
                    .requires(src -> src.getEntity() instanceof ServerPlayerEntity)
                    .then(argument("description", greedyString()).executes(ctx -> {
                        var source = ctx.getSource();
                        // We've already checked via `.requires` that this will guarantee a
                        // ServerPlayerEntity being present.
                        var reporter = source.getPlayerOrThrow();
                        var description = getString(ctx, "description");
                        var embed = new EmbedBuilder()
                                .setDescription(description)
                                .setAuthor(reporter.getEntityName(), null, Utils.playerAvatarUrl(reporter))
                                .setFooter(reporter.getUuidAsString())
                                .build();
                        suggestionChannel.sendMessageEmbeds(embed).queue(
                                msg -> source.sendFeedback(Text.translatable("disfabric.suggest.success", Utils.convertMessageToFormattedLink(msg, "#")), false),
                                err -> {
                                    DisFabric.logger.error("Failed to process suggestion {} by {}:", description, reporter, err);
                                    source.sendError(Text.translatable("disfabric.suggest.failure", err.getMessage(), Utils.createTryAgain(ctx)));
                                }
                        );
                        return Command.SINGLE_SUCCESS;
                    }));
            dispatcher.register(suggest);
        }
    }
}
