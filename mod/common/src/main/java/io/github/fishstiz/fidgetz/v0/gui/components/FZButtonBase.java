package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.state.FZKeyed;
import io.github.fishstiz.fidgetz.v0.utils.FunctionUtils;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class FZButtonBase extends Button.Plain implements FZComponent, FZContextMenu.Source {
    protected static final WidgetRenderables DEFAULT_RENDERABLES = WidgetRenderables.sprites(new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"),
            Identifier.withDefaultNamespace("widget/button_disabled"),
            Identifier.withDefaultNamespace("widget/button_highlighted")
    ));
    protected static final OnPress NOP = _ -> {
    };
    private final GuiComponentPropsState propsState = new GuiComponentPropsState();
    private Consumer<PressEvent> pressHandler = FunctionUtils.nopConsumer();
    private boolean allowCursorChanges = true;
    private ScreenRectangle bounds;

    protected FZButtonBase(int width, int height, Component message) {
        super(0, 0, width, height, message, NOP, DEFAULT_NARRATION);
        bounds = new ScreenRectangle(0, 0, width, height);
    }

    protected FZButtonBase(int width, int height) {
        this(width, height, CommonComponents.EMPTY);
    }

    protected FZButtonBase() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, CommonComponents.EMPTY);
    }

    @Override
    protected void handleCursor(GuiGraphicsExtractor graphics) {
        if (allowCursorChanges) {
            super.handleCursor(graphics);
        }
    }

    @Override
    public void onPress(InputWithModifiers input) {
        super.onPress(input);
        pressHandler.accept(new PressEvent(this, input));
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenu.Collector collector) {
        propsState.contextEntries.accept(collector);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractContents(graphics, mouseX, mouseY, a);
        extractOverlay(graphics, mouseX, mouseY, a);
    }

    protected void extractOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (propsState.overlay != null) {
            propsState.overlay.extractRenderState(graphics, getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, a);
        }
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return propsState.focusOnInteraction;
    }

    @Override
    public ScreenRectangle getRectangle() {
        if (ScreenRectangleUtils.unequal(bounds, this)) {
            bounds = super.getRectangle();
        }
        return bounds;
    }

    protected void applyProps(Props props) {
        propsState.apply(this, props);
        props.pressHandler().ifPresent(handler -> this.pressHandler = handler.value());

        if (props.allowCursorChanges() != TriState.DEFAULT) {
            this.allowCursorChanges = props.allowCursorChanges().toBoolean(true);
        }
    }

    @Override
    public @Nullable String fidgetz$componentId() {
        return propsState.id;
    }

    public record PressEvent(FZButtonBase target, InputWithModifiers input) {
    }

    public interface Props extends GuiComponentProps {
        default Optional<FZKeyed<Consumer<PressEvent>>> pressHandler() {
            return Optional.empty();
        }

        default TriState allowCursorChanges() {
            return TriState.DEFAULT;
        }
    }

    abstract static class PropsImpl extends GuiComponentPropsBase implements Props {
        private final @Nullable FZKeyed<Consumer<PressEvent>> pressHandler;
        private final TriState allowCursorChanges;

        PropsImpl(@Nullable FZKeyed<Consumer<PressEvent>> pressHandler, TriState allowCursorChanges, GuiComponentProps props) {
            super(props);
            this.pressHandler = pressHandler;
            this.allowCursorChanges = allowCursorChanges;
        }

        @Override
        public Optional<FZKeyed<Consumer<PressEvent>>> pressHandler() {
            return Optional.ofNullable(pressHandler);
        }

        @Override
        public TriState allowCursorChanges() {
            return allowCursorChanges;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Props other)) return false;
            return super.equals(o) &&
                   Objects.equals(pressHandler(), other.pressHandler()) &&
                   allowCursorChanges == other.allowCursorChanges();
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), pressHandler, allowCursorChanges);
        }
    }

    public static abstract class AbstractBuilder<T, B extends FZButtonBase, P extends Props> extends GuiComponentPropsBuilder<T> {
        protected @Nullable FZKeyed<Consumer<PressEvent>> pressHandler;
        protected TriState allowCursorChanges = TriState.DEFAULT;

        protected AbstractBuilder() {
        }

        public T bigWidth() {
            return width(BIG_WIDTH);
        }

        public T smallWidth() {
            return width(SMALL_WIDTH);
        }

        public T square() {
            int size = DEFAULT_HEIGHT;
            return size(size, size);
        }

        public T onPress(Consumer<PressEvent> pressHandler) {
            this.pressHandler = FZKeyed.selfKey(Objects.requireNonNull(pressHandler, "pressHandler cannot be null"));
            return self();
        }

        public T onPress(Runnable clickAction) {
            Objects.requireNonNull(clickAction, "clickAction cannot be null");
            this.pressHandler = FZKeyed.selfKey(_ -> clickAction.run());
            return self();
        }

        public T onPress(Object key, Consumer<PressEvent> pressHandler) {
            this.pressHandler = new FZKeyed<>(key, Objects.requireNonNull(pressHandler, "pressHandler cannot be null"));
            return self();
        }

        public T onPress(Object key, Runnable clickAction) {
            Objects.requireNonNull(clickAction, "clickAction cannot be null");
            this.pressHandler = new FZKeyed<>(key, _ -> clickAction.run());
            return self();
        }

        public T allowCursorChanges(boolean allowCursorChanges) {
            this.allowCursorChanges = TriState.from(allowCursorChanges);
            return self();
        }

        public T disableCursorChanges() {
            this.allowCursorChanges = TriState.FALSE;
            return self();
        }

        public abstract P toProps();

        public abstract B build();
    }
}
