package br.com.brforgers.mods.disfabric.markdown;// Created 2022-04-03T04:25:43

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author KJP12
 * @since ${version}
 **/
public class DiscordChatExtension implements Parser.ParserExtension {
    public static final Extension INSTANCE = new DiscordChatExtension();
    private static final Pattern
            USER = Pattern.compile("@!?(\\d++)"),
            ROLE = Pattern.compile("@&(\\d++)"),
            CHANNEL = Pattern.compile("#(\\d++)"),
            EMOJI = Pattern.compile("(a?):([a-zA-Z0-9_-]++):(\\d++)"),
            TIME = Pattern.compile("t:(-?\\d++)(?::([dftDFTR]))?");

    private DiscordChatExtension() {
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new MentionDelimiterProcessor());
    }

    private static class MentionDelimiterProcessor implements DelimiterProcessor {
        @Override
        public char getOpeningCharacter() {
            return '<';
        }

        @Override
        public char getClosingCharacter() {
            return '>';
        }

        @Override
        public int getMinLength() {
            return 1;
        }

        @Override
        public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
            if (openingRun.length() >= 1 && closingRun.length() >= 1) {
                Text opener = openingRun.getOpener();
                Text closer = closingRun.getCloser();
                StringBuilder why = new StringBuilder();
                for (var node : Nodes.between(opener, closer)) {
                    if (!(node instanceof Text text)) return 0;
                    why.append(text.getLiteral());
                }
                if (why.isEmpty()) return 0;
                Matcher matcher = USER.matcher(why);
                if (matcher.matches()) {
                    insertBetween(opener, closer, new MentionNode(MentionType.USER, Long.parseUnsignedLong(matcher.group(1))));
                    return 1;
                }
                if (matcher.usePattern(ROLE).matches()) {
                    insertBetween(opener, closer, new MentionNode(MentionType.ROLE, Long.parseUnsignedLong(matcher.group(1))));
                    return 1;
                }
                if (matcher.usePattern(CHANNEL).matches()) {
                    insertBetween(opener, closer, new MentionNode(MentionType.CHANNEL, Long.parseUnsignedLong(matcher.group(1))));
                    return 1;
                }
                if (matcher.usePattern(EMOJI).matches()) {
                    insertBetween(opener, closer, new EmoteNode("a".equals(matcher.group(1)), matcher.group(2), Long.parseUnsignedLong(matcher.group(3))));
                    return 1;
                }
                if (matcher.usePattern(TIME).matches()) {
                    insertBetween(opener, closer, new TimeNode(Long.parseLong(matcher.group(1)), TimeStyle.of(matcher.group(2))));
                    return 1;
                }
            }
            return 0;
        }

        /**
         * copy from mdchat
         */
        private void insertBetween(Text opener, Text closer, Node node) {
            for (var sibling : Nodes.between(opener, closer)) {
                node.appendChild(sibling);
            }

            opener.insertAfter(node);
        }
    }
}
