package br.com.brforgers.mods.disfabric;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;

@Config(name = DisFabric.MOD_ID)
public class Configuration implements ConfigData {
    @Comment(value = "Sets if DisFabric Should Modify In-Game Chat Messages")
    @ConfigEntry.Category(value = "MinecraftChat")
    public boolean modifyChatMessages = true;

    @Comment(value = "Guild ID")
    @ConfigEntry.Category(value = "Discord")
    public String guildId = "";

    @Comment(value = "Bot Token; see https://discordpy.readthedocs.io/en/latest/discord.html")
    @ConfigEntry.Category(value = "Discord")
    public String botToken = "";

    @Comment(value = "Bot Game Status; What will be displayed on the bot's game status (leave empty for nothing)")
    @ConfigEntry.Category(value = "Discord")
    public String botGameStatus = "";

    @Comment(value = "Enable Webhook; If enabled, player messages will be send using a webhook with the players name and head, instead of a regular message.")
    @ConfigEntry.Category(value = "Discord")
    public boolean isWebhookEnabled = true;

    @Comment(value = "Webhook URL; see https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks")
    @ConfigEntry.Category(value = "Discord")
    public String webhookURL = "";

    @Comment(value = "A list of role IDs that have admin privileges. Add role IDs like this: adminRoleIds=[123456789012345678, 987654321098765432]")
    @ConfigEntry.Category(value = "Discord")
    public List<Long> adminRoleIds = new ArrayList<>();

    @Comment(value = "Allow other bots to run !console commands")
    @ConfigEntry.Category(value = "Discord")
    public boolean allowBots = false;

    @Comment(value = "Channel id in Discord")
    @ConfigEntry.Category(value = "Discord")
    public String channelId = "";

    @Comment(value = "If you enabled \"Server Members Intent\" in the bot's config page, change it to true. (This is only necessary if you want to enable discord mentions inside the game)")
    @ConfigEntry.Category(value = "Discord")
    public boolean membersIntents = false;

    @Comment(value = "Should announce when a players join/leave the server?")
    @ConfigEntry.Category(value = "Discord")
    public boolean announcePlayers = true;

    @Comment(value = "Should announce when a players get an advancement?")
    @ConfigEntry.Category(value = "Discord")
    public boolean announceAdvancements = true;

    @Comment(value = "Should announce when a player die?")
    @ConfigEntry.Category(value = "Discord")
    public boolean announceDeaths = true;

    public Texts texts = new Texts();

    public static class Texts {

        @Comment(value = "Minecraft -> Discord\n"+
                "Player chat message (Only used when Webhook is disabled)\n"+
                "Available placeholders:\n"+
                "%playername% | Player name\n"+
                "%playermessage% | Player message")
        @ConfigEntry.Category(value = "Texts")
        public String playerMessage = "**%playername%:** %playermessage%";

        @Comment(value = "Minecraft -> Discord\n"+
                "Server started message")
        @ConfigEntry.Category(value = "Texts")
        public String serverStarted = ":green_circle: **Server started!**";

        @Comment(value = "Minecraft -> Discord\n"+
                "Server stopped message")
        @ConfigEntry.Category(value = "Texts")
        public String serverStopped = ":octagonal_sign: **Server stopped!**";

        @Comment(value = "Minecraft -> Discord\n"+
                "Join server\n"+
                "Available placeholders:\n"+
                "%playername% | Player name")
        @ConfigEntry.Category(value = "Texts")
        public String joinServer = "**%playername% joined the game**";

        @Comment(value = "Minecraft -> Discord\n"+
                "Left server\n"+
                "Available placeholders:\n"+
                "%playername% | Player name")
        @ConfigEntry.Category(value = "Texts")
        public String leftServer = "**%playername% left the game**";

        @Comment(value = "Minecraft -> Discord\n"+
                "Death message\n"+
                "Available placeholders:\n"+
                "%playername% | Player name\n"+
                "%deathmessage% | Death message")
        @ConfigEntry.Category(value = "Texts")
        public String deathMessage = ":skull: **%deathmessage%**";

        @Comment(value = "Minecraft -> Discord\n"+
                "Advancement type task message\n"+
                "Available placeholders:\n"+
                "%playername% | Player name\n"+
                "%advancement% | Advancement name")
        @ConfigEntry.Category(value = "Texts")
        public String advancementTask = "%playername% has made the advancement **[%advancement%]**";

        @Comment(value = "Minecraft -> Discord\n"+
                "Advancement type challenge message\n"+
                "Available placeholders:\n"+
                "%playername% | Player name\n"+
                "%advancement% | Advancement name")
        @ConfigEntry.Category(value = "Texts")
        public String advancementChallenge = "%playername% has completed the challenge **[%advancement%]**";

        @Comment(value = "Minecraft -> Discord\n"+
                "Advancement type goal message\n"+
                "Available placeholders:\n"+
                "%playername% | Player name\n"+
                "%advancement% | Advancement name")
        @ConfigEntry.Category(value = "Texts")
        public String advancementGoal = "%playername% has reached the goal **[%advancement%]**";

        @Comment(value = "Discord -> Minecraft\n"+
                "Colored part of the message, this part of the message will receive the same color as the role in the discord, comes before the colorless part\n"+
                "Available placeholders:\n"+
                "%discordname% | User nickname in the guild\n"+
                "%message% | The message")
        @ConfigEntry.Category(value = "Texts")
        public String coloredText = "[Discord] ";

        @Comment(value = "Discord -> Minecraft\n"+
                "Colorless (white) part of the message, I think you already know what it is by the other comment\n"+
                "Available placeholders:\n"+
                "%discordname% | Nickname of the user in the guild\n"+
                "%message% | The message")
        @ConfigEntry.Category(value = "Texts")
        public String colorlessText = "<%discordname%> %message%";

    }
}
