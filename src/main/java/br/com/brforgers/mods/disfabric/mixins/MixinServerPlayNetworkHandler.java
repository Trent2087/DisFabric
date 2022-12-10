package br.com.brforgers.mods.disfabric.mixins;

import br.com.brforgers.mods.disfabric.events.ServerChatCallback;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler {

    @Shadow
    public ServerPlayerEntity player;

    /**
     * Fun fact: Chat signing relies on the server always decorating the message the same way every time.
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;getSignedMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;Lnet/minecraft/network/message/LastSeenMessageList;)Lnet/minecraft/network/message/SignedMessage;", shift = At.Shift.BY, by = 2), method = "method_44900", locals = LocalCapture.CAPTURE_FAILHARD)
    private void handleMessage(final ChatMessageC2SPacket chatMessageC2SPacket, final Optional optional, final CallbackInfo ci, final SignedMessage signedMessage) {
        String msg = StringUtils.normalizeSpace(signedMessage.getSignedContent());

        ServerChatCallback.EVENT.invoker().onServerChat(this.player, msg);
    }
}
