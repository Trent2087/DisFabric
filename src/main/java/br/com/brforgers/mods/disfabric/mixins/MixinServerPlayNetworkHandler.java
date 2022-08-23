package br.com.brforgers.mods.disfabric.mixins;

import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

    @Shadow
    public ServerPlayerEntity player;

    /**
     * Fun fact: Chat signing relies on the server always decorating the message the same way every time.
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/message/MessageDecorator;decorate(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/SignedMessage;)Ljava/util/concurrent/CompletableFuture;"), method = "method_45065")
    private void handleMessage(final SignedMessage signedMessage, final CallbackInfoReturnable<CompletableFuture> cir) {
        String msg = StringUtils.normalizeSpace(signedMessage.getSignedContent().plain());

        ServerChatCallback.EVENT.invoker().onServerChat(this.player, msg);
    }
}
