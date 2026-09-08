package io.github.fishstiz.fidgetz.v0.gui.layouts;

import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.components.events.ScrollableContainer;
import io.github.fishstiz.fidgetz.v0.utils.MathUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    private int maxWidth;
    private int minHeight;
    private int maxHeight;

    FZScrollableLayout(Supplier<ScreenRectangle> screenArea, Layout content) {
        super(content);
        this.screenArea = screenArea;
        this.container = new FZScrollableLayout.Container(0, 0);
        this.container.setScrollRate(DEFAULT_SCROLL_RATE);
    }

    public static FZScrollableLayout from(Supplier<ScreenRectangle> maxScrollArea, Layout content) {
        return new FZScrollableLayout(maxScrollArea, content);
    }


    public static FZScrollableLayout from(LayoutElement container, Layout content) {
        return from(container::getRectangle, content);
    }

    public static FZScrollableLayout from(GuiEventListener container, Layout content) {
        return from(container::getRectangle, content);
    }

    public static FZScrollableLayout from(Layout content) {
        return from(ScreenRectangle::empty, content);
    }

    public static FZScrollableLayout from(int maxHeight, Layout content) {
        return from(content).maxHeight(maxHeight);
    }

    public FZScrollableLayout minWidth(int minWidth) {
        this.minWidth = MathUtils.clampOptionalMax(minWidth, 0, screenArea.get().width());
        container.setWidth(Math.max(composed.getWidth(), minWidth));
        return this;
    }

    public FZScrollableLayout maxWidth(int maxWidth) {
        this.maxWidth = MathUtils.clampOptionalMax(maxWidth, 0, screenArea.get().width());
        this.minWidth = MathUtils.optionalMin(this.minWidth, this.maxWidth);
        if (container.getWidth() > this.getWidth()) {
            fidgetz$setWidth(this.maxWidth);
            container.setWidth(this.maxWidth);
        }
        return this;
    }

    public FZScrollableLayout minHeight(int minHeight) {
        this.minHeight = MathUtils.clampOptionalMax(minHeight, 0, screenArea.get().height());
        container.setHeight(Math.max(composed.getHeight(), minHeight));
        return this;
    }

    public FZScrollableLayout maxHeight(int maxHeight) {
        this.maxHeight = MathUtils.clampOptionalMax(maxHeight, 0, screenArea.get().height());
        container.setHeight(MathUtils.clampOptionalMax(container.getHeight(), minHeight, this.maxHeight));
        return this;
    }

    public FZScrollableLayout scrollRate(double scrollRate) {
        container.setScrollRate(scrollRate);
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
        composed.arrangeElements();
        container.clearChildren();
        composed.visitWidgets(container::addChild);
        int contentWidth = composed.getWidth();
        ScreenRectangle containerBounds = screenArea.get();
        container.setWidth(MathUtils.clampOptionalMax(Math.max(contentWidth, minWidth) + reservedWidth(), 0, containerBounds.width()));
        container.setHeight(MathUtils.optionalMin(MathUtils.clampOptionalMax(composed.getHeight(), minHeight, maxHeight), containerBounds.height()));
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

    private final class Container extends AbstractListWidget<Container.Entry> implements ScrollableContainer {
        private ScreenRectangle bounds;

        Container(int width, int height) {
            super(0, 0, width, height, CommonComponents.EMPTY);
            bounds = super.getRectangle();
        }

        private void addChild(AbstractWidget child) {
            addEntry(new Entry(child));
        }

        private void clearChildren() {
            clearEntries();
        }

        @Override
        protected void extractBackgroundRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        }

        @Override
        protected void extractSeparatorsRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        }

        @Override
        protected void extractEntriesRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            for (Entry child : children()) {
                child.widget.render(graphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        protected int contentHeight() {
            return composed.getHeight();
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
            composed.setX(x);
            bounds = super.getRectangle();
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            composed.setY(y - (int) scrollAmount());
            bounds = super.getRectangle();
        }

        @Override
        protected boolean isOverScrollbar(double x, double y) {
            return super.isOverScrollbar(x, y) && isHovered();
        }

        @Override
        protected int scrollbarReserve() {
            return scrollbarSpacing + scrollbarWidth();
        }

        @Override
        protected boolean scrollbarVisible() {
            return contentHeight() > MathUtils.optionalMin(screenArea.get().height(), maxHeight);
        }

        @Override
        public void setScrollAmount(double scrollAmount) {
            super.setScrollAmount(scrollAmount);
            FZScrollableLayout.this.composed.setY(getRectangle().top() - (int) scrollAmount());
        }

        @Override
        public ScreenRectangle getRectangle() {
            return bounds;
        }

        static final class Entry extends AbstractListWidget.Entry<Entry> implements FZComponent {
            private final AbstractWidget widget;
            private final List<AbstractWidget> children;

            private Entry(AbstractWidget widget) {
                this.widget = widget;
                this.children = List.of(widget);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return children;
            }

            @Override
            public void setFocused(boolean focused) {
                widget.setFocused(focused);
            }

            @Override
            public boolean isFocused() {
                return widget.isFocused();
            }

            @Override
            public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
                return ComponentPath.path(this, widget.nextFocusPath(navigationEvent));
            }

            @Override
            public ScreenRectangle getRectangle() {
                return widget.getRectangle();
            }

            @Override
            public void mouseMoved(double mouseX, double mouseY) {
                widget.mouseMoved(mouseX, mouseY);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                return widget.mouseReleased(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
                return widget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }

            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
                return widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                return widget.keyPressed(keyCode, scanCode, modifiers);
            }

            @Override
            public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
                return widget.keyReleased(keyCode, scanCode, modifiers);
            }

            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return widget.charTyped(codePoint, modifiers);
            }

            @Override
            public @Nullable GuiEventListener getFocused() {
                return widget.isFocused() ? widget : null;
            }

            @Override
            public void setFocused(@Nullable GuiEventListener focused) {
                if (focused == null) {
                    widget.setFocused(false);
                } else if (focused == widget) {
                    widget.setFocused(true);
                }
            }

            @Override
            public boolean isMouseOver(double mouseX, double mouseY) {
                return widget.isMouseOver(mouseX, mouseY);
            }

            @Override
            public @Nullable ComponentPath getCurrentFocusPath() {
                return ComponentPath.path(this, widget.getCurrentFocusPath());
            }

            @Override
            public int getTabOrderGroup() {
                return widget.getTabOrderGroup();
            }

            @Override
            public boolean fidgetz$shouldTakeFocusAfterInteraction() {
                return widget instanceof FZComponent component
                        ? component.fidgetz$shouldTakeFocusAfterInteraction()
                        : FZComponent.super.fidgetz$shouldTakeFocusAfterInteraction();
            }
        }
    }
}
