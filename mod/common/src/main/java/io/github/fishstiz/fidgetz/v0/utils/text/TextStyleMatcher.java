package io.github.fishstiz.fidgetz.v0.utils.text;

import net.minecraft.network.chat.Style;

public interface TextStyleMatcher {
    boolean stylable(String input);

    void reset(String input);

    boolean find();

    int start();

    int end();

    Style style();

    TextStyleMatcher copy();
}
