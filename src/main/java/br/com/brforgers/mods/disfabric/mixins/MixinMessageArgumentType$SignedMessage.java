package br.com.brforgers.mods.disfabric.mixins;// Created 2022-17-06T08:06:18

import br.com.brforgers.mods.disfabric.utils.CustomisedSignedMessage;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.FilteredMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Ampflower
 * @since 1.4.0
 **/
@Mixin(MessageArgumentType.SignedMessage.class)
public abstract class MixinMessageArgumentType$SignedMessage implements CustomisedSignedMessage {

    @Shadow
    protected abstract CompletableFuture<FilteredMessage> filterText(final ServerCommandSource source, final String text);

    @Shadow
    @Final
    private SignedMessage signedArgument;

    @Override
    public void disfabric$decorate(ServerCommandSource source, Consumer<SignedMessage> callback, MessageDecorator decorator) {
        MinecraftServer minecraftServer = source.getServer();
        source.getMessageChainTaskQueue().append(() -> {
            var completableFuture = filterText(source, signedArgument.getSignedContent().plain());
            var completableFuture2 = decorator.decorate(source.getPlayer(), this.signedArgument);
            return CompletableFuture.allOf(completableFuture, completableFuture2).thenAcceptAsync((void_) -> {
                var signedMessage = completableFuture2.join().withFilterMask((completableFuture.join()).mask());
                callback.accept(signedMessage);
            }, minecraftServer);
        });
    }
}
