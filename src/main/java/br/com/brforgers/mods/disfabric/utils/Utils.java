package br.com.brforgers.mods.disfabric.utils;

import br.com.brforgers.mods.disfabric.DisFabric;
import com.mojang.brigadier.context.CommandContext;
import net.dv8tion.jda.api.entities.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern
            userMention = Pattern.compile("@([^@#:\\s][^@#:]{0,30}[^@#:\\s])(?:#(\\d{4}))?");

    private static final boolean fabricTailorAvailable = FabricLoader.getInstance().isModLoaded("fabrictailor");
    public static final int BLURPLE = 0x7289DA;

    /**
     * Provides a face avatar for use with webhooks and embeds,
     * should the source be a player.
     */
    public static String playerAvatarUrl(ServerCommandSource source) {
        PlayerEntity player = source.getPlayer();
        if (player != null) {
            return playerAvatarUrl(player);
        }
        return null;
    }

    /**
     * Provides a face avatar for use with webhooks and embeds.
     */
    public static String playerAvatarUrl(PlayerEntity player) {
        if (fabricTailorAvailable && player instanceof TailoredPlayer tailor) {
            return "https://mc-heads.net/avatar/" + tailor.getSkinId() + "/128";
        }
        return "https://mc-heads.net/avatar/" + player.getUuid() + "/128";
    }

    public static String playerName(PlayerEntity player) {
        return player.getDisplayName().getString();
    }

    // FIXME: Ignore content in codeblocks (wrapped in `, `` and ```)
    // FIXME: Make more efficient caching logic for mentions.
    //  Ideally, the lookup is constant time with linear search by hash of
    //  username, decrementing for fuzziness; O(n), rather than linear search
    //  on top of a linear search; O(n^2)
    public static String convertMentionsFromNames(String message) {

        if (!message.contains("@")) return message;

        StringBuilder discordString = new StringBuilder();
        Matcher matcher = userMention.matcher(message);

        while (matcher.find()) {
            Member member = null;
            String name = matcher.group(1);
            String discrim = matcher.group(2);
            int diff = Integer.MAX_VALUE;
            int match = 3;
            int len = 0;
            if (discrim != null && !discrim.isBlank()) {
                // Exact'o'match
                // Note: Will not continue attempting to match when faulted to avoid accidental pingage.
                member = DisFabric.guild.getMemberByTag(name, discrim);
                matcher.appendReplacement(discordString, member == null ? matcher.group() : member.getAsMention());
                continue;
            } else for (Member m : DisFabric.guild.getMembers()) {
                User user = m.getUser();
                String username = user.getName();
                String nickname = m.getNickname();
                if (name.equalsIgnoreCase(username)) {
                    if (match > 0) {
                        diff = Integer.MAX_VALUE;
                        match = 0;
                    }
                    int cmp = Math.abs(name.compareTo(username));
                    if (diff > cmp) {
                        diff = cmp;
                        member = m;
                        len = username.length();
                        if (cmp == 0 && name.equals(username)) break;
                    }
                } else if (match >= 1 && name.equalsIgnoreCase(nickname)) {
                    if (match > 1) {
                        diff = Integer.MAX_VALUE;
                        match = 1;
                    }
                    int cmp = Math.abs(name.compareTo(nickname));
                    if (diff > cmp) {
                        diff = cmp;
                        member = m;
                        len = nickname.length();
                    }
                } else if (match >= 2 && name.startsWith(username)) {
                    if (match > 2) {
                        diff = Integer.MAX_VALUE;
                        match = 2;
                    }
                    int cmp = Math.abs(name.compareTo(username));
                    if (diff > cmp) {
                        diff = cmp;
                        member = m;
                        len = username.length();
                    }
                } else if (match >= 3 && nickname != null && name.startsWith(nickname)) {
                    // if(match > 3) {
                    //     diff = Integer.MAX_VALUE;
                    //     match = 3;
                    // }
                    int cmp = Math.abs(name.compareTo(nickname));
                    if (diff > cmp) {
                        diff = cmp;
                        member = m;
                        len = nickname.length();
                    }
                }
            }
            matcher.appendReplacement(discordString, member == null ? matcher.group() : member.getAsMention() + matcher.group().substring(len + 1));
        }
        return matcher.appendTail(discordString).toString();
    }

    /**
     * Produces a fake chat message applicable for sending as a
     * {@link net.minecraft.network.message.MessageType#SYSTEM system message}.
     *
     * @param source The player to make the fake message for.
     * @param text   The fake message's contents.
     * @return A fake chat message.
     */
    public static Text createFakeChatMessage(ServerCommandSource source, Text text) {
        return createFakeChatMessage(source.getDisplayName(), text);
    }

    /**
     * Produces a fake chat message applicable for sending as a
     * {@link net.minecraft.network.message.MessageType#SYSTEM system message}.
     *
     * @param username The username to make the fake message for.
     * @param text     The fake message's contents.
     * @return A fake chat message.
     */
    public static Text createFakeChatMessage(Text username, Text text) {
        return Text.translatable("chat.type.text", username, text);
    }

    /**
     * Produces a dummy string in the event of unknown entity.
     *
     * @param id   The ID of the unknown entity.
     * @param type The type of entity.
     * @param text The text to stylise.
     * @return The text stylised to the required spec.
     */
    public static MutableText convertUnknownEntityToFormattedText(long id, String type, String text) {
        var username = Text.literal(text);
        var memberStyle = username.getStyle();
        memberStyle = memberStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Discord " + type + '\n' + id)));
        memberStyle = memberStyle.withColor(BLURPLE);
        username.setStyle(memberStyle);
        return username;
    }

    /**
     * Converts the given user & member to a coloured mention.
     *
     * @param user    The user to get the details of.
     * @param member  The user to get the nickname & colour from.
     * @param prepend The string to prepend to the username.
     * @return The username formatted with a hover & click event.
     */
    public static MutableText convertMemberToFormattedText(User user, Member member, String prepend) {
        var username = Text.literal(prepend + (member == null ? user.getName() : member.getEffectiveName()));
        var memberStyle = username.getStyle();
        memberStyle = memberStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Discord: " + user.getAsTag() + '\n' + user.getIdLong())));
        memberStyle = memberStyle.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, '@' + user.getAsTag()));
        int color = member == null ? Role.DEFAULT_COLOR_RAW : member.getColorRaw();
        memberStyle = memberStyle.withColor(color == Role.DEFAULT_COLOR_RAW ? BLURPLE : color);
        username.setStyle(memberStyle);
        return username;
    }

    /**
     * Converts the given role to a coloured mention.
     *
     * @param role    The role to get the details of.
     * @param prepend The string to prepend to the username.
     * @return The role formatted with a hover & click event.
     */
    public static MutableText convertRoleToFormattedText(Role role, String prepend) {
        var username = Text.literal(prepend + role.getName());
        var memberStyle = username.getStyle();
        memberStyle = memberStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Discord Role\n" + role.getIdLong())));
        memberStyle = memberStyle.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, '@' + role.getName()));
        int color = role.getColorRaw();
        memberStyle = memberStyle.withColor(color == Role.DEFAULT_COLOR_RAW ? BLURPLE : color);
        username.setStyle(memberStyle);
        return username;
    }

    /**
     * Converts the given channel to a coloured mention.
     *
     * @param channel The role to get the details of.
     * @param prepend The string to prepend to the username.
     * @return The channel formatted with a hover & click event.
     */
    public static MutableText convertChannelToFormattedText(Channel channel, String prepend) {
        var username = Text.literal(prepend + channel.getName());
        var memberStyle = username.getStyle();
        memberStyle = memberStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Discord Channel\n" + channel.getIdLong())));
        memberStyle = memberStyle.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, '#' + channel.getName()));
        memberStyle = memberStyle.withColor(BLURPLE);
        username.setStyle(memberStyle);
        return username;
    }

    /**
     * Converts the given channel into a clickable mention.
     *
     * @param channel The channel to get the details of.
     * @param prepend The string to prepend to the username.
     * @return The channel formatted with a hover & click event.
     */
    public static MutableText convertChannelToFormattedLink(GuildMessageChannel channel, String prepend) {
        var channelName = Text.literal(prepend + channel.getName());
        var channelStyle = channelName.getStyle();
        channelStyle = channelStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Discord Channel\n" + channel.getIdLong())));
        channelStyle = channelStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, convertChannelToLink(channel)));
        channelStyle = channelStyle.withColor(BLURPLE);
        channelName.setStyle(channelStyle);
        return channelName;
    }

    /**
     * Converts the given message into a clickable mention.
     *
     * @param message The channel to get the details of.
     * @param prepend The string to prepend to the username.
     * @return The channel formatted with a hover & click event.
     */
    public static MutableText convertMessageToFormattedLink(Message message, String prepend) {
        var channel = message.getChannel();
        var channelName = Text.literal(prepend + channel.getName());
        var channelStyle = channelName.getStyle();
        channelStyle = channelStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Discord Channel\n" + channel.getIdLong())));
        channelStyle = channelStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, message.getJumpUrl()));
        channelStyle = channelStyle.withColor(BLURPLE);
        channelName.setStyle(channelStyle);
        return channelName;
    }

    /**
     * Converts the given channel into a link.
     *
     * @param channel The channel to convert into a link.
     * @return The link to the channel.
     */
    public static String convertChannelToLink(Channel channel) {
        if (channel instanceof GuildChannel guildChannel) {
            return "https://discord.com/channels/" + guildChannel.getGuild().getIdLong() + "/" + guildChannel.getIdLong();
        } else {
            return "https://discord.com/channels/@me/" + channel.getIdLong();
        }
    }

    public static MutableText createTryAgain(CommandContext<ServerCommandSource> context) {
        return Text.translatable("disfabric.tryagain").setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, context.getInput())));
    }
}
