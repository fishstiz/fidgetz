package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.state.FZKeyed;
import io.github.fishstiz.fidgetz.v0.gui.state.FZRef;
import io.github.fishstiz.fidgetz.v0.gui.components.events.ScrollableContainer;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.utils.MathUtils;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FZList extends AbstractContainerWidget implements Layout, ScrollableContainer, FZComponent, FZContextMenuEntry.Source {
    private static final int DEFAULT_MAX_CONTENT_WIDTH = 270;
    private static final int SEPARATOR_HEIGHT = 2;
    private static final RenderableRectangle BACKGROUND = Renderables.texture(Identifier.withDefaultNamespace("textures/gui/menu_list_background.png"), 32, 32);
    private static final RenderableRectangle INWORLD_BACKGROUND = Renderables.texture(Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png"), 32, 32);
    private static final RenderableRectangle HEADER_SEPARATOR = Renderables.texture(Screen.HEADER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private static final RenderableRectangle INWORLD_HEADER_SEPARATOR = Renderables.texture(Screen.INWORLD_HEADER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private static final RenderableRectangle FOOTER_SEPARATOR = Renderables.texture(Screen.FOOTER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private static final RenderableRectangle INWORLD_FOOTER_SEPARATOR = Renderables.texture(Screen.INWORLD_FOOTER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    protected static final int DEFAULT_WIDTH = 300;
    protected static final int DEFAULT_HEIGHT = 150;
    protected static final int DEFAULT_SCROLL_RATE = 10;
    private final Minecraft minecraft;
    private final List<Entry> entries = new ArrayList<>();
    private final List<GuiEventListener> children = new ArrayList<>();
    private final List<NarratableEntry> narratables = new ArrayList<>();
    private final List<Renderable> renderables = new ArrayList<>();
    private final GuiComponentPropsState propsState = new GuiComponentPropsState();
    private FZFlexLayout layout;
    private FZKeyed<BiConsumer<FZList, FZFlexLayout>> entryInitializer = FZKeyed.selfKey((_, _) -> {
    });
    private int maxContentWidth = DEFAULT_MAX_CONTENT_WIDTH;
    private int contentPaddingLeft;
    private int contentPaddingRight;
    private boolean reserveScrollbarWidth;
    private int scrollRate;
    private ScreenRectangle bounds;

    protected FZList(Minecraft minecraft, int width, int height, Component message, ScrollbarSettings scrollbarSettings) {
        super(0, 0, width, height, message, scrollbarSettings);
        this.minecraft = minecraft;
        scrollRate = scrollbarSettings.scrollRate();
        layout = FZLayouts.flexVertical();
        bounds = super.getRectangle();
    }

    protected FZList(int width, int height, Component message, ScrollbarSettings scrollbarSettings) {
        this(Minecraft.getInstance(), width, height, message, scrollbarSettings);
    }

    protected FZList() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, CommonComponents.EMPTY, AbstractScrollArea.defaultSettings(DEFAULT_SCROLL_RATE));
    }

    public final void initializeEntries() {
        GuiEventListener lastFocused = getFocused();
        String lastFocusedId = null;

        for (Iterator<Entry> it = entries.listIterator(); it.hasNext(); ) {
            Entry entry = it.next();
            it.remove();
            if (lastFocused != null && entry.widget == lastFocused) {
                lastFocusedId = entry.id;
            }
        }

        children.clear();
        narratables.clear();
        renderables.clear();

        GuiComponentCollector collector = new GuiComponentCollector();
        FZFlexLayout newLayout = FZLayouts.flexVertical().maxWidth(contentWidth()).maxHeight(0);
        onInitializeEntries(newLayout);
        newLayout.visitWidgets(collector::renderableWidget);

        MutableBoolean focusUnresolved = new MutableBoolean(true);
        collector.flushTo(getWidgetSink(lastFocused, lastFocusedId, focusUnresolved), renderables::add);
        if (focusUnresolved.booleanValue()) setFocused(null);

        this.layout = newLayout;
        arrangeElements();
    }

    private WidgetVisitor getWidgetSink(@Nullable GuiEventListener lastFocused, @Nullable String lastFocusedId, MutableBoolean focusUnresolved) {
        int hashCode = hashCode();
        return new WidgetVisitor() {
            private int count = 0;

            @Override
            public <T extends GuiEventListener & NarratableEntry> void fidgetz$visitWidget(T widget) {
                String id = widget instanceof FZComponent component ? component.fidgetz$componentId() : null;
                if (id == null) id = "FZList@%s-element-%s-%s".formatted(hashCode, count, widget.getClass().getName());

                entries.add(new Entry(id, widget));
                children.add(widget);
                narratables.add(widget);
                count++;

                if (widget == lastFocused || (Objects.equals(id, lastFocusedId) && focusUnresolved.booleanValue())) {
                    setFocused(widget);
                    focusUnresolved.setValue(false);
                }
            }
        };
    }

    protected void onInitializeEntries(FZFlexLayout layout) {
        entryInitializer.value().accept(this, layout);
    }

    protected int maxContentWidth() {
        return maxContentWidth;
    }

    protected int contentPaddingLeft() {
        return contentPaddingLeft;
    }

    protected int contentPaddingRight() {
        return contentPaddingRight;
    }

    protected boolean reserveScrollbarWidth() {
        return reserveScrollbarWidth;
    }

    @Override
    protected int contentHeight() {
        return layout.getHeight();
    }

    protected int scrollbarReserve() {
        return reserveScrollbarWidth() || scrollbarVisible() ? scrollbarWidth() : 0;
    }

    protected int contentWidth() {
        int contentWidth = getWidth() - scrollbarReserve() - contentPaddingLeft() - contentPaddingRight();
        return MathUtils.optionalMin(contentWidth, maxContentWidth());
    }

    protected boolean scrollbarVisible() {
        return contentHeight() > getHeight();
    }

    @Override
    protected int scrollBarX() {
        return Math.min(layout.getX() + layout.getWidth() + contentPaddingRight(), getRight() - scrollbarWidth());
    }

    @Override
    public double scrollRate() {
        return scrollRate;
    }

    @Override
    public void arrangeElements() {
        layout.arrangeElements();
        layout.fidgetz$setWidth(contentWidth());
        setPosition(getX(), getY());
        refreshScrollAmount();
    }

    protected void extractBackgroundRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        RenderableRectangle background = minecraft.level == null ? BACKGROUND : INWORLD_BACKGROUND;
        background.extractRenderState(graphics, getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, partialTick);
    }

    protected void extractEntryRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        for (Renderable renderable : renderables) {
            renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    protected void extractSeparatorsRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        RenderableRectangle header = minecraft.level == null ? HEADER_SEPARATOR : INWORLD_HEADER_SEPARATOR;
        RenderableRectangle footer = minecraft.level == null ? FOOTER_SEPARATOR : INWORLD_FOOTER_SEPARATOR;
        header.extractRenderState(graphics, getX(), getY() - SEPARATOR_HEIGHT, getWidth(), SEPARATOR_HEIGHT, mouseX, mouseY, partialTick);
        footer.extractRenderState(graphics, getX(), getBottom(), getWidth(), SEPARATOR_HEIGHT, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractBackgroundRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.enableScissor(getX(), getY(), getRight(), getBottom());
        extractEntryRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.disableScissor();
        extractSeparatorsRenderState(graphics, mouseX, mouseY, partialTick);
        extractScrollbar(graphics, mouseX, mouseY);
        if (propsState.overlay != null) {
            propsState.overlay.extractRenderState(graphics, getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected boolean isOverScrollbar(double x, double y) {
        return super.isOverScrollbar(x, y) && isHovered();
    }

    @Override
    public List<NarratableEntry> getNarratables() {
        return narratables;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        layout.setX(Math.min(
                x + (getWidth() / 2 - contentWidth() / 2) + contentPaddingLeft() - contentPaddingRight(),
                getRight() - scrollbarReserve() - contentWidth()
        ));
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        layout.setY(y - (int) scrollAmount());
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        arrangeElements();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        arrangeElements();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        arrangeElements();
    }

    @Override
    public void setFocused(boolean focused) {
        if (!focused) {
            setFocused(null);
        }
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (getFocused() != focused) {
            super.setFocused(focused);
        }
    }

    @Override
    public ScreenRectangle getBorderForArrowNavigation(ScreenDirection opposite) {
        GuiEventListener focused = getFocused();
        return focused != null
                ? focused.getBorderForArrowNavigation(opposite)
                : new ScreenRectangle(getX(), getY(), width, contentHeight()).getBorder(opposite);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        return addScrollEffectOnFocus(navigationEvent, super.nextFocusPath(navigationEvent));
    }

    @Override
    public void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        layout.setY(getY() - (int) scrollAmount());
    }

    @Override
    public ScreenRectangle getRectangle() {
        if (ScreenRectangleUtils.unequal(bounds, this)) {
            this.bounds = super.getRectangle();
        }
        return this.bounds;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> layoutElementVisitor) {
        layoutElementVisitor.accept(this);
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenuEntry.Collector collector) {
        propsState.contextEntries.accept(collector);
        FZContextMenuEntry.Source.super.fidgetz$updateContextEntries(x, y, collector);
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return propsState.focusOnNavigation;
    }

    @Override
    public @Nullable String fidgetz$componentId() {
        return propsState.id;
    }

    private @Nullable Entry getEntryAt(double x, double y) {
        for (Entry entry : entries) {
            if (entry.widget.isMouseOver(x, y)) {
                return entry;
            }
        }
        return null;
    }

    private @Nullable Entry resolveEntry(Entry unknownEntry) {
        for (Entry entry : entries) {
            if (unknownEntry == entry ||
                unknownEntry.widget == entry.widget ||
                unknownEntry.id.equals(entry.id)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean scrolled = updateScrolling(event);

        Entry entry = getEntryAt(event.x(), event.y());
        if (entry != null && entry.widget.mouseClicked(event, doubleClick) && entry.widget.shouldTakeFocusAfterInteraction()) {
            // buttons are only focused after end of mouseClicked,
            // so when a button inside the list reinitializes entries,
            // the new button instance with the same id as the clicked button cannot be refocused
            Entry clickedEntry = resolveEntry(entry);
            if (clickedEntry != null && clickedEntry.widget.shouldTakeFocusAfterInteraction()) {
                setFocused(clickedEntry.widget);
                if (isValidClickButton(event.buttonInfo())) {
                    setDragging(true);
                }
                return true;
            }
        }

        return scrolled;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (isActive() && getChildAt(mx, my).filter(child -> child.mouseScrolled(mx, my, scrollX, scrollY)).isPresent()) {
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY) && scrollable();
    }

    protected void applyProps(Props props) {
        this.propsState.apply(this, props);

        props.maxContentWidth().ifPresent(maxWidth -> this.maxContentWidth = maxWidth);
        props.contentPaddingLeft().ifPresent(padding -> this.contentPaddingLeft = padding);
        props.contentPaddingRight().ifPresent(padding -> this.contentPaddingRight = padding);

        if (props.reserveScrollbarWidth() != TriState.DEFAULT) {
            this.reserveScrollbarWidth = props.reserveScrollbarWidth().toBoolean(false);
        }

        props.scrollRate().ifPresent(scrollRate -> this.scrollRate = scrollRate);

        props.entryInitializer().ifPresent(initializer -> {
            FZKeyed<BiConsumer<FZList, FZFlexLayout>> previousEntryInitializer = this.entryInitializer;
            this.entryInitializer = initializer;
            if (!Objects.equals(previousEntryInitializer, this.entryInitializer)) {
                this.initializeEntries();
            }
        });
    }

    public static FZList bind(String key, FZRef<Props> ref) {
        Props props = ref.value();
        FZList list = new FZList();
        list.applyProps(props);
        ref.subscribe(key, list::applyProps);
        return list;
    }

    public static Builder builder() {
        return new Builder();
    }

    private record Entry(String id, GuiEventListener widget) {
    }

    public interface Props extends GuiComponentProps {
        default OptionalInt maxContentWidth() {
            return OptionalInt.empty();
        }

        default OptionalInt contentPaddingLeft() {
            return OptionalInt.empty();
        }

        default OptionalInt contentPaddingRight() {
            return OptionalInt.empty();
        }

        default TriState reserveScrollbarWidth() {
            return TriState.DEFAULT;
        }

        default OptionalInt scrollRate() {
            return OptionalInt.empty();
        }

        default Optional<FZKeyed<BiConsumer<FZList, FZFlexLayout>>> entryInitializer() {
            return Optional.empty();
        }
    }

    protected static final class PropsImpl extends GuiComponentPropsBase implements Props {
        private final @Nullable FZKeyed<BiConsumer<FZList, FZFlexLayout>> entryInitializer;
        private final @Nullable Integer maxContentWidth;
        private final @Nullable Integer contentPaddingLeft;
        private final @Nullable Integer contentPaddingRight;
        private final TriState reserveScrollbarWidth;
        private final @Nullable Integer scrollRate;

        protected PropsImpl(
                GuiComponentProps props,
                @Nullable FZKeyed<BiConsumer<FZList, FZFlexLayout>> entryInitializer,
                @Nullable Integer maxContentWidth,
                @Nullable Integer contentPaddingLeft,
                @Nullable Integer contentPaddingRight,
                TriState reserveScrollbarWidth,
                @Nullable Integer scrollRate
        ) {
            super(props);
            this.entryInitializer = entryInitializer;
            this.maxContentWidth = maxContentWidth;
            this.contentPaddingLeft = contentPaddingLeft;
            this.contentPaddingRight = contentPaddingRight;
            this.reserveScrollbarWidth = reserveScrollbarWidth;
            this.scrollRate = scrollRate;
        }

        @Override
        public Optional<FZKeyed<BiConsumer<FZList, FZFlexLayout>>> entryInitializer() {
            return Optional.ofNullable(entryInitializer);
        }

        @Override
        public OptionalInt maxContentWidth() {
            return wrapBoxedInt(maxContentWidth);
        }

        @Override
        public OptionalInt contentPaddingLeft() {
            return wrapBoxedInt(contentPaddingLeft);
        }

        @Override
        public OptionalInt contentPaddingRight() {
            return wrapBoxedInt(contentPaddingRight);
        }

        @Override
        public TriState reserveScrollbarWidth() {
            return reserveScrollbarWidth;
        }

        @Override
        public OptionalInt scrollRate() {
            return wrapBoxedInt(scrollRate);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Props other)) return false;
            return super.equals(o)
                   && Objects.equals(maxContentWidth(), other.maxContentWidth())
                   && Objects.equals(contentPaddingLeft(), other.contentPaddingLeft())
                   && Objects.equals(contentPaddingRight(), other.contentPaddingRight())
                   && reserveScrollbarWidth == other.reserveScrollbarWidth()
                   && Objects.equals(scrollRate(), other.scrollRate())
                   && Objects.equals(entryInitializer(), other.entryInitializer());
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    super.hashCode(),
                    maxContentWidth,
                    contentPaddingLeft,
                    contentPaddingRight,
                    reserveScrollbarWidth,
                    scrollRate,
                    entryInitializer
            );
        }
    }

    public static final class Builder extends GuiComponentPropsBuilder<Builder> {
        private ScrollbarSettings settings = AbstractScrollArea.defaultSettings(DEFAULT_SCROLL_RATE);
        private @Nullable FZKeyed<BiConsumer<FZList, FZFlexLayout>> entryInitializer = null;
        private @Nullable Integer maxContentWidth;
        private @Nullable Integer contentPaddingLeft;
        private @Nullable Integer contentPaddingRight;
        private TriState reserveScrollbarWidth = TriState.DEFAULT;
        private @Nullable Integer scrollRate;

        private Builder() {
        }

        public Builder scrollbarSettings(ScrollbarSettings settings) {
            this.settings = Objects.requireNonNull(settings, "settings cannot be null");
            return this;
        }

        public Builder scrollRate(int scrollRate) {
            this.settings = new ScrollbarSettings(
                    this.settings.scrollerSprite(),
                    this.settings.disabledScrollerSprite(),
                    this.settings.backgroundSprite(),
                    this.settings.scrollbarWidth(),
                    this.settings.scrollbarMinHeight(),
                    scrollRate,
                    this.settings.resizingScrollbar()
            );
            this.scrollRate = scrollRate;
            return this;
        }

        public Builder entries(BiConsumer<FZList, FZFlexLayout> entryInitializer) {
            this.entryInitializer = FZKeyed.selfKey(entryInitializer);
            return this;
        }

        public Builder entries(Consumer<FZFlexLayout> entryInitializer) {
            Objects.requireNonNull(entryInitializer, "entryInitializer cannot be null");
            this.entryInitializer = FZKeyed.selfKey((_, layout) -> entryInitializer.accept(layout));
            return this;
        }


        public Builder entries(Object key, BiConsumer<FZList, FZFlexLayout> entryInitializer) {
            this.entryInitializer = new FZKeyed<>(key, Objects.requireNonNull(entryInitializer, "entryInitializer cannot be null"));
            return this;
        }

        public Builder maxContentWidth(int maxContentWidth) {
            this.maxContentWidth = maxContentWidth;
            return this;
        }

        public Builder contentPaddingLeft(int contentPaddingLeft) {
            this.contentPaddingLeft = contentPaddingLeft;
            return this;
        }

        public Builder contentPaddingRight(int contentPaddingRight) {
            this.contentPaddingRight = contentPaddingRight;
            return this;
        }

        public Builder contentPadding(int contentPadding) {
            return contentPaddingLeft(contentPadding).contentPaddingRight(contentPadding);
        }

        public Builder reserveScrollbarWidth(boolean reserveScrollbarWidth) {
            this.reserveScrollbarWidth = TriState.from(reserveScrollbarWidth);
            return this;
        }

        public Builder reserveScrollbarWidth() {
            return reserveScrollbarWidth(true);
        }

        public Props toProps() {
            return new PropsImpl(
                    props,
                    entryInitializer,
                    maxContentWidth,
                    contentPaddingLeft,
                    contentPaddingRight,
                    reserveScrollbarWidth,
                    scrollRate
            );
        }

        public FZList build() {
            FZList list = new FZList(DEFAULT_WIDTH, DEFAULT_HEIGHT, props.message().orElse(CommonComponents.EMPTY), settings);
            list.applyProps(toProps());
            list.initializeEntries();
            return list;
        }
    }
}
