package br.com.brforgers.mods.disfabric.markdown;// Created 2022-30-12T01:01:33

import org.commonmark.node.CustomNode;

/**
 * @author Ampflower
 * @since ${version}
 **/
public class SpecialStringNode extends CustomNode {
    public final SpecialStringType type;

    SpecialStringNode(SpecialStringType type) {
        this.type = type;
    }
}
