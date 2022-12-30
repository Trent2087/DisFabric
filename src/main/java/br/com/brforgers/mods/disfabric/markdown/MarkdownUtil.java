package br.com.brforgers.mods.disfabric.markdown;// Created 2022-30-12T01:02:22

import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.Text;

/**
 * @author KJP12
 * @since ${version}
 **/
final class MarkdownUtil {

    /**
     * Copied from Markdown-Chat.
     */
    static void insertBetween(Text opener, Text closer, Node node) {
        for (var sibling : Nodes.between(opener, closer)) {
            node.appendChild(sibling);
        }

        opener.insertAfter(node);
    }
}
