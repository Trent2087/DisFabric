package br.com.brforgers.mods.disfabric.utils;

import br.com.brforgers.mods.disfabric.DisFabric;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern
            userMention = Pattern.compile("@([^@#:\\s][^@#:]{0,30}[^@#:\\s])(?:#(\\d{4}))?");

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
     * Produces a dummy string in the event of unknown entity.
     *
     * @param id   The ID of the unknown entity.
     * @param type The type of entity.
     * @param text The text to stylise.
     * @return The text stylised to the required spec.
     */
    public static MutableText convertUnknownEntityToFormattedText(long id, String type, String text) {
        var username = new LiteralText(text);
        var memberStyle = username.getStyle();
        memberStyle = memberStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Discord " + type + '\n' + id)));
        memberStyle = memberStyle.withColor(0x7289DA);
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
        var username = new LiteralText(prepend + (member == null ? user.getName() : member.getEffectiveName()));
        var memberStyle = username.getStyle();
        memberStyle = memberStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Discord: " + user.getAsTag() + '\n' + user.getIdLong())));
        memberStyle = memberStyle.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, '@' + user.getAsTag()));
        int color = member == null ? -1 : member.getColorRaw();
        memberStyle = memberStyle.withColor(color == -1 ? 0x7289DA : color);
        username.setStyle(memberStyle);
        return username;
    }

    /**
     * Converts the given role to a coloured mention.
     *
     * @param role    The role to get the details of.
     * @param prepend The string to prepend to the username.
     * @return The username formatted with a hover & click event.
     */
    public static MutableText convertRoleToFormattedText(Role role, String prepend) {
        var username = new LiteralText(prepend + role.getName());
        var memberStyle = username.getStyle();
        memberStyle = memberStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Discord Role\n" + role.getIdLong())));
        memberStyle = memberStyle.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, '@' + role.getName()));
        int color = role.getColorRaw();
        memberStyle = memberStyle.withColor(color == -1 ? 0x7289DA : color);
        username.setStyle(memberStyle);
        return username;
    }

    /**
     * Converts the given role to a coloured mention.
     *
     * @param channel The role to get the details of.
     * @param prepend The string to prepend to the username.
     * @return The username formatted with a hover & click event.
     */
    public static MutableText convertChannelToFormattedText(Channel channel, String prepend) {
        var username = new LiteralText(prepend + channel.getName());
        var memberStyle = username.getStyle();
        memberStyle = memberStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Discord Channel\n" + channel.getIdLong())));
        memberStyle = memberStyle.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, '#' + channel.getName()));
        memberStyle = memberStyle.withColor(0x7289DA);
        username.setStyle(memberStyle);
        return username;
    }
}
