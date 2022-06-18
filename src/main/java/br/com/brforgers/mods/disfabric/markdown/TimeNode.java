package br.com.brforgers.mods.disfabric.markdown;// Created 2022-04-03T04:51:20

import org.commonmark.node.CustomNode;

/**
 * @author KJP12
 * @since 1.3.5
 **/
public class TimeNode extends CustomNode {
    public final long timestamp;
    public final TimeStyle style;

    TimeNode(long timestamp, TimeStyle style) {
        this.timestamp = timestamp;
        this.style = style;
    }
}
