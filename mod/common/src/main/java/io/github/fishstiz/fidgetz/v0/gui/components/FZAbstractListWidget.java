package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableElement;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class FZAbstractListWidget<E extends FZAbstractListWidget.Entry> extends AbstractListWidget {
    protected static final int DEFAULT_SCROLL_RATE = 10;
    protected final List<E> children = new ArrayList<>();
    private @Nullable E hovered;
    private ScreenRectangle bounds;
    private int cachedContentHeight;

    protected FZAbstractListWidget(int x, int y, int width, int height, Component message, ScrollbarSettings scrollbarSettings) {
        super(Minecraft.getInstance(), x, y, width, height, message, scrollbarSettings);
        this.bounds = new ScreenRectangle(getX(), getY(), getWidth(), getHeight());
    }

    protected FZAbstractListWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.bounds = new ScreenRectangle(getX(), getY(), getWidth(), getHeight());
    }

    protected FZAbstractListWidget(Component message, ScrollbarSettings scrollbarSettings) {
        this(0, 0, 0, 0, message, scrollbarSettings);
    }

    protected FZAbstractListWidget(Component message) {
        this(message, AbstractScrollArea.defaultSettings(DEFAULT_SCROLL_RATE));
    }

    protected FZAbstractListWidget() {
        this(CommonComponents.EMPTY);
    }

    protected void setHovered(@Nullable E hovered) {
        if (this.hovered != hovered) {
            if (this.hovered != null) {
                this.hovered.setHovered(false);
            }
            if (hovered != null) {
                hovered.setHovered(true);
            }
            this.hovered = hovered;
        }
    }

    protected @Nullable E getHovered() {
        return this.hovered;
    }

    protected void extractFocusedRenderState(GuiGraphicsExtractor graphics, E focused) {
        graphics.outline(focused.getX(), focused.getY(), focused.getWidth(), focused.getHeight(), CommonColors.WHITE);
    }

    @Override
    protected void extractEntriesRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ScreenRectangle bounds = getRectangle();
        for (E entry : children) {
            if (entry.getRectangle().overlaps(bounds)) {
                if (entry.isMouseOver(mouseX, mouseY)) {
                    setHovered(entry);
                }

                entry.extractRenderState(graphics, mouseX, mouseY, partialTick);

                if (entry.isFocused()) {
                    extractFocusedRenderState(graphics, entry);
                }
            }
        }
    }

    @Override
    protected int contentHeight() {
        return cachedContentHeight;
    }

    protected int rowSpacing() {
        return 0;
    }

    protected void repositionEntries() {
        int childX = contentLeft();
        int childY = getY() - (int) scrollAmount();
        int childWidth = contentWidth();
        int totalheight = 0;

        for (int i = 0; i < children.size(); i++) {
            E child = children.get(i);
            child.index = i;

            int height = child.getHeight();
            int marginTop = child.getMarginTop();
            int marginBottom = child.getMarginBottom();

            child.setBounds(childX, childY + marginTop, childWidth, height);
            int occupiedHeight = height + marginTop + marginBottom + (i + 1 < children.size() ? rowSpacing() : 0);

            childY += occupiedHeight;
            totalheight += occupiedHeight;
        }

        this.cachedContentHeight = totalheight;
    }

    @Override
    public void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        repositionEntries();
    }

    private void updateBounds() {
        this.bounds = new ScreenRectangle(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        repositionEntries();
        updateBounds();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        repositionEntries();
        updateBounds();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        int childWidth = contentWidth();
        for (E child : children) {
            child.setWidth(childWidth);
        }
        updateBounds();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        setScrollAmount(scrollAmount());
        updateBounds();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        setScrollAmount(scrollAmount());
        updateBounds();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return bounds;
    }

    @Override
    public List<E> children() {
        return children;
    }

    protected abstract static class Entry extends AbstractContainerEventHandler implements
            LayoutElement,
            Renderable,
            NarratableEntry,
            FZHoverableContainer {
        private ScreenRectangle bounds;
        int index;
        private boolean focused;
        private boolean hovered;
        private TriState fidgetz$hovered = TriState.DEFAULT;
        private @Nullable GuiEventListener fidgetz$hoveredElement;

        protected Entry(int height) {
            this.bounds = new ScreenRectangle(0, 0, 0, height);
        }

        protected Entry() {
            this.bounds = ScreenRectangle.empty();
        }

        protected int getIndex() {
            return index;
        }

        @Override
        public void setX(int x) {
            this.bounds = ScreenRectangleUtils.withLeft(bounds, x);
        }

        @Override
        public void setY(int y) {
            this.bounds = ScreenRectangleUtils.withTop(bounds, y);
        }

        protected void setWidth(int width) {
            this.bounds = ScreenRectangleUtils.withWidth(bounds, width);
        }

        protected void setHeight(int height) {
            this.bounds = ScreenRectangleUtils.withHeight(bounds, height);
        }

        protected void setBounds(int x, int y, int width, int height) {
            this.bounds = new ScreenRectangle(x, y, width, height);
        }

        @Override
        public final int getX() {
            return bounds.left();
        }

        @Override
        public final int getY() {
            return bounds.top();
        }

        @Override
        public final int getWidth() {
            return bounds.width();
        }

        @Override
        public final int getHeight() {
            return bounds.height();
        }

        protected int getMarginTop() {
            return 0;
        }

        protected int getMarginBottom() {
            return 0;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
            if (!focused) {
                setFocused(null);
            }
        }

        @Override
        public boolean isFocused() {
            return focused;
        }

        void setHovered(boolean hovered) {
            this.hovered = hovered;
        }

        public boolean isHovered() {
            return hovered && fidgetz$hovered != TriState.FALSE;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return getRectangle().containsPoint((int) mouseX, (int) mouseY);
        }

        @Override
        public ScreenRectangle getRectangle() {
            return bounds;
        }

        @Override
        public NarrationPriority narrationPriority() {
            if (isFocused()) {
                return NarrationPriority.FOCUSED;
            } else if (isHovered()) {
                return NarrationPriority.HOVERED;
            }
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> widgetVisitor) {
        }

        @Override
        @ApiStatus.Internal
        public final boolean fidgetz$isHovered() {
            return fidgetz$hovered == TriState.TRUE || fidgetz$getHovered() != null;
        }

        @Override
        @ApiStatus.Internal
        public final void fidgetz$setHovered(boolean hovered) {
            this.fidgetz$hovered = TriState.from(hovered);
            if (!hovered) {
                fidgetz$setHovered(null);
            }
        }

        @Override
        @ApiStatus.Internal
        public final void fidgetz$setHovered(@Nullable GuiEventListener hovered) {
            GuiEventListener previous = fidgetz$getHovered();
            if (previous != hovered) {
                if (previous instanceof FZHoverableElement previousElement) {
                    previousElement.fidgetz$setHovered(false);
                }
                if (hovered instanceof FZHoverableElement hoverableElement) {
                    hoverableElement.fidgetz$setHovered(true);
                }
                fidgetz$hoveredElement = hovered;
            }
        }

        @Override
        @ApiStatus.Internal
        public final @Nullable GuiEventListener fidgetz$getHovered() {
            return fidgetz$hoveredElement;
        }

        @Override
        @ApiStatus.Internal
        public final boolean fidgetz$updateHovered(double mouseX, double mouseY) {
            fidgetz$setHovered(isMouseOver(mouseX, mouseY));
            boolean hovered = fidgetz$isHovered();
            if (hovered) {
                for (GuiEventListener child : children()) {
                    if (((FZHoverableElement) child).fidgetz$updateHovered(mouseX, mouseY)) {
                        fidgetz$setHovered(child);
                        return true;
                    }
                }
            }
            return hovered;
        }
    }
}
