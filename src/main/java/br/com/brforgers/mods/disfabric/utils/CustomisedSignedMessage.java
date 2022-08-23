package br.com.brforgers.mods.disfabric.utils;// Created 2022-17-06T08:07:24

import net.minecraft.network.message.MessageDecorator;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Consumer;

/**
 * {@link net.minecraft.command.argument.MessageArgumentType.SignedMessage}
 * with a custom decorator input.
 *
 * @author KJP12
 * @since 1.4.0
 **/
public interface CustomisedSignedMessage {
    void disfabric$decorate(ServerCommandSource source, Consumer<SignedMessage> callback, MessageDecorator decorator);
}
