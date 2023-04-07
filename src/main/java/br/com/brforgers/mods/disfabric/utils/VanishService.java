package br.com.brforgers.mods.disfabric.utils;

import br.com.brforgers.mods.disfabric.listeners.MinecraftEventListener;
import com.mojang.logging.LogUtils;
import me.drex.vanish.api.VanishAPI;
import me.drex.vanish.api.VanishEvents;
import me.drex.vanish.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;

/**
 * @author Octal
 */
public final class VanishService {
    private static final Logger logger = LogUtils.getLogger();
    private static final boolean vanishAvailable = FabricLoader.getInstance().isModLoaded("melius-vanish");
    private static boolean configAbiBroke;

    public static boolean isPlayerVanished(ServerPlayerEntity player) {
        return vanishAvailable && VanishAPI.isVanished(player);
    }

    public static void listen() {
        if (vanishAvailable) {
            VanishEvents.VANISH_EVENT.register(MinecraftEventListener::onPlayerVanishChange);
        }
    }

    public static boolean isChatDisabled(ServerPlayerEntity player) {
        return isPlayerVanished(player) && queryDisableChat();
    }

    private static boolean queryDisableChat() {
        try {
            return ConfigManager.vanish().disableChat;
        } catch (LinkageError e) {
            if (!configAbiBroke) {
                logger.error("Warning: ABI broke with Vanish. Assuming chat is disabled.", e);
                configAbiBroke = true;
            }
            return true;
        }
    }
}
