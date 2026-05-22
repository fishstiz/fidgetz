package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.state.FZKeyed;
import io.github.fishstiz.fidgetz.v0.gui.state.FZRef;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.utils.FunctionUtils;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import io.github.fishstiz.fidgetz.v0.utils.Undefinable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class FZModal extends FZDialog implements FZComponent, FZContextMenuEntry.Source {
    protected static final ScreenRectangle DEFAULT_MARGIN = ScreenRectangle.empty();
    protected static final ScreenRectangle DEFAULT_PADDING = ScreenRectangleUtils.insets(8);
    protected static final RenderableRectangle DEFAULT_BACKDROP = Renderables.fill(ARGB.color(0.5f, CommonColors.BLACK));
    protected static final RenderableRectangle DEFAULT_BACKGROUND = Renderables.sprite(Identifier.withDefaultNamespace("popup/background"));
    private @Nullable String id;
    private Component message = CommonComponents.EMPTY;
    private FZKeyed<Consumer<FZContextMenuEntry.Collector>> contextEntries = FZKeyed.selfKey(FunctionUtils.nopConsumer());
    private Runnable closeHandler = FunctionUtils.nop();
    protected Layout layout;
    protected ScreenRectangle margin = DEFAULT_MARGIN;
    protected @Nullable RenderableRectangle backdrop = DEFAULT_BACKDROP;
    protected @Nullable RenderableRectangle background = DEFAULT_BACKGROUND;
    protected boolean captureClick = true;
    protected boolean closeAfterClickOutOfBounds = true;
    protected boolean captureFocus = true;
    private boolean bound = false;
    private ScreenRectangle bounds;

    protected FZModal(FZDialogContainer container, Layout layout) {
        super(container);
        this.layout = layout;
        this.bounds = layout.getRectangle();
    }

    public void open() {
        if (!isBound()) {
            setOpen(true);
        }
    }

    public void close() {
        setOpen(false);
    }

    @Override
    protected void setOpen(boolean open) {
        if (!isBound()) {
            super.setOpen(open);
        } else if (!open) {
            // modal requests to close from auto-closing methods,
            // close handler should manually update open state
            closeHandler.run();
        }
    }

    private void forceSetOpen(boolean open) {
        super.setOpen(open);
    }

    @Override
    protected void onClose() {
        super.onClose();
        clearWidgets();
        if (captureFocus) {
            refocusLastContainerPath();
        }
    }

    @Override
    protected void onOpen() {
        clearWidgets();
        repositionElements();
        layout.visitWidgets(this::addRenderableWidget);
        super.onOpen();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return bounds;
    }

    @Override
    public boolean shouldCaptureClick() {
        return captureClick;
    }

    @Override
    public boolean shouldCloseAfterClickOutOfBounds() {
        return closeAfterClickOutOfBounds;
    }

    @Override
    public boolean shouldCaptureFocus() {
        return captureFocus;
    }

    @Override
    protected void extractDialogRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (backdrop != null) {
            ScreenRectangle bounds = container.getRectangle();
            backdrop.extractRenderState(graphics, bounds.left(), bounds.top(), bounds.width(), bounds.height(), mouseX, mouseY, partialTick);
        }
        if (background != null) {
            ScreenRectangle bounds = getRectangle();
            background.extractRenderState(graphics, bounds.left(), bounds.top(), bounds.width(), bounds.height(), mouseX, mouseY, partialTick);
        }
        super.extractDialogRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, message);
        super.updateNarration(output);
    }

    @Override
    public void repositionElements() {
        FZLayouts.composer(container, layout)
                .padded(margin.left(), margin.top(), margin.right(), margin.bottom())
                .clamped()
                .arrange();
        bounds = layout.getRectangle();
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenuEntry.Collector collector) {
        contextEntries.value().accept(collector);
        FZContextMenuEntry.Source.super.fidgetz$updateContextEntries(x, y, collector);
    }

    @Override
    public @Nullable String fidgetz$componentId() {
        return id;
    }

    protected boolean isBound() {
        return bound;
    }

    private static void ifNonDefault(TriState triState, Consumer<TriState> consumer) {
        if (triState != TriState.DEFAULT) {
            consumer.accept(triState);
        }
    }

    protected void applyProps(Props props) {
        FZDialogContainer newContainer = props.container();
        if (newContainer != container) {
            throw new UnsupportedOperationException("Updating the container of FZModal is not supported.");
        }

        props.id().ifPresent(id -> this.id = id);
        props.popoverOrder().ifPresent(order -> this.popoverOrder = order);
        props.message().ifPresent(message -> this.message = message);
        props.contextEntries().ifPresent(contextEntries -> this.contextEntries = contextEntries);

        props.backdrop().ifDefined(backdrop -> this.backdrop = backdrop);
        props.background().ifDefined(background -> this.background = background);
        props.closeHandler().ifPresent(handler -> this.closeHandler = handler.value());

        ifNonDefault(props.open(), open -> {
            if (isBound()) {
                forceSetOpen(open.toBoolean(isOpen()));
            } else {
                setOpen(open.toBoolean(false));
            }
        });

        ifNonDefault(props.captureClick(), captureClick -> this.captureClick = captureClick.toBoolean(true));
        ifNonDefault(props.closeAfterClickOutOfBounds(), close -> this.closeAfterClickOutOfBounds = close.toBoolean(true));
        ifNonDefault(props.captureFocus(), captureFocus -> this.captureFocus = captureFocus.toBoolean(true));

        props.margin().ifPresent(margin -> this.margin = margin);

        Layout baseLayout = props.layout();
        FZLayouts.Composer<?> composer = FZLayouts.composer(container, baseLayout);
        ScreenRectangle padding = props.padding().orElse(DEFAULT_PADDING);
        if (!ScreenRectangleUtils.isInsetsEmpty(padding)) {
            composer = composer.padded(padding.left(), padding.top(), padding.right(), padding.bottom());
        }
        if (props.centered().toBoolean(true)) {
            composer = composer.centered();
        }
        this.layout = composer.clamped().get();
        repositionElements();
    }

    public static FZModal bind(String key, FZRef<Props> ref) {
        Props props = ref.value();
        FZModal modal = new FZModal(props.container(), props.layout());
        modal.bound = true;
        modal.applyProps(ref.value());
        ref.subscribe(key, modal::applyProps);
        return modal;
    }

    public static Builder builder(FZDialogContainer container, Layout layout) {
        return new Builder(container, layout);
    }

    public interface Props {
        FZDialogContainer container();

        Layout layout();

        default Optional<String> id() {
            return Optional.empty();
        }

        default OptionalInt width() {
            return OptionalInt.empty();
        }

        default OptionalInt height() {
            return OptionalInt.empty();
        }

        default Optional<Component> message() {
            return Optional.empty();
        }

        default Optional<FZKeyed<Consumer<FZContextMenuEntry.Collector>>> contextEntries() {
            return Optional.empty();
        }

        default TriState open() {
            return TriState.DEFAULT;
        }

        default Optional<FZKeyed<Runnable>> closeHandler() {
            return Optional.empty();
        }

        default Optional<ScreenRectangle> margin() {
            return Optional.empty();
        }

        default Optional<ScreenRectangle> padding() {
            return Optional.empty();
        }

        default Undefinable<@Nullable RenderableRectangle> backdrop() {
            return Undefinable.undefined();
        }

        default Undefinable<@Nullable RenderableRectangle> background() {
            return Undefinable.undefined();
        }

        default TriState captureClick() {
            return TriState.DEFAULT;
        }

        default TriState closeAfterClickOutOfBounds() {
            return TriState.DEFAULT;
        }

        default TriState captureFocus() {
            return TriState.DEFAULT;
        }

        default TriState centered() {
            return TriState.DEFAULT;
        }

        default OptionalInt popoverOrder() {
            return OptionalInt.empty();
        }
    }

    private static final class PropsImpl implements Props {
        private final FZDialogContainer container;
        private final @Nullable String id;
        private final @Nullable Integer width;
        private final @Nullable Integer height;
        private final @Nullable Component message;
        private final @Nullable FZKeyed<Consumer<FZContextMenuEntry.Collector>> contextEntries;
        private final Layout layout;
        private final TriState open;
        private final @Nullable FZKeyed<Runnable> closeHandler;
        private final @Nullable ScreenRectangle margin;
        private final @Nullable ScreenRectangle padding;
        private final Undefinable<@Nullable RenderableRectangle> backdrop;
        private final Undefinable<@Nullable RenderableRectangle> background;
        private final TriState captureClick;
        private final TriState closeAfterClickOutOfBounds;
        private final TriState captureFocus;
        private final TriState centered;
        private final @Nullable Integer popoverOrder;

        private PropsImpl(
                FZDialogContainer container,
                @Nullable String id,
                @Nullable Integer width,
                @Nullable Integer height,
                @Nullable Component message,
                @Nullable FZKeyed<Consumer<FZContextMenuEntry.Collector>> contextEntries,
                Layout layout,
                TriState open,
                @Nullable FZKeyed<Runnable> closeHandler,
                @Nullable ScreenRectangle margin,
                @Nullable ScreenRectangle padding,
                Undefinable<@Nullable RenderableRectangle> backdrop,
                Undefinable<@Nullable RenderableRectangle> background,
                TriState captureClick,
                TriState closeAfterClickOutOfBounds,
                TriState captureFocus,
                TriState centered, @Nullable Integer popoverOrder
        ) {
            this.container = container;
            this.id = id;
            this.width = width;
            this.height = height;
            this.message = message;
            this.contextEntries = contextEntries;
            this.layout = layout;
            this.open = open;
            this.closeHandler = closeHandler;
            this.margin = margin;
            this.padding = padding;
            this.backdrop = backdrop;
            this.background = background;
            this.captureClick = captureClick;
            this.closeAfterClickOutOfBounds = closeAfterClickOutOfBounds;
            this.captureFocus = captureFocus;
            this.centered = centered;
            this.popoverOrder = popoverOrder;
        }

        @Override
        public FZDialogContainer container() {
            return container;
        }

        @Override
        public Optional<String> id() {
            return Optional.ofNullable(id);
        }

        @Override
        public OptionalInt width() {
            return width == null ? OptionalInt.empty() : OptionalInt.of(width);
        }

        @Override
        public OptionalInt height() {
            return height == null ? OptionalInt.empty() : OptionalInt.of(height);
        }

        @Override
        public Optional<Component> message() {
            return Optional.ofNullable(message);
        }

        @Override
        public Optional<FZKeyed<Consumer<FZContextMenuEntry.Collector>>> contextEntries() {
            return Optional.ofNullable(contextEntries);
        }

        @Override
        public Layout layout() {
            return layout;
        }

        @Override
        public TriState open() {
            return open;
        }

        @Override
        public Optional<FZKeyed<Runnable>> closeHandler() {
            return Optional.ofNullable(closeHandler);
        }

        @Override
        public Optional<ScreenRectangle> margin() {
            return Optional.ofNullable(margin);
        }

        @Override
        public Optional<ScreenRectangle> padding() {
            return Optional.ofNullable(padding);
        }

        @Override
        public Undefinable<@Nullable RenderableRectangle> backdrop() {
            return backdrop;
        }

        @Override
        public Undefinable<@Nullable RenderableRectangle> background() {
            return background;
        }

        @Override
        public TriState captureClick() {
            return captureClick;
        }

        @Override
        public TriState closeAfterClickOutOfBounds() {
            return closeAfterClickOutOfBounds;
        }

        @Override
        public TriState captureFocus() {
            return captureFocus;
        }

        @Override
        public TriState centered() {
            return centered;
        }

        @Override
        public OptionalInt popoverOrder() {
            return popoverOrder == null ? OptionalInt.empty() : OptionalInt.of(popoverOrder);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Props other)) return false;
            return Objects.equals(container, other.container()) &&
                   Objects.equals(id(), other.id()) &&
                   Objects.equals(width(), other.width()) &&
                   Objects.equals(height(), other.height()) &&
                   Objects.equals(message(), other.message()) &&
                   Objects.equals(contextEntries(), other.contextEntries()) &&
                   Objects.equals(layout, other.layout()) &&
                   open == other.open() &&
                   Objects.equals(closeHandler(), other.closeHandler()) &&
                   Objects.equals(margin(), other.margin()) &&
                   Objects.equals(padding(), other.padding()) &&
                   Objects.equals(backdrop(), other.backdrop()) &&
                   Objects.equals(background(), other.background()) &&
                   captureClick == other.captureClick() &&
                   closeAfterClickOutOfBounds == other.closeAfterClickOutOfBounds() &&
                   captureFocus == other.captureFocus() &&
                   centered == other.centered() &&
                   Objects.equals(popoverOrder(), other.popoverOrder());
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    container,
                    id,
                    width,
                    height,
                    message,
                    contextEntries,
                    layout,
                    open,
                    closeHandler,
                    margin,
                    padding,
                    backdrop,
                    background,
                    captureClick,
                    closeAfterClickOutOfBounds,
                    captureFocus,
                    centered,
                    popoverOrder
            );
        }
    }

    public static final class Builder {
        private final FZDialogContainer container;
        private final Layout layout;
        private @Nullable String id;
        private @Nullable Integer width;
        private @Nullable Integer height;
        private @Nullable Component message;
        private @Nullable FZKeyed<Consumer<FZContextMenuEntry.Collector>> contextEntries;
        private TriState open = TriState.DEFAULT;
        private @Nullable FZKeyed<Runnable> closeHandler;
        private @Nullable ScreenRectangle margin;
        private @Nullable ScreenRectangle padding;
        private Undefinable<@Nullable RenderableRectangle> background = Undefinable.undefined();
        private Undefinable<@Nullable RenderableRectangle> backdrop = Undefinable.undefined();
        private TriState captureClick = TriState.DEFAULT;
        private TriState closeAfterClickOutOfBounds = TriState.DEFAULT;
        private TriState captureFocus = TriState.DEFAULT;
        private TriState centered = TriState.DEFAULT;
        private @Nullable Integer popoverOrder;

        Builder(FZDialogContainer container, Layout layout) {
            this.container = container;
            this.layout = layout;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder size(int width, int height) {
            return width(width).height(height);
        }

        public Builder message(@Nullable Component message) {
            this.message = message;
            return this;
        }

        public Builder contextEntries(FZContextMenuEntry entry, FZContextMenuEntry... rest) {
            Objects.requireNonNull(entry, "entry cannot be null");
            this.contextEntries = FZKeyed.selfKey(collector -> {
                collector.addEntry(entry);
                for (FZContextMenuEntry e : rest) {
                    collector.addEntry(Objects.requireNonNull(e, "entry cannot be null"));
                }
            });
            return this;
        }

        public Builder contextEntries(Consumer<FZContextMenuEntry.Collector> contextSupplier) {
            this.contextEntries = FZKeyed.selfKey(Objects.requireNonNull(contextSupplier, "contextSupplier cannot be null"));
            return this;
        }

        public Builder open(boolean open) {
            this.open = TriState.from(open);
            return this;
        }

        public Builder open() {
            return open(true);
        }

        public Builder closeHandler(FZKeyed<Runnable> closeHandler) {
            this.closeHandler = closeHandler;
            return this;
        }

        public Builder closeHandler(Runnable closeHandler) {
            this.closeHandler = FZKeyed.selfKey(closeHandler);
            return this;
        }

        public Builder margin(int margin) {
            this.margin = ScreenRectangleUtils.insets(margin);
            return this;
        }

        public Builder margin(int left, int top, int right, int bottom) {
            this.margin = ScreenRectangleUtils.insets(left, top, right, bottom);
            return this;
        }

        public Builder padding(int padding) {
            this.padding = ScreenRectangleUtils.insets(padding);
            return this;
        }

        public Builder padding(int left, int top, int right, int bottom) {
            this.padding = ScreenRectangleUtils.insets(left, top, right, bottom);
            return this;
        }

        public Builder backdrop(@Nullable RenderableRectangle backdrop) {
            this.backdrop = Undefinable.of(backdrop);
            return this;
        }

        public Builder background(@Nullable RenderableRectangle background) {
            this.background = Undefinable.of(background);
            return this;
        }

        public Builder captureClick(boolean captureClick) {
            this.captureClick = TriState.from(captureClick);
            return this;
        }

        public Builder closeAfterClickOutOfBounds(boolean closeAfterClickOutOfBounds) {
            this.closeAfterClickOutOfBounds = TriState.from(closeAfterClickOutOfBounds);
            return this;
        }

        public Builder captureFocus(boolean captureFocus) {
            this.captureFocus = TriState.from(captureFocus);
            return this;
        }

        public Builder centered(boolean centered) {
            this.centered = TriState.from(centered);
            return this;
        }

        public Builder centered() {
            return centered(true);
        }

        public Builder uncentered() {
            return centered(false);
        }

        public Builder popoverOrder(int order) {
            this.popoverOrder = order;
            return this;
        }

        public Props toProps() {
            return new PropsImpl(
                    container,
                    id,
                    width,
                    height,
                    message,
                    contextEntries,
                    layout,
                    open,
                    closeHandler,
                    margin,
                    padding,
                    backdrop,
                    background,
                    captureClick,
                    closeAfterClickOutOfBounds,
                    captureFocus,
                    centered,
                    popoverOrder
            );
        }

        public FZModal build() {
            FZModal modal = new FZModal(container, layout);
            modal.applyProps(toProps());
            return modal;
        }

        public FZModal buildAndOpen() {
            FZModal modal = build();
            modal.setOpen(true);
            return modal;
        }
    }
}
