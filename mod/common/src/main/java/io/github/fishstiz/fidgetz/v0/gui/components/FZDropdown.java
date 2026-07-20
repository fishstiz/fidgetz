package io.github.fishstiz.fidgetz.v0.gui.components;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.Fidgetz;
import io.github.fishstiz.fidgetz.v0.gui.state.FZRef;
import io.github.fishstiz.fidgetz.v0.gui.text.TextComponentUtils;
import io.github.fishstiz.fidgetz.v0.utils.*;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.*;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class FZDropdown extends Button implements FZComponent, FZContextMenu.Source, FZPopoverContainer, Layout {
    private static final int DEFAULT_ELEMENT_SPACING = 8;
    private static final int ENTRY_SPACING = 4;
    private static final int DEFAULT_SELECTION_HEIGHT = 200;
    private static final Component BLACK_RIGHT_POINTING_TRIANGLE = TextComponentUtils.BLACK_RIGHT_POINTING_TRIANGLE;
    private static final Component BLACK_DOWN_POINTING_TRIANGLE = TextComponentUtils.BLACK_DOWN_POINTING_TRIANGLE;
    private final GuiComponentPropsState propsState = new GuiComponentPropsState();
    private final SelectionContainer selectionContainer;
    private final ContainerEventHandler parentContainer;
    private final Font font;
    private List<FZPopoverMenuItem> items = Collections.emptyList();
    private boolean hideMessage;
    private @Nullable WidgetElements leftIcon;
    private Component interactSymbol = BLACK_RIGHT_POINTING_TRIANGLE;
    private Component inactiveInteractSymbol = inactiveMessage(interactSymbol);
    private int interactIconWidth;
    private boolean iconWidthDirty = true;
    private ScreenRectangle bounds;

    FZDropdown(int x, int y, int width, int height, Component message, ContainerEventHandler parentContainer) {
        super(x, y, width, height, message, FZButton.NOP, DEFAULT_NARRATION);
        this.font = Minecraft.getInstance().font;
        this.parentContainer = parentContainer;
        this.selectionContainer = new SelectionContainer(parentContainer);
        this.bounds = super.getRectangle();
    }

    FZDropdown(ContainerEventHandler parentContainer) {
        this(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT, CommonComponents.EMPTY, parentContainer);
    }

    private static Component inactiveMessage(Component message) {
        return ComponentUtils.mergeStyles(message.copy(), Style.EMPTY.withColor(CommonColors.LIGHT_GRAY));
    }

    public void openSelection() {
        selectionContainer.build(this.items);
        selectionContainer.repositionElements();
        selectionContainer.setOpen(true);
    }

    public void closeSelection() {
        selectionContainer.setOpen(false);
    }

    @Override
    public void onPress() {
        super.onPress();
        if (selectionContainer.isOpen()) {
            closeSelection();
        } else {
            openSelection();
        }
    }

    private int getInteractIconWidth() {
        if (iconWidthDirty) {
            interactIconWidth = font.width(interactSymbol);
            iconWidthDirty = false;
        }
        return interactIconWidth;
    }

    private Component getInteractSymbol() {
        return active ? interactSymbol : inactiveInteractSymbol;
    }

    @Override
    public void renderString(GuiGraphics graphics, Font font, int color) {
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        int left = getX();
        int top = getY();
        int width = getWidth();
        int height = getHeight();
        int right = getRight();
        int spacing = DEFAULT_ELEMENT_SPACING;

        right -= spacing + getInteractIconWidth();

        if (leftIcon != null) {
            left += spacing + leftIcon.margin().left();

            int iconY = (top + height / 2 - leftIcon.height() / 2) + leftIcon.margin().top() - leftIcon.margin().bottom();

            leftIcon.elements()
                    .get(isActive(), isHoveredOrFocused())
                    .extractRenderState(graphics, left, iconY, leftIcon.width(), leftIcon.height(), mouseX, mouseY, partialTick);

            left += spacing + leftIcon.width() + leftIcon.margin().right();
        }

        if (!hideMessage) {
            int color = isActive() ? CommonColors.WHITE : 0xFFA0A0A0;
            super.renderString(graphics, font, color | Mth.ceil(this.alpha * 255.0F) << 24);
        }

        int symbolX = MathUtils.clampOrAverage(right, getX() + spacing, right);
        int symbolY = top + (height / 2) - (font.lineHeight / 2);
        graphics.drawString(font, getInteractSymbol(), symbolX, symbolY, CommonColors.WHITE);

        if (propsState.overlay != null) {
            propsState.overlay.extractRenderState(graphics, left, top, width, height, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (selectionContainer.isOpen() && keyCode == InputConstants.KEY_ESCAPE) {
            closeSelection();
            parentContainer.setFocused(this);
            return true;
        }
        return false;
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenu.Collector collector) {
        propsState.contextEntries.accept(collector);
    }

//    @Override
//    public boolean shouldTakeFocusAfterInteraction() {
//        return propsState.focusOnInteraction;
//    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        ComponentPath path = super.nextFocusPath(navigationEvent);
        if (path != null || !isFocused() || !selectionContainer.isOpen() || selectionContainer.isFocused()) return path;

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
        if (selectionContainer.isOpen()) {
            selectionContainer.setX(selectionContainer.getX() + (x - getX()));
        }
        super.setX(x);
    }

    @Override
    public void setY(int y) {
        if (selectionContainer.isOpen()) {
            selectionContainer.setY(selectionContainer.getY() + (y - getY()));
        }
        super.setY(y);
    }

    @Override
    public void arrangeElements() {
        if (selectionContainer.isOpen()) {
            selectionContainer.repositionElements();
        }
    }

    @Override
    public @Nullable String fidgetz$componentId() {
        return propsState.id;
    }

    @Override
    public boolean fidgetz$shouldTakeFocusAfterInteraction() {
        return propsState.focusOnInteraction;
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
        props.containerBackground().ifPresent(selectionContainer::setBackground);
        props.maxContainerHeight().ifPresent(maxHeight -> selectionContainer.maxHeight = maxHeight);
        props.minContainerWidth().ifPresent(minContainerWidth -> {
            selectionContainer.minWidth = minContainerWidth.leftInt();
            selectionContainer.rootDirection = minContainerWidth.right();
        });
        props.entryDivider().ifDefined(selectionContainer::setEntryDivider);
        props.sectionDivider().ifDefined(selectionContainer::setSectionDivider);

        List<FZPopoverMenuItem> previousEntries = this.items;
        this.items = props.entries();
        if (selectionContainer.isOpen() && !Objects.equals(previousEntries, this.items)) {
            selectionContainer.build(this.items);
            selectionContainer.repositionElements();
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

    private final class SelectionContainer extends FZPopoverMenu {
        private HorizontalDirection rootDirection = HorizontalDirection.RIGHT;
        private int maxHeight = DEFAULT_SELECTION_HEIGHT;
        private int minWidth;
        private boolean focused;

        SelectionContainer(ContainerEventHandler container) {
            super(container);
            setPadding(ScreenRectangleUtils.insets(ENTRY_SPACING));
            setRowSpacing(ENTRY_SPACING);
        }

        private void updateIcon() {
            interactSymbol = isOpen() ? BLACK_DOWN_POINTING_TRIANGLE : BLACK_RIGHT_POINTING_TRIANGLE;
            inactiveInteractSymbol = inactiveMessage(interactSymbol);
            iconWidthDirty = true;
        }

        @Override
        protected void onOpen() {
            super.onOpen();
            updateIcon();
        }

        @Override
        protected void onClose() {
            boolean focused = parentContainer.getFocused() == this;
            super.onClose();
            if (focused) parentContainer.setFocused(FZDropdown.this);
            updateIcon();
        }

        @Override
        protected void build(List<FZPopoverMenuItem> items) {
            setMaxHeight(maxHeight);
            super.build(items);
        }

        @Override
        public void repositionElements() {
            ScreenRectangle parentBounds = parentContainer.getRectangle();
            ScreenRectangle buttonBounds = FZDropdown.this.getRectangle();
            ScreenRectangle selectionBounds = getRectangle();

            int selectionWidth = Math.max(buttonBounds.width(), minWidth);
            int selectionHeight = MathUtils.optionalMin(selectionBounds.height(), maxHeight);

            int spaceBelow = parentBounds.bottom() - buttonBounds.bottom();
            int spaceAbove = buttonBounds.top() - parentBounds.top();

            int anchor = rootDirection.flip().edge(buttonBounds);

            HorizontalDirection newDirection = rootDirection.resolve(parentBounds, selectionWidth, anchor);
            anchor = newDirection.flip().edge(buttonBounds);

            int selectionX = newDirection.clamp(parentBounds, selectionWidth, anchor);
            int selectionY;
            if (selectionHeight > spaceBelow) {
                if (spaceAbove > spaceBelow) {
                    selectionHeight = Math.min(selectionHeight, spaceAbove);
                    selectionY = buttonBounds.top() - selectionHeight;
                } else {
                    selectionHeight = spaceBelow;
                    selectionY = buttonBounds.bottom();
                }
            } else {
                selectionY = buttonBounds.bottom();
            }

            setMaxHeight(selectionHeight);
            setMinWidth(selectionWidth);
            setMaxWidth(selectionWidth);
            super.repositionElements();

            setX(selectionX);
            setY(selectionY);
        }

        @Override
        protected void extractDialogRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.extractDialogRenderState(graphics, mouseX, mouseY, partialTick);

            GuiEventListener sibling = parentContainer.getFocused();
            if ((sibling != this && sibling != FZDropdown.this) || !FZDropdown.this.isActive()) {
                closeSelection();
            }
        }

        @Override
        public boolean shouldCaptureFocus() {
            return false;
        }

        @Override
        public boolean shouldFocusOnOpen() {
            return false;
        }

        @Override
        public boolean shouldRefocusLastPath() {
            return false;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
            super.setFocused(focused);
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            if (focused == null || isOpen()) {
                super.setFocused(focused);
            }
        }

        @Override
        public boolean isFocused() {
            return isOpen() && (focused || getFocused() != null);
        }

        @Override
        public @Nullable ComponentPath getCurrentFocusPath() {
            if (!isFocused()) return null;
            return getFocused() == null ? ComponentPath.leaf(this) : super.getCurrentFocusPath();
        }

        private boolean isPositionedUp() {
            return getY() < FZDropdown.this.getY();
        }

        private boolean isPositionedDown() {
            return getY() > FZDropdown.this.getY();
        }

        @Override
        public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
            if (!isOpen()) return null;

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

        @Override
        public int getTabOrderGroup() {
            return FZDropdown.this.getTabOrderGroup();
        }

        @Override
        public boolean isActive() {
            return FZDropdown.this.isActive() && super.isActive();
        }
    }

    public interface Props extends GuiComponentProps {
        ContainerEventHandler parentContainer();

        List<FZPopoverMenuItem> entries();

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

        default Undefinable<FZPopoverMenuItem.@Nullable Divider> entryDivider() {
            return Undefinable.undefined();
        }

        default Undefinable<FZPopoverMenuItem.@Nullable Divider> sectionDivider() {
            return Undefinable.undefined();
        }
    }

    private static final class PropsImpl extends GuiComponentPropsBase implements Props {
        private final ContainerEventHandler parentContainer;
        private final List<FZPopoverMenuItem> entries;
        private final TriState hideMessage;
        private final Undefinable<@Nullable WidgetElements> leftIcon;
        private final @Nullable RenderableRectangle containerBackground;
        private final @Nullable Integer maxContainerHeight;
        private final @Nullable IntObjectPair<HorizontalDirection> minContainerWidth;
        private final Undefinable<FZPopoverMenuItem.@Nullable Divider> entryDivider;
        private final Undefinable<FZPopoverMenuItem.@Nullable Divider> sectionDivider;

        private PropsImpl(
                ContainerEventHandler parentContainer,
                List<FZPopoverMenuItem> entries,
                TriState hideMessage,
                Undefinable<@Nullable WidgetElements> leftIcon,
                @Nullable RenderableRectangle containerBackground,
                @Nullable Integer maxContainerHeight,
                @Nullable IntObjectPair<HorizontalDirection> minContainerWidth,
                Undefinable<FZPopoverMenuItem.@Nullable Divider> entryDivider,
                Undefinable<FZPopoverMenuItem.@Nullable Divider> sectionDivider,
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
            this.sectionDivider = sectionDivider;
        }

        @Override
        public ContainerEventHandler parentContainer() {
            return parentContainer;
        }

        @Override
        public List<FZPopoverMenuItem> entries() {
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
        public Undefinable<FZPopoverMenuItem.@Nullable Divider> entryDivider() {
            return entryDivider;
        }

        @Override
        public Undefinable<FZPopoverMenuItem.@Nullable Divider> sectionDivider() {
            return sectionDivider;
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
                   Objects.equals(entryDivider(), other.entryDivider()) &&
                   Objects.equals(sectionDivider(), other.sectionDivider());
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
        private static final WidgetRenderables DEFAULT_ENTRY_BACKGROUND;
        private final ContainerEventHandler container;
        private final List<FZPopoverMenuItem> entries = new ArrayList<>();
        private TriState hideMessage = TriState.DEFAULT;
        private Undefinable<@Nullable WidgetElements> leftIcon = Undefinable.undefined();
        private @Nullable RenderableRectangle containerBackground;
        private @Nullable Integer maxContainerHeight;
        private @Nullable IntObjectPair<HorizontalDirection> minContainerWidth;
        private Undefinable<FZPopoverMenuItem.@Nullable Divider> entryDivider = Undefinable.undefined();
        private Undefinable<FZPopoverMenuItem.@Nullable Divider> sectionDivider = Undefinable.undefined();

        static {
            RenderableRectangle background = Renderables.sprite(Fidgetz.id("widget/popovermenu_entry")).withBlend();
            RenderableRectangle higlighted = Renderables.sprite(Fidgetz.id("widget/popovermenu_entry_highlighted")).withBlend();
            DEFAULT_ENTRY_BACKGROUND = new WidgetRenderables(background, background, higlighted);
        }

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

        private FZButton.Builder defaultButtonBuilder() {
            return FZButton.builder()
                    .height(DEFAULT_HEIGHT)
                    .sprites(DEFAULT_ENTRY_BACKGROUND)
                    .leftAlignedMessage();
        }

        public Builder entry(Component message, Runnable selectionHandler) {
            Objects.requireNonNull(selectionHandler, "selectionHandler cannot be null");
            this.entries.add(FZPopoverMenuItem.fromWidget(defaultButtonBuilder().message(message).onPress(selectionHandler).build()));
            return this;
        }

        public Builder entry(UnaryOperator<FZButton.Builder> entryBuilder) {
            this.entries.add(FZPopoverMenuItem.fromWidget(entryBuilder.apply(defaultButtonBuilder()).build()));
            return this;
        }

        public Builder entry(FZPopoverMenuItem entry) {
            this.entries.add(entry);
            return this;
        }

        public Builder entries(List<FZPopoverMenuItem> entries) {
            this.entries.addAll(entries);
            return this;
        }

        public Builder entryDivider(FZPopoverMenuItem.@Nullable Divider entryDivider) {
            this.entryDivider = Undefinable.of(entryDivider);
            return this;
        }

        public Builder sectionDivider(FZPopoverMenuItem.@Nullable Divider sectionDivider) {
            this.sectionDivider = Undefinable.of(sectionDivider);
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
                    sectionDivider,
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
