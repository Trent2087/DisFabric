package br.com.brforgers.mods.disfabric.commands;// Created 2022-27-05T22:09:41

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * {@code /report bug &lt;description&gt;}<br/>
 * {@code /report player &lt;player&gt; &lt;description&gt;}
 *
 * @author KJP12
 * @since ${version}
 **/
public class ReportCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        boolean enable = false;
        var report = literal("report").requires(src -> src.getEntity() instanceof ServerPlayerEntity);

        var bugReportChannel = DisFabric.bugReportChannel;
        if (bugReportChannel != null) {
            enable = true;
            var bug = literal("bug")
                    .then(argument("description", greedyString()).executes(ctx -> {
                        var source = ctx.getSource();
                        var reporter = source.getPlayer();
                        var description = getString(ctx, "description");
                        var embed = new EmbedBuilder().setDescription(description)
                                .setAuthor(reporter.getEntityName(), null, Utils.playerAvatarUrl(reporter))
                                .setFooter(reporter.getUuidAsString())
                                .build();
                        bugReportChannel.sendMessageEmbeds(embed).queue(
                                DisFabric.config.bugReportAutoThread ?
                                        msg -> msg.createThreadChannel(description.length() < 32 ? description : description.substring(0, 32)).queue(
                                                thread -> source.sendFeedback(new TranslatableText("disfabric.report.bug.success", Utils.convertChannelToFormattedLink(thread, "#")), false),
                                                err -> {
                                                    DisFabric.logger.warn("Failed to create thread for {}:", msg, err);
                                                    // We still have a message, so, technically still successful regardless of if the thread failed to be created.
                                                    source.sendFeedback(new TranslatableText("disfabric.report.bug.success", Utils.convertMessageToFormattedLink(msg, "#")), false);
                                                }) :
                                        msg -> source.sendFeedback(new TranslatableText("disfabric.report.bug.success", Utils.convertMessageToFormattedLink(msg, "#")), false),
                                err -> {
                                    DisFabric.logger.error("Failed to report bug {} by {}:", description, reporter, err);
                                    source.sendError(new TranslatableText("disfabric.report.bug.failure", err.getMessage(), Utils.createTryAgain(ctx)));
                                });
                        return Command.SINGLE_SUCCESS;
                    }));
            report.then(bug);
        }

        var userReportChannel = DisFabric.userReportChannel;
        if (userReportChannel != null) {
            enable = true;
            var player = literal("player").then(argument("target", player())
                    .then(argument("description", greedyString()).executes(ctx -> {
                        var source = ctx.getSource();
                        var reporter = source.getPlayer();
                        var reported = getPlayer(ctx, "target");
                        var description = getString(ctx, "description");
                        var embed = new EmbedBuilder().setDescription(description)
                                .setAuthor(reported.getEntityName() + " - " + reported.getUuid(), null, Utils.playerAvatarUrl(reported))
                                .setFooter("Reported by " + reporter.getEntityName() + " - " + reporter.getUuid(), Utils.playerAvatarUrl(reporter))
                                .build();
                        var messageAction = userReportChannel.sendMessageEmbeds(embed);
                        var staff = DisFabric.config.userReportStaffRole;
                        if (staff != 0L) {
                            messageAction.append("<@&").append(Long.toUnsignedString(staff)).append(">");
                        }
                        messageAction.queue(
                                // Note: broadcastToOps = true is intentional for this.
                                msg -> source.sendFeedback(new TranslatableText("disfabric.report.user.success", reported.getDisplayName()), true),
                                err -> {
                                    DisFabric.logger.error("Failed to report player {}: {} by {}: ", reported, description, reporter, err);
                                    source.sendError(new TranslatableText("disfabric.report.user.failure", reported.getDisplayName(), err.getMessage(), Utils.createTryAgain(ctx)));
                                }
                        );
                        return Command.SINGLE_SUCCESS;
                    })));
            report.then(player);
        }

        if (enable) {
            dispatcher.register(report);
        }
    }
}
