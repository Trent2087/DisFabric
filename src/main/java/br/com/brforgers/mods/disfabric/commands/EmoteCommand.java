package br.com.brforgers.mods.disfabric.commands;

import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import br.com.brforgers.mods.disfabric.utils.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Emote command for shrug, tableflip and unflip that uses custom
 * decorators and supports chat signing.
 *
 * <h2>Examples</h2>
 * <ul>
 *     <li>{@code /shrug Whatever}<br/>
 *     {@code &lt;Player&gt; Whatever ¯\_(ツ)_/¯}</li>
 *     <li>{@code /tableflip This is better!}<br/>
 *     {@code &lt;Player&gt; This is better! (╯°□°）╯︵ ┻━┻}</li>
 *     <li>{@code /unflip Alright, calm down}<br/>
 *     {@code &lt;Player&gt; Alright, calm down ┬─┬ ノ( ゜-゜ノ)}</li>
 * </ul>
 *
 * @see MixinMessageArgumentType
 * @see MixinMessageArgumentType$SignedMessage
 * @see net.minecraft.server.command.MeCommand
 */
public class EmoteCommand implements MessageDecorator, CommandRegistrationCallback {
    public static final String SHRUG = "¯\\_(ツ)_/¯";
    public static final String TABLE_FLIP = "(╯°□°）╯︵ ┻━┻";
    public static final String UNFLIP = "┬─┬ ノ( ゜-゜ノ)";
    private final MessageArgumentType message;
    private final String NAME;
    private final String EMOTE;

    /**
     * Initializer of the emote command registration callback &amp; decorator.
     *
     * @param name  The command name, usually {@code "shrug"}, {@code "tableflip"}
     *              or {@code "unflip"}.
     * @param emote The raw emote string. May be {@link #SHRUG}, {@link #TABLE_FLIP},
     *              {@link #UNFLIP} or a custom string.
     */
    public EmoteCommand(String name, String emote) {
        this.NAME = name;
        this.EMOTE = emote;
        this.message = MessageArgumentType.message();
    }

    /**
     * Registers the command to the given dispatcher at call time.
     *
     * @param dispatcher     The dispatcher to register command {@link #NAME} to.
     * @param registryAccess Ignored.
     * @param environment    Current running environment. Ignored.
     */
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal(NAME).then(argument("message", message).executes(context -> {
            var source = context.getSource();

            MessageArgumentType.getSignedMessage(context, "message", message -> {
                var player = source.getPlayer();
                var playerManager = source.getServer().getPlayerManager();


                // Fun fact: It's perfectly fine to do this as the client will
                // just see it as signed, unaltered in a significant manner.
                var redecorated = message.getContent().copy().append(' ' + EMOTE);
                var signedRedecorated = message.withUnsignedContent(redecorated);

                playerManager.broadcast(signedRedecorated, source, MessageType.params(MessageType.CHAT, source));

                if (player != null) {
                    ServerChatCallback.EVENT.invoker().onServerChat(player, message.getSignedContent() + ' ' + EMOTE);
                }
            });
            return Command.SINGLE_SUCCESS;
        })).executes(context -> {
            var source = context.getSource();
            source.getServer().getPlayerManager().broadcast(Utils.createFakeChatMessage(source, Text.of(EMOTE)), false);
            var player = source.getPlayer();
            if (player != null) {
                ServerChatCallback.EVENT.invoker().onServerChat(player, EMOTE);
            }
            return Command.SINGLE_SUCCESS;
        }));
    }

    /**
     * Appends the {@link #EMOTE} at the end of the message as part of the command.
     *
     * @param sender  The sender of the message.
     * @param message The message to append {@link #EMOTE} to.
     * @return The message with {@link #EMOTE} wrapped in a {@link CompletableFuture}.
     */
    @Override
    public CompletableFuture<Text> decorate(@Nullable ServerPlayerEntity sender, Text message) {
        return CompletableFuture.completedFuture(message.copy().append(' ' + EMOTE));
    }
}
