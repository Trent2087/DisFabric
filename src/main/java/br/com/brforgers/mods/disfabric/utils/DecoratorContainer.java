package br.com.brforgers.mods.disfabric.utils;// Created 2022-17-06T06:03:19

import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.MinecraftServer;

/**
 * Decorator container interface for anything that has a
 * custom decorator attached.
 *
 * @author KJP12
 * @see br.com.brforgers.mods.disfabric.mixins.MixinMessageArgumentType
 * @since 1.4.0
 **/
public interface DecoratorContainer {
    void disfabric$setDecorator(MessageDecorator decorator);

    MessageDecorator disfabric$asDecorator(MinecraftServer server);
}
