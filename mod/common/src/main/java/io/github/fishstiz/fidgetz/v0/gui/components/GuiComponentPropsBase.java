package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.state.FZKeyed;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.utils.Undefinable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class GuiComponentPropsBase implements GuiComponentProps {
    public @Nullable String id;
    public @Nullable Integer width;
    public @Nullable Integer height;
    public TriState active = TriState.DEFAULT;
    public TriState visible = TriState.DEFAULT;
    public TriState focusOnNavigation = TriState.DEFAULT;
    public @Nullable Component message;
    public Undefinable<@Nullable Tooltip> tooltip = Undefinable.undefined();
    public Undefinable<@Nullable RenderableRectangle> overlay = Undefinable.undefined();
    public @Nullable FZKeyed<Consumer<FZContextMenuEntry.Collector>> contextEntries;

    private GuiComponentPropsBase() {
    }

    public GuiComponentPropsBase(GuiComponentProps props) {
        props.id().ifPresent(id -> this.id = id);
        props.width().ifPresent(w -> this.width = w);
        props.height().ifPresent(h -> this.height = h);
        ifNonDefault(props.active(), a -> this.active = a);
        ifNonDefault(props.visible(), a -> this.visible = a);
        ifNonDefault(props.focusOnNavigation(), a -> this.focusOnNavigation = a);
        props.message().ifPresent(m -> this.message = m);
        this.tooltip = props.tooltip();
        this.overlay = props.overlay();
        props.contextEntries().ifPresent(contextEntries -> this.contextEntries = contextEntries);
    }

    public static GuiComponentPropsBase defaults() {
        return new GuiComponentPropsBase();
    }

    protected static void ifNonDefault(TriState state, Consumer<TriState> consumer) {
        if (state != TriState.DEFAULT) {
            consumer.accept(state);
        }
    }

    protected static OptionalInt wrapBoxedInt(@Nullable Integer value) {
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }

    @Override
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    @Override
    public OptionalInt width() {
        return wrapBoxedInt(width);
    }

    @Override
    public OptionalInt height() {
        return wrapBoxedInt(height);
    }

    @Override
    public TriState active() {
        return active;
    }

    @Override
    public TriState visible() {
        return visible;
    }

    @Override
    public TriState focusOnNavigation() {
        return focusOnNavigation;
    }

    @Override
    public Optional<Component> message() {
        return Optional.ofNullable(message);
    }

    @Override
    public Undefinable<@Nullable Tooltip> tooltip() {
        return tooltip;
    }

    @Override
    public Undefinable<@Nullable RenderableRectangle> overlay() {
        return overlay;
    }

    @Override
    public Optional<FZKeyed<Consumer<FZContextMenuEntry.Collector>>> contextEntries() {
        return Optional.ofNullable(contextEntries);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuiComponentProps other)) return false;
        return Objects.equals(width(), other.width())
               && Objects.equals(height(), other.height())
               && active() == other.active()
               && visible() == other.visible()
               && focusOnNavigation() == other.focusOnNavigation()
               && Objects.equals(message(), other.message())
               && Objects.equals(tooltip(), other.tooltip())
               && Objects.equals(overlay(), other.overlay())
               && Objects.equals(contextEntries(), other.contextEntries());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                width,
                height,
                active,
                visible,
                focusOnNavigation,
                message,
                tooltip,
                overlay,
                contextEntries
        );
    }
}
