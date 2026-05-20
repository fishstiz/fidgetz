package io.github.fishstiz.fidgetz.v0.utils.text;

import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextStylePatternMatcher implements TextStyleMatcher {
    private final @Nullable Predicate<String> stylable;
    private final Matcher matcher;
    private final Style style;

    public TextStylePatternMatcher(Pattern pattern, Style style, @Nullable Predicate<String> stylable) {
        this.stylable = stylable;
        this.matcher = pattern.matcher("");
        this.style = style;
    }

    public TextStylePatternMatcher(Pattern pattern, Style style) {
        this(pattern, style, null);
    }

    @Override
    public boolean stylable(String input) {
        return stylable == null || stylable.test(input);
    }

    @Override
    public void reset(String input) {
        matcher.reset(input);
    }

    @Override
    public boolean find() {
        return matcher.find();
    }

    @Override
    public int start() {
        return matcher.start();
    }

    @Override
    public int end() {
        return matcher.end();
    }

    @Override
    public Style style() {
        return matcher.hasMatch() ? style : Style.EMPTY;
    }

    @Override
    public TextStyleMatcher copy() {
        return new TextStylePatternMatcher(matcher.pattern(), style, stylable);
    }
}
