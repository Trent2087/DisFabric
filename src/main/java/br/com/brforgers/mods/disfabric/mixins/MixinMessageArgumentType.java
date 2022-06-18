package br.com.brforgers.mods.disfabric.mixins;// Created 2022-17-06T05:54:43

import br.com.brforgers.mods.disfabric.utils.DecoratorContainer;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author KJP12
 * @since 1.4.0
 **/
@Mixin(MessageArgumentType.class)
public class MixinMessageArgumentType implements DecoratorContainer {
    @Unique
    private MessageDecorator decorator;

    @Inject(method = "decorate(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/argument/MessageArgumentType$MessageFormat;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("RETURN"), cancellable = true)
    private void disfabric$postDecorate(ServerCommandSource source, MessageArgumentType.MessageFormat format, CallbackInfoReturnable<CompletableFuture<Text>> cir) {
        if (this.decorator != null) {
            CompletableFuture<Text> ret = cir.getReturnValue();
            if (ret.isDone()) {
                cir.setReturnValue(ret.thenCompose(disfabric$postDecorator(source.getPlayer())));
                return;
            }
            cir.setReturnValue(ret.thenComposeAsync(disfabric$postDecorator(source.getPlayer())));
        }
    }

    @Unique
    private Function<Text, CompletableFuture<Text>> disfabric$postDecorator(ServerPlayerEntity source) {
        return text -> decorator.decorate(source, text);
    }

    @Override
    public void disfabric$setDecorator(MessageDecorator decorator) {
        this.decorator = decorator;
    }

    @Override
    public MessageDecorator disfabric$asDecorator(MinecraftServer server) {
        return (sender, text) -> server.getMessageDecorator().decorate(sender, text).thenComposeAsync(disfabric$postDecorator(sender));
    }
}
