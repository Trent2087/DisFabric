package br.com.brforgers.mods.disfabric.markdown;// Created 2022-05-03T02:17:39

import org.commonmark.node.CustomNode;

/**
 * @author KJP12
 * @since ${version}
 **/
public class EmoteNode extends CustomNode {
    public final boolean animated;
    public final String name;
    public final long snowflake;

    EmoteNode(boolean animated, String name, long snowflake) {
        this.animated = animated;
        this.name = name;
        this.snowflake = snowflake;
    }
}
