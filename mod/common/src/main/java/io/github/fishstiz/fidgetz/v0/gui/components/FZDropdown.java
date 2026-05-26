package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.Fidgetz;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZScrollableLayout;
import io.github.fishstiz.fidgetz.v0.gui.state.FZKeyed;
import io.github.fishstiz.fidgetz.v0.gui.state.FZRef;
import io.github.fishstiz.fidgetz.v0.utils.*;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.text.TextComponentUtils;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TriState;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class FZDropdown extends Button.Plain implements FZComponent, FZContextMenuEntry.Source, FZPopoverContainer, Layout {
    private static final int DEFAULT_ELEMENT_SPACING = 8;
    private static final int ENTRY_SPACING = 4;
    private static final int DEFAULT_SELECTION_HEIGHT = 200;
    private static final RenderableRectangle DEFAULT_DIVIDER = Renderables.sprite(Fidgetz.id("widget/dropdown_entry_divider"));
    private final GuiComponentPropsState propsState = new GuiComponentPropsState();
    private final SelectionContainer selectionContainer = new SelectionContainer();
    private final ContainerEventHandler parentContainer;
    private final Font font;
    private final int collapseWidth;
    private final int expandWidth;
    private List<Entry> entries = Collections.emptyList();
    private boolean hideMessage;
    private @Nullable WidgetElements leftIcon;
    private int maxContainerHeight = DEFAULT_SELECTION_HEIGHT;
    private int minContainerWidth;
    private HorizontalDirection preferredDirection = HorizontalDirection.RIGHT;
    private RenderableRectangle entryDivider = DEFAULT_DIVIDER;
    private Component interactSymbol = TextComponentUtils.BLACK_RIGHT_POINTING_TRIANGLE;
    private Component inactiveInteractSymbol = defaultInactiveMessage(interactSymbol);
    private int interactIconWidth;
    private ScreenRectangle bounds;

    FZDropdown(int x, int y, int width, int height, Component message, ContainerEventHandler parentContainer) {
        super(x, y, width, height, message, FZButton.NOP, DEFAULT_NARRATION);
        this.font = Minecraft.getInstance().font;
        this.collapseWidth = font.width(TextComponentUtils.BLACK_RIGHT_POINTING_TRIANGLE);
        this.expandWidth = font.width(TextComponentUtils.BLACK_DOWN_POINTING_TRIANGLE);
        this.interactIconWidth = collapseWidth;
        this.parentContainer = parentContainer;
        this.bounds = super.getRectangle();
    }

    FZDropdown(ContainerEventHandler parentContainer) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, CommonComponents.EMPTY, parentContainer);
    }

    private void initializeSelection() {
        FZFlexLayout layout = FZLayouts.flexVertical().spacing(1);
        layout.defaultChildSettings().flexCross();

        for (int i = 0; i < entries.size(); i++) {
            layout.child(entries.get(i).createButton());
            if (i + 1 < entries.size()) {
                layout.child(FZIcon.builder(entryDivider).height(ENTRY_SPACING).build());
            }
        }

        selectionContainer.setLayout(FZLayouts.composer(parentContainer, layout)
                .padded(ENTRY_SPACING)
                .scrollable()
                .get()
                .scrollbarSpacing(0));
    }

    public void openSelection() {
        initializeSelection();
        selectionContainer.setOpen(true);
        arrangeElements();
    }

    public void closeSelection() {
        selectionContainer.setOpen(false);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        super.onPress(input);
        if (selectionContainer.open) {
            closeSelection();
        } else {
            openSelection();
        }
    }

    private Component getInteractSymbol() {
        return active ? interactSymbol : inactiveInteractSymbol;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractDefaultSprite(graphics);

        int left = getX();
        int top = getY();
        int width = getWidth();
        int height = getHeight();
        int right = getRight();
        int spacing = DEFAULT_ELEMENT_SPACING;

        right -= spacing + interactIconWidth;

        if (leftIcon != null) {
            left += spacing + leftIcon.margin().left();

            int iconY = (top + height / 2 - leftIcon.height() / 2) + leftIcon.margin().top() - leftIcon.margin().bottom();

            leftIcon.elements()
                    .get(isActive(), isHoveredOrFocused())
                    .extractRenderState(graphics, left, iconY, leftIcon.width(), leftIcon.height(), mouseX, mouseY, partialTick);

            left += spacing + leftIcon.width() + leftIcon.margin().right();
        }

        ActiveTextCollector textRenderer = graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE);

        if (!hideMessage) {
            GuiGraphicsUtils.scrollingText(textRenderer, getMessage(), left + spacing, top, right - spacing, getBottom());
        }

        int symbolX = MathUtils.clampOrAverage(right, getX() + spacing, right);
        int symbolY = top + (height / 2) - (font.lineHeight / 2);
        textRenderer.accept(symbolX, symbolY, getInteractSymbol());

        if (propsState.overlay != null) {
            propsState.overlay.extractRenderState(graphics, left, top, width, height, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        if (selectionContainer.open && event.isEscape()) {
            closeSelection();
            parentContainer.setFocused(this);
            return true;
        }
        return false;
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenuEntry.Collector collector) {
        propsState.contextEntries.accept(collector);
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return propsState.focusOnInteraction;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        ComponentPath path = super.nextFocusPath(navigationEvent);
        if (path != null || !isFocused() || !selectionContainer.open || selectionContainer.isFocused()) return path;

        boolean up = NavigationUtils.isUp(navigationEvent, true);
        boolean down = NavigationUtils.isDown(navigationEvent, true);

        if ((up && selectionContainer.isPositionedUp()) || (down && selectionContainer.isPositionedDown())) {
            return selectionContainer.nextFocusPath(navigationEvent);
        }

        if (up || down) {
            return getCurrentFocusPath();
        }

        return null;
    }

    @Override
    public ScreenRectangle getRectangle() {
        if (ScreenRectangleUtils.unequal(bounds, this)) {
            this.bounds = super.getRectangle();
        }
        return this.bounds;
    }

    @Override
    public void fidgetz$visitPopovers(Consumer<FZPopover> visitor) {
        visitor.accept(selectionContainer);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> layoutElementVisitor) {
        layoutElementVisitor.accept(this);
    }

    @Override
    public void setX(int x) {
        if (selectionContainer.open) {
            selectionContainer.setX(selectionContainer.getX() + (x - getX()));
        }
        super.setX(x);
    }

    @Override
    public void setY(int y) {
        if (selectionContainer.open) {
            selectionContainer.setY(selectionContainer.getY() + (y - getY()));
        }
        super.setY(y);
    }

    @Override
    public void arrangeElements() {
        if (!selectionContainer.open) return;

        selectionContainer.arrangeElements();

        int containerWidth = Math.max(getWidth(), minContainerWidth);
        int containerHeight = Math.min(selectionContainer.getHeight(), maxContainerHeight);

        ScreenRectangle containerBounds = parentContainer.getRectangle();
        int spaceBelow = containerBounds.bottom() - getBottom();
        int spaceAbove = getY() - containerBounds.top();

        int anchor = preferredDirection.flip().edge(getRectangle());
        HorizontalDirection resolvedDirection = preferredDirection.resolve(containerBounds, containerWidth, anchor);
        anchor = resolvedDirection.flip().edge(getRectangle());
        int containerX = resolvedDirection.clamp(containerBounds, containerWidth, anchor);

        int containerY;
        if (containerHeight > spaceBelow) {
            if (spaceAbove > spaceBelow) {
                containerHeight = Math.min(containerHeight, spaceAbove);
                containerY = getY() - containerHeight;
            } else {
                containerHeight = spaceBelow;
                containerY = getBottom();
            }
        } else {
            containerY = getBottom();
        }

        selectionContainer.setSize(containerWidth, containerHeight);
        selectionContainer.setPosition(containerX, containerY);
    }

    @Override
    public @Nullable String fidgetz$componentId() {
        return propsState.id;
    }

    void applyProps(Props props) {
        if (props.parentContainer() != parentContainer) {
            throw new UnsupportedOperationException("Updating the parent container of FZDropdown is not supported.");
        }

        propsState.apply(this, props);

        if (props.hideMessage() != TriState.DEFAULT) {
            hideMessage = props.hideMessage().toBoolean(false);
        }
        props.leftIcon().ifDefined(leftIcon -> this.leftIcon = leftIcon);
        props.containerBackground().ifPresent(containerBackground -> selectionContainer.background = containerBackground);
        props.maxContainerHeight().ifPresent(maxContainerHeight -> {
            this.maxContainerHeight = maxContainerHeight;
            selectionContainer.setHeight(maxContainerHeight);
        });
        props.minContainerWidth().ifPresent(minContainerWidth -> {
            this.minContainerWidth = minContainerWidth.leftInt();
            this.preferredDirection = minContainerWidth.right();
        });
        props.entryDivider().ifPresent(entryDivider -> this.entryDivider = entryDivider);

        List<Entry> previousEntries = this.entries;
        this.entries = props.entries();
        if (selectionContainer.open && !Objects.equals(previousEntries, this.entries)) {
            initializeSelection();
        }
    }

    public static FZDropdown bind(String key, FZRef<Props> ref) {
        Props props = ref.value();
        FZDropdown dropdown = new FZDropdown(props.parentContainer());
        dropdown.applyProps(props);
        ref.subscribe(key, dropdown::applyProps);
        return dropdown;
    }

    public static Builder builder(ContainerEventHandler container) {
        return new Builder(container);
    }

    private final class SelectionContainer extends FZContainer implements FZPopover, Layout {
        private static final RenderableRectangle DEFAULT_BACKGROUND = Renderables.boxShadow(24)
                .then(Renderables.sprite(Fidgetz.id("widget/dropdown_container")));
        private RenderableRectangle background = DEFAULT_BACKGROUND;
        private @Nullable FZScrollableLayout layout;
        private boolean focused;
        private boolean open;

        private void setOpen(boolean open) {
            if (!open) {
                ComponentPath path = selectionContainer.getCurrentFocusPath();
                if (path != null) path.applyFocus(false);
            }

            selectionContainer.open = open;
            interactSymbol = open ? TextComponentUtils.BLACK_DOWN_POINTING_TRIANGLE : TextComponentUtils.BLACK_RIGHT_POINTING_TRIANGLE;
            inactiveInteractSymbol = defaultInactiveMessage(interactSymbol);
            interactIconWidth = open ? expandWidth : collapseWidth;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return getChildAt(event.x(), event.y()).map(child -> {
                if (child.mouseClicked(event, doubleClick)) {
                    closeSelection();
                    parentContainer.setFocused(FZDropdown.this);
                    return true;
                }
                return false;
            }).orElse(false);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (open) {
                background.extractRenderState(graphics, getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, partialTick);
                super.extractRenderState(graphics, mouseX, mouseY, partialTick);

                GuiEventListener sibling = parentContainer.getFocused();
                if (sibling != this && sibling != FZDropdown.this) {
                    closeSelection();
                }
            }
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (super.keyPressed(event)) {
                return true;
            }
            if (event.isEscape()) {
                closeSelection();
                parentContainer.setFocused(FZDropdown.this);
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldTakeFocusAfterInteraction() {
            return open;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
            super.setFocused(focused);
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            if (focused == null || open) {
                super.setFocused(focused);
            }
        }

        @Override
        public boolean isFocused() {
            return open && (focused || getFocused() != null);
        }

        private boolean isPositionedUp() {
            return getY() < FZDropdown.this.getY();
        }

        private boolean isPositionedDown() {
            return getY() > FZDropdown.this.getY();
        }

        @Override
        public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
            if (!open) return null;

            ComponentPath path = super.nextFocusPath(navigationEvent);
            if (path != null || !isFocused() || FZDropdown.this.isFocused()) return path;

            boolean up = NavigationUtils.isUp(navigationEvent, true);
            boolean down = NavigationUtils.isDown(navigationEvent, true);

            if ((up && isPositionedDown()) || (down && isPositionedUp())) {
                return FZDropdown.this.nextFocusPath(navigationEvent);
            }

            if (up || down) {
                return getCurrentFocusPath();
            }

            return null;
        }

        private void setLayout(FZScrollableLayout layout) {
            this.layout = layout;
            clearWidgets();
            layout.visitWidgets(this::addRenderableWidget);
        }

        @Override
        public void setX(int x) {
            if (layout != null) layout.setX(x);
        }

        @Override
        public void setY(int y) {
            if (layout != null) layout.setY(y);
        }

        @Override
        public void setPosition(int x, int y) {
            if (layout != null) layout.setPosition(x, y);
        }

        @Override
        public int getX() {
            return layout == null ? 0 : layout.getX();
        }

        @Override
        public int getY() {
            return layout == null ? 0 : layout.getY();
        }

        private void setHeight(int height) {
            if (layout != null) {
                layout.maxHeight(height);
                layout.arrangeElements();
            }
        }

        private void setSize(int width, int height) {
            if (layout != null) {
                layout.maxHeight(height);
                layout.fidgetz$setWidth(width);
                layout.arrangeElements();
            }
        }

        @Override
        public int getWidth() {
            return layout == null ? 0 : layout.getWidth();
        }

        @Override
        public int getHeight() {
            return layout == null ? 0 : layout.getHeight();
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return open && getRectangle().containsPoint((int) mouseX, (int) mouseY);
        }

        @Override
        public ScreenRectangle getRectangle() {
            return layout == null || !open ? ScreenRectangle.empty() : layout.getRectangle();
        }

        @Override
        public void arrangeElements() {
            if (layout != null) layout.arrangeElements();
        }

        @Override
        public void fidgetz$visitWidgets(WidgetVisitor visitor) {
            visitor.visitWidget(this);
        }

        @Override
        public void fidgetz$visitRenderables(Consumer<Renderable> visitor) {
            visitor.accept(this);
        }

        @Override
        public void visitChildren(Consumer<LayoutElement> layoutElementVisitor) {
            if (layout != null) layout.visitChildren(layoutElementVisitor);
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> widgetVisitor) {
            if (layout != null) layout.visitWidgets(widgetVisitor);
        }

        @Override
        public int getTabOrderGroup() {
            return FZDropdown.this.getTabOrderGroup();
        }
    }

    public record Entry(FZKeyed<UnaryOperator<FZButton.Builder>> builderFactory) {
        private static final WidgetRenderables DEFAULT_BACKGROUND = new WidgetRenderables(
                Renderables.sprite(Fidgetz.id("widget/dropdown_entry")),
                Renderables.sprite(Fidgetz.id("widget/dropdown_entry")),
                Renderables.sprite(Fidgetz.id("widget/dropdown_entry_highlighted"))
        );

        public Entry(UnaryOperator<FZButton.Builder> builderFactory) {
            this(FZKeyed.selfKey(builderFactory));
        }

        private FZButton createButton() {
            FZButton.Builder defaultBuilder = FZButton.builder()
                    .height(DEFAULT_HEIGHT)
                    .sprites(DEFAULT_BACKGROUND)
                    .leftAlignedMessage();

            return builderFactory.value().apply(defaultBuilder).build();
        }
    }

    public interface Props extends GuiComponentProps {
        ContainerEventHandler parentContainer();

        List<Entry> entries();

        default TriState hideMessage() {
            return TriState.DEFAULT;
        }

        default Undefinable<@Nullable WidgetElements> leftIcon() {
            return Undefinable.undefined();
        }

        default Optional<RenderableRectangle> containerBackground() {
            return Optional.empty();
        }

        default OptionalInt maxContainerHeight() {
            return OptionalInt.empty();
        }

        default Optional<IntObjectPair<HorizontalDirection>> minContainerWidth() {
            return Optional.empty();
        }

        default Optional<RenderableRectangle> entryDivider() {
            return Optional.empty();
        }
    }

    private static final class PropsImpl extends GuiComponentPropsBase implements Props {
        private final ContainerEventHandler parentContainer;
        private final List<Entry> entries;
        private final TriState hideMessage;
        private final Undefinable<@Nullable WidgetElements> leftIcon;
        private final @Nullable RenderableRectangle containerBackground;
        private final @Nullable Integer maxContainerHeight;
        private final @Nullable IntObjectPair<HorizontalDirection> minContainerWidth;
        private final @Nullable RenderableRectangle entryDivider;

        private PropsImpl(
                ContainerEventHandler parentContainer,
                List<Entry> entries,
                TriState hideMessage,
                Undefinable<@Nullable WidgetElements> leftIcon,
                @Nullable RenderableRectangle containerBackground,
                @Nullable Integer maxContainerHeight,
                @Nullable IntObjectPair<HorizontalDirection> minContainerWidth,
                @Nullable RenderableRectangle entryDivider,
                GuiComponentProps props
        ) {
            super(props);
            this.parentContainer = parentContainer;
            this.entries = entries;
            this.hideMessage = hideMessage;
            this.leftIcon = leftIcon;
            this.containerBackground = containerBackground;
            this.maxContainerHeight = maxContainerHeight;
            this.minContainerWidth = minContainerWidth;
            this.entryDivider = entryDivider;
        }

        @Override
        public ContainerEventHandler parentContainer() {
            return parentContainer;
        }

        @Override
        public List<Entry> entries() {
            return entries;
        }

        @Override
        public TriState hideMessage() {
            return hideMessage;
        }

        @Override
        public Undefinable<@Nullable WidgetElements> leftIcon() {
            return leftIcon;
        }

        @Override
        public Optional<RenderableRectangle> containerBackground() {
            return Optional.ofNullable(containerBackground);
        }

        @Override
        public OptionalInt maxContainerHeight() {
            return wrapBoxedInt(maxContainerHeight);
        }

        @Override
        public Optional<IntObjectPair<HorizontalDirection>> minContainerWidth() {
            return Optional.ofNullable(minContainerWidth);
        }

        @Override
        public Optional<RenderableRectangle> entryDivider() {
            return Optional.ofNullable(entryDivider);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Props other)) return false;
            return super.equals(o) &&
                   Objects.equals(parentContainer, other.parentContainer()) &&
                   Objects.equals(entries, other.entries()) &&
                   hideMessage == other.hideMessage() &&
                   Objects.equals(leftIcon, other.leftIcon()) &&
                   Objects.equals(containerBackground(), other.containerBackground()) &&
                   Objects.equals(maxContainerHeight(), other.maxContainerHeight()) &&
                   Objects.equals(entryDivider(), other.entryDivider());
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    super.hashCode(),
                    parentContainer,
                    entries,
                    hideMessage,
                    leftIcon,
                    containerBackground,
                    maxContainerHeight,
                    entryDivider
            );
        }
    }

    public static final class Builder extends GuiComponentPropsBuilder<Builder> {
        private final ContainerEventHandler container;
        private final List<Entry> entries = new ArrayList<>();
        private TriState hideMessage = TriState.DEFAULT;
        private Undefinable<@Nullable WidgetElements> leftIcon = Undefinable.undefined();
        private @Nullable RenderableRectangle containerBackground;
        private @Nullable Integer maxContainerHeight;
        private @Nullable IntObjectPair<HorizontalDirection> minContainerWidth;
        private @Nullable RenderableRectangle entryDivider;

        private Builder(ContainerEventHandler container) {
            this.container = container;
        }

        public Builder hideMessage(boolean hideMessage) {
            this.hideMessage = TriState.from(hideMessage);
            return this;
        }

        public Builder hideMessage() {
            return hideMessage(true);
        }

        public Builder leftIcon(@Nullable WidgetElements leftIcon) {
            this.leftIcon = Undefinable.of(leftIcon);
            return this;
        }

        public Builder containerBackground(@Nullable RenderableRectangle containerBackground) {
            this.containerBackground = containerBackground;
            return this;
        }

        public Builder maxContainerHeight(int maxContainerHeight) {
            this.maxContainerHeight = maxContainerHeight;
            return this;
        }

        public Builder minContainerWidth(int minContainerWidth, HorizontalDirection preferredDirection) {
            this.minContainerWidth = IntObjectPair.of(minContainerWidth, preferredDirection);
            return this;
        }

        public Builder minContainerWidth(int minContainerWidth) {
            this.minContainerWidth = this.minContainerWidth == null
                    ? IntObjectPair.of(minContainerWidth, HorizontalDirection.RIGHT)
                    : IntObjectPair.of(minContainerWidth, this.minContainerWidth.right());

            return this;
        }

        public Builder entries(List<Entry> options) {
            this.entries.addAll(options);
            return this;
        }

        public Builder entries(Entry... options) {
            this.entries.addAll(Arrays.asList(options));
            return this;
        }

        public Builder entry(Component message, Runnable selectionHandler) {
            Objects.requireNonNull(selectionHandler, "selectionHandler cannot be null");
            this.entries.add(new Entry(builder -> builder.message(message)));
            return this;
        }

        public Builder entry(UnaryOperator<FZButton.Builder> entryBuilder) {
            this.entries.add(new Entry(entryBuilder));
            return this;
        }

        public Builder entry(Entry entry) {
            this.entries.add(entry);
            return this;
        }

        public Builder entryDivider(@Nullable RenderableRectangle entryDivider) {
            this.entryDivider = entryDivider;
            return this;
        }

        public Props toProps() {
            return new PropsImpl(
                    container,
                    List.copyOf(entries),
                    hideMessage,
                    leftIcon,
                    containerBackground,
                    maxContainerHeight,
                    minContainerWidth,
                    entryDivider,
                    props
            );
        }

        public FZDropdown build() {
            FZDropdown dropdown = new FZDropdown(container);
            dropdown.applyProps(toProps());
            return dropdown;
        }
    }
}
