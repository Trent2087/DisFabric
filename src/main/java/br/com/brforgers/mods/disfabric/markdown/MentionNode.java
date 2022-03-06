package br.com.brforgers.mods.disfabric.markdown;// Created 2022-04-03T04:28:50

import org.commonmark.node.CustomNode;

/**
 * @author KJP12
 * @since ${version}
 **/
public class MentionNode extends CustomNode {
    public final MentionType type;
    public final long id;

    MentionNode(MentionType type, long id) {
        this.type = type;
        this.id = id;
    }
}
