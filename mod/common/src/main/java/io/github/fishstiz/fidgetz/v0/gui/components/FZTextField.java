package io.github.fishstiz.fidgetz.v0.gui.components;

import com.google.common.collect.Lists;
import io.github.fishstiz.fidgetz.v0.gui.state.FZKeyed;
import io.github.fishstiz.fidgetz.v0.gui.state.FZRef;
import io.github.fishstiz.fidgetz.v0.inject.mixins.access.EditBoxAccess;
import io.github.fishstiz.fidgetz.v0.utils.CollectionUtils;
import io.github.fishstiz.fidgetz.v0.utils.FunctionUtils;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import io.github.fishstiz.fidgetz.v0.utils.Undefinable;
import io.github.fishstiz.fidgetz.v0.utils.text.TextStyleFormatter;
import io.github.fishstiz.fidgetz.v0.utils.text.TextStyleMatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class FZTextField extends EditBox implements FZComponent, FZContextMenuEntry.Source {
    private static final int DEFAULT_MAX_LENGTH = 64;
    private static final int DEFAULT_WIDTH = 150;
    private static final int DEFAULT_HEIGHT = 20;
    private final GuiComponentPropsState propsState = new GuiComponentPropsState();
    private final Formatter formatter = new Formatter();
    private final Consumer<String> responder;
    private final boolean bound;
    private Consumer<String> changeHandler = FunctionUtils.nopConsumer();
    private ScreenRectangle bounds;
    private Predicate<String> filter = _ -> true;
    private int disableResponderCount = 0;
    private String previousValue = "";
    private int previousCursorPos;
    private int previousHighlightPos;

    // when bound
    private boolean valueBound;
    private int pendingCursorPos;
    private int pendingHighlightPos;
    private String pendingValue = "";

    private FZTextField(Font font, int width, int height, Component narration, boolean bound) {
        super(font, width, height, narration);
        this.bound = bound;
        bounds = super.getRectangle();
        setMaxLength(DEFAULT_MAX_LENGTH);
        super.addFormatter(formatter);
        this.responder = this::handleChange;
        super.setResponder(responder);
    }

    private FZTextField(boolean bound) {
        this(Minecraft.getInstance().font, DEFAULT_WIDTH, DEFAULT_HEIGHT, CommonComponents.EMPTY, bound);
    }

    private boolean isBound() {
        return bound && valueBound;
    }

    private void disableResponder() {
        disableResponderCount++;
        if (disableResponderCount > 0) {
            super.setResponder(FunctionUtils.nopConsumer());
        }
    }

    private void enableResponder() {
        disableResponderCount--;
        if (disableResponderCount <= 0) {
            super.setResponder(responder);
        }
    }

    private int getHighlightPosition() {
        return ((EditBoxAccess) (Object) this).fidgetz$getHighlightPos();
    }

    private void handleChange(String value) {
        boolean valid = filter.test(value);
        disableResponder();
        if (!valid || isBound()) {
            int cursorPos = getCursorPosition();
            int highlightPos = getHighlightPosition();

            setValue(previousValue);
            setCursorPosition(previousCursorPos);
            setHighlightPos(previousHighlightPos);

            if (isBound()) {
                pendingValue = value;
                pendingCursorPos = cursorPos;
                pendingHighlightPos = highlightPos;
                changeHandler.accept(value);
            }
        } else {
            previousValue = value;
            previousHighlightPos = getHighlightPosition();
            previousCursorPos = getCursorPosition();
            changeHandler.accept(value);
        }
        enableResponder();
    }

    private void setBoundValue(String value) {
        valueBound = true;
        disableResponder();
        setValue(value);
        if (value.equals(pendingValue)) {
            setCursorPosition(pendingCursorPos);
            setHighlightPos(pendingHighlightPos);
        } else {
            moveCursorToEnd(false);
            setHighlightPos(getCursorPosition());
        }
        previousValue = value;
        previousHighlightPos = getHighlightPosition();
        previousCursorPos = getCursorPosition();
        enableResponder();
    }

    @Override
    public void setHighlightPos(int pos) {
        super.setHighlightPos(pos);
        if (disableResponderCount <= 0 && previousValue.equals(getValue())) {
            previousHighlightPos = getHighlightPosition();
        }
    }

    @Override
    public void setCursorPosition(int pos) {
        super.setCursorPosition(pos);
        if (disableResponderCount <= 0 && previousValue.equals(getValue())) {
            previousCursorPos = getCursorPosition();
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        if (propsState.overlay != null) {
            propsState.overlay.extractRenderState(graphics, getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, a);
        }
    }

    @Override
    public @Nullable String fidgetz$componentId() {
        return propsState.id;
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenuEntry.Collector collector) {
        propsState.contextEntries.accept(collector);
    }

    @Override
    @Deprecated
    public void setResponder(Consumer<String> responder) {
        throw new UnsupportedOperationException("setResponder is not supported. Use builder to set the onChange handler instead.");
    }

    @Override
    public void addFormatter(TextFormatter formatter) {
        this.formatter.formatters = CollectionUtils.addLast(this.formatter.formatters, formatter);
    }

    @Override
    public ScreenRectangle getRectangle() {
        if (ScreenRectangleUtils.unequal(bounds, this)) {
            this.bounds = super.getRectangle();
        }
        return this.bounds;
    }

    private void applyProps(Props props) {
        propsState.apply(this, props);
        props.filter().ifPresent(filter -> this.filter = filter.value());
        props.maxLength().ifPresent(this::setMaxLength);
        props.hint().ifPresent(this::setHint);
        props.suggestion().ifDefined(this::setSuggestion);
        props.text().ifPresentOrElse(this::setBoundValue, () -> valueBound = false);

        if (props.styleMatchers().isPresent() && props.formatters().isPresent()) {
            List<TextFormatter> formatters = props.formatters().get().stream().map(FZKeyed::value).collect(Collectors.toCollection(ArrayList::new));
            formatters.add(new TextStyleFormatter(props.styleMatchers().get(), this::getValue));
            formatter.formatters = formatters;
        } else if (props.styleMatchers().isPresent()) {
            formatter.formatters = List.of(new TextStyleFormatter(props.styleMatchers().get(), this::getValue));
        } else if (props.formatters().isPresent()) {
            formatter.formatters = props.formatters().get().stream().map(FZKeyed::value).toList();
        }

        props.changeHandler().ifPresent(changeHandler -> this.changeHandler = changeHandler.value());
    }

    public static FZTextField bind(String key, FZRef<Props> ref) {
        FZTextField textField = new FZTextField(true);
        textField.applyProps(ref.value());
        ref.subscribe(key, textField::applyProps);
        return textField;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static final class Formatter implements TextFormatter {
        private List<TextFormatter> formatters = Collections.emptyList();

        @Override
        public @Nullable FormattedCharSequence format(String text, int offset) {
            for (TextFormatter formatter : formatters) {
                FormattedCharSequence result = formatter.format(text, offset);
                if (result != null) return result;
            }
            return null;
        }
    }

    public interface Props extends GuiComponentProps {
        default Optional<String> text() {
            return Optional.empty();
        }

        default TriState editable() {
            return TriState.DEFAULT;
        }

        default Optional<Component> hint() {
            return Optional.empty();
        }

        default Undefinable<@Nullable String> suggestion() {
            return Undefinable.undefined();
        }

        default OptionalInt maxLength() {
            return OptionalInt.empty();
        }

        default Optional<List<FZKeyed<TextFormatter>>> formatters() {
            return Optional.empty();
        }

        default Optional<List<TextStyleMatcher>> styleMatchers() {
            return Optional.empty();
        }

        default Optional<FZKeyed<Predicate<String>>> filter() {
            return Optional.empty();
        }

        default Optional<FZKeyed<Consumer<String>>> changeHandler() {
            return Optional.empty();
        }
    }

    private static final class PropsImpl extends GuiComponentPropsBase implements Props {
        private final @Nullable String text;
        private final TriState editable;
        private final @Nullable Component hint;
        private final Undefinable<@Nullable String> suggestion;
        private final @Nullable Integer maxLength;
        private final @Nullable List<FZKeyed<TextFormatter>> formatters;
        private final @Nullable List<TextStyleMatcher> styleMatchers;
        private final @Nullable FZKeyed<Predicate<String>> filter;
        private final @Nullable FZKeyed<Consumer<String>> changeHandler;

        private PropsImpl(
                GuiComponentProps props,
                @Nullable String text,
                TriState editable,
                @Nullable Component hint,
                Undefinable<@Nullable String> suggestion,
                @Nullable Integer maxLength,
                @Nullable List<FZKeyed<TextFormatter>> formatters,
                @Nullable List<TextStyleMatcher> styleMatchers,
                @Nullable FZKeyed<Predicate<String>> filter,
                @Nullable FZKeyed<Consumer<String>> changeHandler
        ) {
            super(props);
            this.text = text;
            this.editable = editable;
            this.hint = hint;
            this.suggestion = suggestion;
            this.maxLength = maxLength;
            this.formatters = formatters;
            this.styleMatchers = styleMatchers;
            this.filter = filter;
            this.changeHandler = changeHandler;
        }

        @Override
        public Optional<String> text() {
            return Optional.ofNullable(text);
        }

        @Override
        public TriState editable() {
            return editable;
        }

        @Override
        public Optional<Component> hint() {
            return Optional.ofNullable(hint);
        }

        @Override
        public Undefinable<@Nullable String> suggestion() {
            return suggestion;
        }

        @Override
        public OptionalInt maxLength() {
            return maxLength == null ? OptionalInt.empty() : OptionalInt.of(maxLength);
        }

        @Override
        public Optional<List<FZKeyed<TextFormatter>>> formatters() {
            return Optional.ofNullable(formatters);
        }

        @Override
        public Optional<List<TextStyleMatcher>> styleMatchers() {
            return Optional.ofNullable(styleMatchers);
        }

        @Override
        public Optional<FZKeyed<Predicate<String>>> filter() {
            return Optional.ofNullable(filter);
        }

        @Override
        public Optional<FZKeyed<Consumer<String>>> changeHandler() {
            return Optional.ofNullable(changeHandler);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Props other)) return false;
            return super.equals(o) &&
                   Objects.equals(text(), other.text()) &&
                   editable == other.editable() &&
                   Objects.equals(hint(), other.hint()) &&
                   Objects.equals(suggestion(), other.suggestion()) &&
                   Objects.equals(maxLength(), other.maxLength()) &&
                   Objects.equals(formatters(), other.formatters()) &&
                   Objects.equals(styleMatchers(), other.styleMatchers()) &&
                   Objects.equals(filter(), other.filter()) &&
                   Objects.equals(changeHandler(), other.changeHandler());
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    super.hashCode(),
                    text,
                    editable,
                    hint,
                    suggestion,
                    maxLength,
                    formatters,
                    styleMatchers,
                    filter,
                    changeHandler
            );
        }
    }

    public static final class Builder extends GuiComponentPropsBuilder<Builder> {
        private @Nullable String text;
        private TriState editable = TriState.DEFAULT;
        private @Nullable Component hint;
        private Undefinable<@Nullable String> suggestion = Undefinable.undefined();
        private @Nullable Integer maxLength;
        private @Nullable List<FZKeyed<TextFormatter>> formatters;
        private @Nullable FZKeyed<Predicate<String>> filter;
        private @Nullable FZKeyed<Consumer<String>> changeHandler;
        private @Nullable List<TextStyleMatcher> styleMatchers;

        private Builder() {
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder editable(boolean editable) {
            this.editable = TriState.from(editable);
            return this;
        }

        public Builder editable() {
            return editable(true);
        }

        public Builder uneditable() {
            return editable(false);
        }

        public Builder hint(Component hint) {
            this.hint = hint;
            return this;
        }

        public Builder suggestion(@Nullable String suggestion) {
            this.suggestion = Undefinable.of(suggestion);
            return this;
        }

        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder filter(Object key, Predicate<String> filter) {
            this.filter = new FZKeyed<>(key, filter);
            return this;
        }

        public Builder filter(Predicate<String> filter) {
            this.filter = FZKeyed.selfKey(filter);
            return this;
        }

        public Builder formatter(Object key, TextFormatter formatter) {
            if (formatters == null) {
                formatters = Lists.newArrayList(new FZKeyed<>(key, formatter));
            } else {
                formatters.add(new FZKeyed<>(key, formatter));
            }
            return this;
        }

        public Builder formatter(TextFormatter formatter) {
            return formatter(formatter, formatter);
        }

        public Builder styleMatcher(TextStyleMatcher styler) {
            if (styleMatchers == null) {
                styleMatchers = Lists.newArrayList(styler);
            } else {
                styleMatchers.add(styler);
            }
            return this;
        }

        public Builder onChange(Object key, Consumer<String> changeHandler) {
            this.changeHandler = new FZKeyed<>(key, changeHandler);
            return this;
        }

        public Builder onChange(Consumer<String> changeHandler) {
            this.changeHandler = FZKeyed.selfKey(changeHandler);
            return this;
        }

        public Props toProps() {
            return new PropsImpl(
                    props,
                    text,
                    editable,
                    hint,
                    suggestion,
                    maxLength,
                    formatters,
                    styleMatchers,
                    filter,
                    changeHandler
            );
        }

        public FZTextField build() {
            FZTextField textField = new FZTextField(false);
            textField.applyProps(toProps());
            return textField;
        }
    }
}
