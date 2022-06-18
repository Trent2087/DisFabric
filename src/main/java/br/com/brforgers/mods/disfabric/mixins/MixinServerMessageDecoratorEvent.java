package br.com.brforgers.mods.disfabric.mixins;// Created 2022-17-06T13:09:56

import br.com.brforgers.mods.disfabric.DisFabric;
import br.com.brforgers.mods.disfabric.WeakCacheTable;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Nasty hack for allowing for caching otherwise undeterminisic results to
 * allow chat signing to work correctly for stuff like timestamps, and to
 * help reduce load of spam.
 *
 * @author KJP12
 * @since 1.4.0
 **/
@Pseudo
@Mixin(ServerMessageDecoratorEvent.class)
public class MixinServerMessageDecoratorEvent {
    private static final WeakCacheTable<ServerPlayerEntity, Text, CompletableFuture<Text>> decoratorCache = new WeakCacheTable<>();

    static {
        DisFabric.scheduler.scheduleAtFixedRate(decoratorCache::clean, 15, 15, TimeUnit.SECONDS);
    }

    /**
     * Minimally invasive hack to allow for caching to occur for every usage
     * of {@link MinecraftServer#getMessageDecorator()}.
     *
     * @param function The original invoker generator.
     * @return The caching invoker.
     * @author KJP12
     * @reason This was the least invasive way to allow for caching.
     */
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/EventFactory;createWithPhases(Ljava/lang/Class;Ljava/util/function/Function;[Lnet/minecraft/util/Identifier;)Lnet/fabricmc/fabric/api/event/Event;"), require = 0)
    private static Function<MessageDecorator[], MessageDecorator> disfabric$interceptAndCache(Function<MessageDecorator[], MessageDecorator> function) {
        return decorators -> {
            // FIXME: apply full API of the decorator, not just the `decorate` function
            var original = function.apply(decorators);
            return (sender, message) -> decoratorCache.computeIfMismatch(sender, message, original::decorate);
        };
    }
}
