package br.com.brforgers.mods.disfabric.mixins;

import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

    @Shadow
    public ServerPlayerEntity player;

    /**
     * Fun fact: Chat signing relies on the server always decorating the message the same way every time.
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/message/MessageDecorator;decorateChat(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/filter/FilteredMessage;Lnet/minecraft/network/message/MessageSignature;Z)Ljava/util/concurrent/CompletableFuture;"), method = "handleMessage")
    private void handleMessage(ChatMessageC2SPacket packet, FilteredMessage<String> message, CallbackInfo ci) {
        String msg = StringUtils.normalizeSpace(message.filteredOrElse(message.raw()));

        ServerChatCallback.EVENT.invoker().onServerChat(this.player, msg);
    }
}
