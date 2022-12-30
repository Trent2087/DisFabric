package br.com.brforgers.mods.disfabric.markdown;// Created 2022-30-12T02:34:00

import br.com.brforgers.mods.disfabric.commands.EmoteCommand;
import com.google.common.collect.ImmutableMap;
import net.minecraft.text.Text;
import org.commonmark.node.Node;

import java.util.Map;

/**
 * Special-cased strings to mimick Discord's special casing of strings.
 *
 * @author KJP12
 * @since ${version}
 **/
public enum SpecialStringType {
    SHRUG(EmoteCommand.SHRUG);

    private static final SpecialStringType[] cache = values();
    public static final Map<String, SpecialStringType> nameToTypeStore;

    static {
        final var builder = ImmutableMap.<String, SpecialStringType>builder();
        for (final var value : cache) {
            builder.put(value.name(), value);
        }
        nameToTypeStore = builder.build();
    }

    public final String find;
    public final Text text;

    SpecialStringType(String text) {
        this.find = text;
        this.text = Text.of(text);
    }

    /**
     * Constructs a node referencing this enum for rendering.
     */
    public Node node() {
        return new SpecialStringNode(this);
    }

    /**
     * Replaces instances of {@link #find} with the name of the enum.
     */
    public static String preprocess(String str) {
        for (final var value : cache) {
            str = str.replace(value.find, '\b' + value.name() + '\b');
        }
        return str;
    }
}
