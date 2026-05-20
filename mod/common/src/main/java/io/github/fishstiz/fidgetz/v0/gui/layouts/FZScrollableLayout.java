package io.github.fishstiz.fidgetz.v0.gui.layouts;

import io.github.fishstiz.fidgetz.v0.gui.components.events.ScrollableContainer;
import io.github.fishstiz.fidgetz.v0.utils.MathUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FZScrollableLayout extends ComposedLayout {
    private static final int DEFAULT_SCROLL_RATE = 10;
    private static final int DEFAULT_SCROLLBAR_SPACING = 4;
    private final Supplier<ScreenRectangle> screenArea;
    private final FZScrollableLayout.Container container;
    private boolean reserveScrollbarArea;
    private int scrollbarSpacing = DEFAULT_SCROLLBAR_SPACING;
    private int minWidth;
    private int minHeight;
    private int maxHeight;

    FZScrollableLayout(Supplier<ScreenRectangle> screenArea, Layout content, AbstractScrollArea.ScrollbarSettings scrollbarSettings) {
        super(content);
        this.screenArea = screenArea;
        this.container = new FZScrollableLayout.Container(0, 0, scrollbarSettings);
    }

    FZScrollableLayout(Supplier<ScreenRectangle> screenArea, Layout content) {
        this(screenArea, content, AbstractScrollArea.defaultSettings(DEFAULT_SCROLL_RATE));
    }

    public FZScrollableLayout minWidth(int minWidth) {
        this.minWidth = Math.min(minWidth, screenArea.get().width());
        container.setWidth(Math.max(layout.getWidth(), minWidth));
        return this;
    }

    public FZScrollableLayout minHeight(int minHeight) {
        this.minHeight = Math.min(minHeight, screenArea.get().height());
        container.setHeight(Math.max(layout.getHeight(), minHeight));
        return this;
    }

    public FZScrollableLayout maxHeight(int maxHeight) {
        this.maxHeight = Math.min(maxHeight, screenArea.get().height());
        container.setHeight(Math.clamp(container.getHeight(), minHeight, maxHeight));
        return this;
    }

    public FZScrollableLayout scrollbarSpacing(int scrollbarSpacing) {
        this.scrollbarSpacing = scrollbarSpacing;
        return this;
    }

    public FZScrollableLayout reserveScrollbarArea(boolean reserveScrollbarArea) {
        this.reserveScrollbarArea = reserveScrollbarArea;
        return this;
    }

    public FZScrollableLayout reserveScrollbarArea() {
        return reserveScrollbarArea(true);
    }

    private int reservedWidth() {
        return reserveScrollbarArea || container.scrollbarVisible() ? container.scrollbarReserve() : 0;
    }

    @Override
    public void arrangeElements() {
        layout.arrangeElements();
        container.children.clear();
        layout.visitWidgets(container.children::add);
        int contentWidth = layout.getWidth();
        ScreenRectangle containerBounds = screenArea.get();
        container.setWidth(Math.min(containerBounds.width(), Math.max(contentWidth, minWidth) + reservedWidth()));
        container.setHeight(Math.min(containerBounds.height(), MathUtils.clampOptionalMax(layout.getHeight(), minHeight, maxHeight)));
        container.refreshScrollAmount();
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> layoutElementVisitor) {
        layoutElementVisitor.accept(container);
    }

    @Override
    public void setX(int x) {
        container.setX(x);
    }

    @Override
    public void setY(int y) {
        container.setY(y);
    }

    @Override
    public int getX() {
        return container.getX();
    }

    @Override
    public int getY() {
        return container.getY();
    }

    @Override
    public int getWidth() {
        return container.getWidth();
    }

    @Override
    public int getHeight() {
        return container.getHeight();
    }

    @Override
    public void fidgetz$setWidth(int width) {
        super.fidgetz$setWidth(Math.max(0, width - reservedWidth()));
        arrangeElements();
    }

    @Override
    public void fidgetz$setHeight(int height) {
        minHeight(height);
        maxHeight(height);
        arrangeElements();
    }

    @Override
    public void fidgetz$setSize(int width, int height) {
        super.fidgetz$setWidth(Math.max(0, width - reservedWidth()));
        minHeight(height);
        maxHeight(height);
        arrangeElements();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return container.getRectangle();
    }

    private final class Container extends AbstractContainerWidget implements ScrollableContainer {
        private final List<AbstractWidget> children = new ArrayList<>();
        private ScreenRectangle bounds;

        public Container(int width, int height, AbstractScrollArea.ScrollbarSettings scrollbarSettings) {
            super(0, 0, width, height, CommonComponents.EMPTY, scrollbarSettings);
            bounds = super.getRectangle();
        }

        @Override
        protected int contentHeight() {
            return layout.getHeight();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            graphics.enableScissor(getX(), getY(), getRight(), getBottom());

            for (AbstractWidget child : children) {
                child.extractRenderState(graphics, mouseX, mouseY, a);
            }

            graphics.disableScissor();
            extractScrollbar(graphics, mouseX, mouseY);
        }



        @Override
        public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
            if (isActive() && getChildAt(mx, my).filter(child -> child.mouseScrolled(mx, my, scrollX, scrollY)).isPresent()) {
                return true;
            }
            return super.mouseScrolled(mx, my, scrollX, scrollY) && scrollable();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
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
        public void setFocused(boolean focused) {
            super.setFocused(focused);
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {
            if (focused != getFocused()) {
                super.setFocused(focused);
            }
        }

        @Override
        public void setWidth(int width) {
            super.setWidth(width);
            bounds = super.getRectangle();
        }

        @Override
        public void setHeight(int height) {
            super.setHeight(height);
            bounds = super.getRectangle();
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);
            bounds = super.getRectangle();
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            layout.setX(x);
            bounds = super.getRectangle();
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            layout.setY(y - (int) scrollAmount());
            bounds = super.getRectangle();
        }

        @Override
        protected boolean isOverScrollbar(double x, double y) {
            return super.isOverScrollbar(x, y) && isHovered();
        }

        private int scrollbarReserve() {
            return scrollbarSpacing + scrollbarWidth();
        }

        private boolean scrollbarVisible() {
            return contentHeight() > MathUtils.optionalMin(screenArea.get().height(), maxHeight);
        }

        @Override
        public double scrollRate() {
            return super.scrollRate();
        }

        @Override
        public void setScrollAmount(double scrollAmount) {
            super.setScrollAmount(scrollAmount);
            FZScrollableLayout.this.layout.setY(getRectangle().top() - (int) scrollAmount());
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }

        @Override
        public Collection<? extends NarratableEntry> getNarratables() {
            return children;
        }

        @Override
        public ScreenRectangle getRectangle() {
            return bounds;
        }
    }
}
