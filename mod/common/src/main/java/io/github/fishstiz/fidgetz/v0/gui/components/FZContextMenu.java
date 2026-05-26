package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.Fidgetz;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.utils.NavigationUtils;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class FZContextMenu extends FZDialog {
    private static final ScreenRectangle DEFAULT_PADDING = ScreenRectangleUtils.insets(4);
    private static final RenderableRectangle DEFAULT_BACKGROUND = Renderables.boxShadow(24)
            .then(Renderables.sprite(Fidgetz.id("widget/contextmenu")));
    private static final int DEFAULT_MAX_HEIGHT = 240;
    private static final int DEFAULT_ENTRY_WIDTH = 150;
    private static final int DEFAULT_SPACING = 1;
    private final HorizontalDirection preferredDirection;
    private final @Nullable RenderableRectangle background;
    private final FZContextMenuEntry.@Nullable Divider sectionDivider;
    private final FZContextMenuEntry.@Nullable Divider entryDivider;
    private final ScreenRectangle padding;
    private final int rowSpacing;
    private final int maxHeight;
    private final int minWidth;
    private final boolean focusOnOpen;
    private final @Nullable FZContextMenu parent;
    private @Nullable ChildDetails child;
    private HorizontalDirection direction;
    private ScreenRectangle bounds = ScreenRectangle.empty();

    private FZContextMenu(
            ContainerEventHandler container,
            HorizontalDirection preferredDirection,
            @Nullable RenderableRectangle background,
            FZContextMenuEntry.@Nullable Divider sectionDivider,
            FZContextMenuEntry.@Nullable Divider entryDivider,
            ScreenRectangle padding,
            int rowSpacing,
            int maxHeight,
            int minWidth,
            boolean focusOnOpen,
            @Nullable FZContextMenu parent
    ) {
        super(container);
        this.preferredDirection = preferredDirection;
        this.direction = preferredDirection;
        this.background = background;
        this.sectionDivider = sectionDivider;
        this.entryDivider = entryDivider;
        this.padding = padding;
        this.rowSpacing = rowSpacing;
        this.maxHeight = maxHeight;
        this.minWidth = minWidth;
        this.focusOnOpen = focusOnOpen;
        this.parent = parent;
    }

    @Override
    public boolean shouldCaptureClick() {
        return false;
    }

    @Override
    public boolean shouldFocusOnOpen() {
        return parent == null && focusOnOpen;
    }

    @Override
    public boolean shouldRefocusLastPath() {
        return parent == null;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return bounds;
    }

    private void open(int x, int y, HorizontalDirection direction, List<? extends FZContextMenuEntry> entries) {
        if (entries.isEmpty()) {
            close();
            return;
        }

        FZFlexLayout content = FZLayouts.flexVertical().spacing(rowSpacing);
        int entryCount = 0;
        int entryWidth = minWidth;

        for (int i = 0; i < entries.size(); i++) {
            FZContextMenuEntry current = entries.get(i);
            FZContextMenuEntry next = (i + 1 < entries.size()) ? entries.get(i + 1) : null;
            boolean canDivideNext = next != null && !(next instanceof FZContextMenuEntry.Divider);

            if (current instanceof FZContextMenuEntry.Divider divider) {
                if (canDivideNext && entryCount > 0) {
                    content.child(new EntryWidget(divider.fallback() && sectionDivider != null ? sectionDivider : current));
                }
            } else {
                content.child(new EntryWidget(current));
                entryWidth = Math.max(entryWidth, current.minWidth());
                entryCount++;
                if (canDivideNext && entryDivider != null && current.canAutoDivideAfterEntry()) {
                    content.child(new EntryWidget(entryDivider));
                }
            }
        }

        if (entryCount == 0) {
            close();
            return;
        }

        if (entryWidth > minWidth) {
            int width = entryWidth;
            content.visitWidgets(widget -> widget.setWidth(width));
        }

        Layout layout = FZLayouts.composer(container, content)
                .padded(padding.left(), padding.top(), padding.right(), padding.bottom())
                .scrollable(scrollable -> scrollable
                        .scrollbarSpacing(0)
                        .minWidth(minWidth)
                        .maxHeight(maxHeight))
                .arrange()
                .get();

        ScreenRectangle targetBounds = layout.getRectangle();
        ScreenRectangle containerBounds = container.getRectangle();
        HorizontalDirection newDirection = direction.resolve(containerBounds, targetBounds.width(), x);
        layout.setX(newDirection.clamp(containerBounds, targetBounds.width(), x));
        layout.setY(Math.max(0, y + targetBounds.height() > containerBounds.height() ? containerBounds.height() - targetBounds.height() : y));

        this.direction = newDirection;
        this.bounds = layout.getRectangle();

        clearWidgets();
        layout.visitWidgets(this::addRenderableWidget);
        setOpen(true);
    }

    public void open(double x, double y, List<? extends FZContextMenuEntry> entries) {
        open((int) x, (int) y, preferredDirection, entries);
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        this.child = null;
    }

    @Override
    protected void onClose() {
        super.onClose();

        if (child != null) {
            child.menu.close();
        }

        clearWidgets();
        this.direction = preferredDirection;
        this.bounds = ScreenRectangle.empty();

        if (parent != null) {
            if (parent.child != null) {
                parent.child.key.setFocused(false);
            }

            parent.child = null;

            if (parent.getFocused() == this) {
                parent.setFocused(null);
                ComponentPath path = menuPath(parent, null);
                if (path != null) {
                    path.applyFocus(true);
                }
            }
        }
    }

    public void close() {
        setOpen(false);
    }

    private void closeAll() {
        if (parent != null) {
            parent.closeAll();
        } else {
            close();
        }
    }

    private void closeChild() {
        if (child != null) {
            child.menu.close();
        }
    }

    private void openChild(EntryWidget entry, List<? extends FZContextMenuEntry> entries) {
        if (child != null && child.key == entry) return;

        FZContextMenu menu = new FZContextMenu(
                container,
                preferredDirection,
                background,
                sectionDivider,
                entryDivider,
                padding,
                rowSpacing,
                maxHeight,
                minWidth,
                focusOnOpen,
                this
        );

        ScreenRectangle entryBounds = entry.getRectangle();
        int anchor = direction.edge(entryBounds);
        HorizontalDirection newDirection = direction.resolve(container.getRectangle(), getRectangle().width(), anchor);
        int x = newDirection.edge(entryBounds);
        int y = entryBounds.top();

        menu.open(x, y, newDirection, entries);

        closeChild();
        addWidgetFirst(menu);

        child = new ChildDetails(entry, menu);
    }

    @Override
    protected void extractDialogRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (background != null) {
            background.extractRenderState(graphics, bounds.left(), bounds.top(), bounds.width(), bounds.height(), mouseX, mouseY, partialTick);
        }
        super.extractDialogRenderState(graphics, mouseX, mouseY, partialTick);
        if (child != null) {
            child.menu.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    protected boolean areCoordinatesInBounds(double x, double y) {
        return super.areCoordinatesInBounds(x, y) || (child != null && child.menu.areCoordinatesInBounds(x, y));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || (child != null && child.menu.isMouseOver(mouseX, mouseY));
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (child != null) {
            ComponentPath path = child.menu.nextFocusPath(event);
            if (path != null) {
                return path;
            }
        }

        return super.nextFocusPath(event);
    }

    private static @Nullable ComponentPath menuPath(FZContextMenu current, @Nullable ComponentPath childPath) {
        if (childPath == null) {
            return current.parent == null ? ComponentPath.path(current, current.container) : menuPath(current.parent, ComponentPath.leaf(current));
        }
        ComponentPath path = childPath.component() == current ? childPath : ComponentPath.path(current, childPath);
        return current.parent == null ? ComponentPath.path(current.container, path) : menuPath(current.parent, path);
    }

    public static Builder builder(FZDialogContainer container) {
        return new Builder(container);
    }

    private record ChildDetails(EntryWidget key, FZContextMenu menu) {
    }

    private final class EntryWidget extends AbstractWidget implements FZContextMenuEntry.Context {
        private final FZContextMenuEntry entry;
        private ScreenRectangle entryBounds = ScreenRectangle.empty();
        private boolean lastHovered;

        private EntryWidget(FZContextMenuEntry entry) {
            super(0, 0, FZContextMenu.this.minWidth, entry.height(), entry.message());
            this.entry = entry;
        }

        @Override
        public void closeMenu() {
            closeAll();
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (isActive() && entry.mouseClicked(event, this)) {
                if (entry.shouldCloseOnInteraction()) {
                    closeMenu();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldTakeFocusAfterInteraction() {
            return isOpen();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            entry.extractRenderState(graphics, this, mouseX, mouseY, partialTick);
            boolean hovered = isHovered();
            if (hovered && !lastHovered) {
                updateChild();
            }
            lastHovered = hovered;
        }

        private void updateChild() {
            if (FZContextMenu.this.getFocused() == null || !FZContextMenu.this.isDragging()) {
                List<? extends FZContextMenuEntry> children = entry.childEntries();
                if (children.isEmpty()) {
                    closeChild();
                } else {
                    openChild(this, children);
                }
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            Component message = getMessage();
            output.add(NarratedElementType.TITLE, AbstractWidget.wrapDefaultNarrationMessage(message));
            if (isActive()) {
                if (isFocused()) {
                    output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
                } else if (isHovered()) {
                    output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
                }
            }
            entry.updateNarration(output, this);
        }

        @Override
        public boolean isActive() {
            return entry.active();
        }

        @Override
        public Component getMessage() {
            return entry.message();
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return super.isMouseOver(mouseX, mouseY) && (child == null || !child.menu().isMouseOver(mouseX, mouseY));
        }

        @Override
        public boolean isChildOpened() {
            return child != null && child.key == this && child.menu.isOpen();
        }

        private boolean handleKeyboardFocusOnClose() {
            if (parent == null) return false;

            ChildDetails child = parent.child;
            close();

            ComponentPath path = menuPath(parent, child == null
                    ? parent.nextFocusPath(new FocusNavigationEvent.InitialFocus())
                    : NavigationUtils.findPath(parent, child.key)
            );

            if (path != null) {
                path.applyFocus(true);
                return true;
            }

            return false;
        }

        private boolean handleKeyboardFocusOnOpen() {
            if (isChildOpened()) return false;

            List<? extends FZContextMenuEntry> children = entry.childEntries();
            if (children.isEmpty()) return false;

            openChild(this, children);
            if (child == null) return false;

            ComponentPath path = menuPath(child.menu, NavigationUtils.initialFocus(child.menu));
            if (path != null) {
                path.applyFocus(true);
                setFocused(true);
                return true;
            }
            return false;
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (entry.keyPressed(event, this)) {
                return true;
            }
            if (event.isLeft()) {
                return handleKeyboardFocusOnClose();
            }
            if (event.isRight() || event.isSelection()) {
                return handleKeyboardFocusOnOpen();
            }
            return false;
        }

        @Override
        public void setX(int x) {
            int previousX = getX();
            super.setX(x);
            if (previousX != getX()) entryBounds = super.getRectangle();
        }

        @Override
        public void setY(int y) {
            int previousY = getY();
            super.setY(y);
            if (previousY != getY()) entryBounds = super.getRectangle();
        }

        @Override
        public void setWidth(int width) {
            int previousWidth = getWidth();
            super.setWidth(width);
            if (previousWidth != getWidth()) entryBounds = super.getRectangle();
        }

        @Override
        public void setHeight(int height) {
            int previousHeight = getHeight();
            super.setHeight(height);
            if (previousHeight != getHeight()) entryBounds = super.getRectangle();
        }

        @Override
        public ScreenRectangle getRectangle() {
            return entryBounds;
        }
    }

    public final static class Builder {
        private final FZDialogContainer container;
        private HorizontalDirection preferredDirection = HorizontalDirection.RIGHT;
        private ScreenRectangle padding = DEFAULT_PADDING;
        private @Nullable RenderableRectangle background = DEFAULT_BACKGROUND;
        private FZContextMenuEntry.@Nullable Divider sectionDivider = FZContextMenuEntryImpl.DividerImpl.DEFAULT_SECTION;
        private FZContextMenuEntry.@Nullable Divider entryDivider = FZContextMenuEntryImpl.DividerImpl.DEFAULT_ENTRY;
        private int maxHeight = DEFAULT_MAX_HEIGHT;
        private int minWidth = DEFAULT_ENTRY_WIDTH;
        private int popoverOrder = DEFAULT_POPOVER_ORDER;
        private int rowSpacing = DEFAULT_SPACING;
        private @Nullable String componentId;
        private boolean focusOnOpen = true;

        private Builder(FZDialogContainer container) {
            this.container = container;
        }

        public Builder id(String id) {
            this.componentId = id;
            return this;
        }

        public Builder background(@Nullable RenderableRectangle background) {
            this.background = background;
            return this;
        }

        public Builder sectionDivider(FZContextMenuEntry.@Nullable Divider sectionDivider) {
            this.sectionDivider = sectionDivider;
            return this;
        }

        public Builder sectionDivider(RenderableRectangle sectionDivider) {
            Objects.requireNonNull(entryDivider, "sectionDivider cannot be null");
            this.sectionDivider = new FZContextMenuEntryImpl.DividerImpl(sectionDivider, false);
            return this;
        }

        public Builder entryDivider(FZContextMenuEntry.@Nullable Divider entryDivider) {
            this.entryDivider = entryDivider;
            return this;
        }

        public Builder entryDivider(RenderableRectangle entryDivider) {
            Objects.requireNonNull(entryDivider, "entryDivider cannot be null");
            this.sectionDivider = new FZContextMenuEntryImpl.DividerImpl(entryDivider, false);
            return this;
        }

        public Builder noEntryDivider() {
            this.entryDivider = null;
            return this;
        }

        public Builder rowSpacing(int spacing) {
            this.rowSpacing = spacing;
            return this;
        }

        public Builder preferredDirection(HorizontalDirection preferredDirection) {
            this.preferredDirection = preferredDirection;
            return this;
        }

        public Builder padding(int left, int top, int right, int bottom) {
            this.padding = ScreenRectangleUtils.insets(left, top, right, bottom);
            return this;
        }

        public Builder padding(int padding) {
            return padding(padding, padding, padding, padding);
        }

        public Builder maxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public Builder minWidth(int minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        public Builder popoverOrder(int popoverOrder) {
            this.popoverOrder = popoverOrder;
            return this;
        }

        public Builder focusOnOpen(boolean focusOnOpen) {
            this.focusOnOpen = focusOnOpen;
            return this;
        }

        public Builder focusOnOpen() {
            return focusOnOpen(true);
        }

        public FZContextMenu build() {
            FZContextMenu contextMenu = new FZContextMenu(
                    container,
                    preferredDirection,
                    background,
                    sectionDivider,
                    entryDivider,
                    padding,
                    rowSpacing,
                    maxHeight,
                    minWidth,
                    focusOnOpen,
                    null
            );
            contextMenu.componentId = componentId;
            contextMenu.popoverOrder = popoverOrder;
            return contextMenu;
        }

        public FZContextMenu buildAndOpen(double x, double y, List<? extends FZContextMenuEntry> entries) {
            FZContextMenu contextMenu = build();
            contextMenu.open(x, y, entries);
            return contextMenu;
        }
    }
}
