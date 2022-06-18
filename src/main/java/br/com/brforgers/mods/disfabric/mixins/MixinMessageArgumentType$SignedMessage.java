package br.com.brforgers.mods.disfabric.mixins;// Created 2022-17-06T08:06:18

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.utils.CustomisedSignedMessage;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

/**
 * @author KJP12
 * @since 1.4.0
 **/
@Mixin(MessageArgumentType.SignedMessage.class)
public abstract class MixinMessageArgumentType$SignedMessage implements CustomisedSignedMessage {

    @Shadow
    protected abstract CompletableFuture<FilteredMessage<Text>> filter(ServerCommandSource source, Text message);

    @Shadow
    @Final
    private Text formatted;

    @Shadow
    @Final
    private MessageSignature signature;

    @Shadow
    @Final
    private boolean signedPreview;

    @Shadow
    @Nullable
    protected abstract SignedMessage getVerifiable(FilteredMessage<SignedMessage> decorated);

    @Shadow
    protected abstract void logInvalidSignatureWarning(ServerCommandSource source, SignedMessage message);

    @Override
    public CompletableFuture<FilteredMessage<SignedMessage>> disfabric$decorate(ServerCommandSource source, MessageDecorator decorator) {
        var future = filter(source, formatted).thenComposeAsync(filtered -> decorator.decorateChat(source.getPlayer(), filtered, signature, signedPreview), source.getServer()).thenApply(decorated -> {
            var signedMessage = getVerifiable(decorated);
            if (signedMessage != null) {
                logInvalidSignatureWarning(source, signedMessage);
            }
            return decorated;
        });

        future.exceptionally(throwable -> {
            DisFabric.logger.error("Encountered unexpected exception while resolving chat message argument from '{}' with argument '{}'", source.getDisplayName(), formatted, throwable);
            return null;
        });

        return future;
    }
}
