package br.com.brforgers.mods.disfabric;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = DisFabric.MOD_ID)
public class Configuration implements ConfigData {
    @Comment(value = "Do you only what to use disfabric for commands?")
    @ConfigEntry.Category(value = "CommandsOnly")
    public boolean commandsOnly = false;

    @Comment(value = "Sets if DisFabric Should Modify In-Game Chat Messages")
    @ConfigEntry.Category(value = "MinecraftChat")
    public boolean modifyChatMessages = true;

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

    @Comment(value = """
            Admins ids in Discord; see https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-
            If more than one, enclose each id in quotation marks separated by commas, like this:
            "adminsIds": [\s
            \t\t"000",
            \t\t"111",
            \t\t"222"
            \t]""")
    @ConfigEntry.Category(value = "Discord")
    public String[] adminsIds = {""};

    @Comment(value = "Channel id in Discord")
    @ConfigEntry.Category(value = "Discord")
    public String channelId = "";

    @Comment("Bug reports channel in Discord. Will be disabled when set to 0.")
    @ConfigEntry.Category("Discord")
    public long bugReportChannel = 0L;

    @Comment("Create thread on bug report? Will be automatically disabled if disallowed.")
    @ConfigEntry.Category("Discord")
    public boolean bugReportAutoThread = true;

    @Comment("Player reports channel in Discord. Will be disabled when set to 0.")
    @ConfigEntry.Category("Discord")
    public long userReportChannel = 0L;

    @Comment("Role to ping on player report. Staff role recommended. Will not ping when set to 0.")
    @ConfigEntry.Category("Discord")
    public long userReportStaffRole = 0L;

    @Comment("Suggestions channel in Discord. Will be disabled when set to 0.")
    @ConfigEntry.Category("Discord")
    public long suggestionChannel = 0L;

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

        @Comment(value = """
                Minecraft -> Discord
                Player chat message (Only used when Webhook is disabled)
                Available placeholders:
                %playername% | Player name
                %playermessage% | Player message""")
        @ConfigEntry.Category(value = "Texts")
        public String playerMessage = "**%playername%:** %playermessage%";

        @Comment(value = "Minecraft -> Discord\n"+
                "Server started message")
        @ConfigEntry.Category(value = "Texts")
        public String serverStarted = "**Server started!**";

        @Comment(value = "Minecraft -> Discord\n"+
                "Server stopped message")
        @ConfigEntry.Category(value = "Texts")
        public String serverStopped = "**Server stopped!**";

        @Comment(value = """
                Minecraft -> Discord
                Join server
                Available placeholders:
                %playername% | Player name""")
        @ConfigEntry.Category(value = "Texts")
        public String joinServer = "**%playername% joined the game**";

        @Comment(value = """
                Minecraft -> Discord
                Left server
                Available placeholders:
                %playername% | Player name""")
        @ConfigEntry.Category(value = "Texts")
        public String leftServer = "**%playername% left the game**";

        @Comment(value = """
                Minecraft -> Discord
                Death message
                Available placeholders:
                %playername% | Player name
                %deathmessage% | Death message""")
        @ConfigEntry.Category(value = "Texts")
        public String deathMessage = "**%deathmessage%**";

        @Comment(value = """
                Minecraft -> Discord
                Advancement type task message
                Available placeholders:
                %playername% | Player name
                %advancement% | Advancement name""")
        @ConfigEntry.Category(value = "Texts")
        public String advancementTask = "%playername% has made the advancement **[%advancement%]**";

        @Comment(value = """
                Minecraft -> Discord
                Advancement type challenge message
                Available placeholders:
                %playername% | Player name
                %advancement% | Advancement name""")
        @ConfigEntry.Category(value = "Texts")
        public String advancementChallenge = "%playername% has completed the challenge **[%advancement%]**";

        @Comment(value = """
                Minecraft -> Discord
                Advancement type goal message
                Available placeholders:
                %playername% | Player name
                %advancement% | Advancement name""")
        @ConfigEntry.Category(value = "Texts")
        public String advancementGoal = "%playername% has reached the goal **[%advancement%]**";
    }
}
