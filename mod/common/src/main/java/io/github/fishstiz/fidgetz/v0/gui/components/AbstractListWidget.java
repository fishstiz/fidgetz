package io.github.fishstiz.fidgetz.v0.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.fishstiz.fidgetz.v0.gui.components.events.ScrollableContainer;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.inject.mixins.access.AbstractSelectionListAccess;
import io.github.fishstiz.fidgetz.v0.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

// extend AbstractSelectionList for smooth scrolling compat even though we deprecate a lot of methods and would prefer
// AbstractScrollWidget or custom impl. Avoid on 1.21.1 if possible
public abstract class AbstractListWidget<T extends AbstractListWidget.Entry<T>> extends AbstractSelectionList<T> implements ScrollableContainer, ContainerEventHandlerPatch {
    protected static final int DEFAULT_MAX_CONTENT_WIDTH = 270;
    protected static final int DEFAULT_SCROLL_RATE = 10;
    protected static final int SEPARATOR_HEIGHT = 2;
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final ResourceLocation INWORLD_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    private static final RenderableRectangle HEADER_SEPARATOR = Renderables.texture(Screen.HEADER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private static final RenderableRectangle INWORLD_HEADER_SEPARATOR = Renderables.texture(Screen.INWORLD_HEADER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private static final RenderableRectangle FOOTER_SEPARATOR = Renderables.texture(Screen.FOOTER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private static final RenderableRectangle INWORLD_FOOTER_SEPARATOR = Renderables.texture(Screen.INWORLD_FOOTER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);

    protected AbstractListWidget(Minecraft minecraft, int x, int y, int width, int height, Component message) {
        super(minecraft, width, height, y, DEFAULT_SCROLL_RATE * 2);
        setMessage(message);
    }

    protected AbstractListWidget(int x, int y, int width, int height, Component message) {
        this(Minecraft.getInstance(), x, y, width, height, message);
    }

    @Override
    @Deprecated
    protected final void renderListBackground(GuiGraphics graphics) {
        extractBackgroundRenderState(graphics, 0, 0, 0);
    }

    protected void extractBackgroundRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation background = minecraft.level == null ? BACKGROUND : INWORLD_BACKGROUND;
        RenderSystem.enableBlend();
        graphics.blit(
                background,
                getX(),
                getY(),
                getRight(),
                getBottom() + (int) scrollAmount(),
                getWidth(),
                getHeight(),
                32,
                32
        );
        RenderSystem.disableBlend();
    }

    @Override
    @Deprecated
    protected final void renderListSeparators(GuiGraphics graphics) {
        extractSeparatorsRenderState(graphics, 0, 0, 0);
    }

    protected void extractSeparatorsRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderableRectangle header = minecraft.level == null ? HEADER_SEPARATOR : INWORLD_HEADER_SEPARATOR;
        RenderableRectangle footer = minecraft.level == null ? FOOTER_SEPARATOR : INWORLD_FOOTER_SEPARATOR;
        RenderSystem.enableBlend();
        header.extractRenderState(graphics, getX(), getY() - SEPARATOR_HEIGHT, getWidth(), SEPARATOR_HEIGHT, mouseX, mouseY, partialTick);
        footer.extractRenderState(graphics, getX(), getBottom(), getWidth(), SEPARATOR_HEIGHT, mouseX, mouseY, partialTick);
        RenderSystem.disableBlend();
    }

    @Override
    @Deprecated
    protected final void renderListItems(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        extractEntriesRenderState(graphics, mouseX, mouseY, partialTick);
    }

    protected abstract void extractEntriesRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    @Override
    @Deprecated
    protected final void renderDecorations(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    // workaround for smooth scrolling which directly modifies scrollAmount instead of calling the setter.
    // it assumes that calls to setScrollAmount should snap
    // https://github.com/SmajloSlovakian/Minecraft-Smooth-Scrolling/blob/1.21.1/src/main/java/smsk/smoothscroll/mixin/EntryListWidgetMixin.java
    void onChangeScrollAmount(double scrollAmount) {
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        double lastScrollAmount = scrollAmount();
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        double newScrollAmount = scrollAmount();
        if (lastScrollAmount != newScrollAmount) {
            onChangeScrollAmount(newScrollAmount);
        }
    }

    protected int contentPaddingLeft() {
        return 0;
    }

    protected int contentPaddingRight() {
        return 0;
    }

    protected int maxContentWidth() {
        return DEFAULT_MAX_CONTENT_WIDTH;
    }

    protected int contentWidth() {
        int contentWidth = getWidth() - scrollbarReserve() - contentPaddingLeft() - contentPaddingRight();
        return MathUtils.optionalMin(contentWidth, maxContentWidth());
    }

    @Override
    @Deprecated
    public final int getRowWidth() {
        return contentWidth();
    }

    protected abstract int contentHeight();

    @Override
    @Deprecated
    protected final int getMaxPosition() {
        return contentHeight();
    }

    protected final int contentLeft() {
        return Math.min(
                getX() + (getWidth() / 2 - contentWidth() / 2) + contentPaddingLeft() - contentPaddingRight(),
                getRight() - scrollbarReserve() - contentWidth()
        );
    }

    @Override
    @Deprecated
    public final int getRowLeft() {
        return contentLeft();
    }

    @Override
    @Deprecated
    public final int getRowRight() {
        return contentLeft() + contentWidth();
    }

    @Override
    @Deprecated
    protected final int getRowTop(int index) {
        Entry<T> entry = index >= 0 && index < children().size() ? children().get(index) : null;
        return entry == null ? 0 : entry.getRectangle().top();
    }

    @Override
    @Deprecated
    protected final int getRowBottom(int index) {
        Entry<T> entry = index >= 0 && index < children().size() ? children().get(index) : null;
        return entry == null ? 0 : entry.getRectangle().bottom();
    }

    protected final int scrollbarWidth() {
        return SCROLLBAR_WIDTH;
    }

    protected boolean scrollbarVisible() {
        return contentHeight() > getHeight();
    }

    protected boolean reserveScrollbarWidth() {
        return false;
    }

    protected int scrollbarReserve() {
        return reserveScrollbarWidth() || scrollbarVisible() ? scrollbarWidth() : 0;
    }

    protected int scrollBarX() {
        return Math.min(contentLeft() + contentWidth() + contentPaddingRight(), getRight() - scrollbarWidth());
    }

    @Override
    @Deprecated
    protected final int getScrollbarPosition() {
        return scrollBarX();
    }

    @Override
    @Deprecated
    public final double getScrollAmount() {
        return super.getScrollAmount();
    }

    @Override
    public double scrollAmount() {
        return super.getScrollAmount();
    }

    public void refreshScrollAmount() {
        setScrollAmount(scrollAmount());
    }

    public void setScrollRate(double scrollRate) {
        ((AbstractSelectionListAccess) this).fidgetz$setScrollRate(scrollRate);
    }

    @Override
    public final double scrollRate() {
        return (double) itemHeight / 2;
    }

    protected boolean isOverScrollbar(double x, double y) {
        return isHovered() && x >= scrollBarX() && x <= scrollBarX() + scrollbarWidth() && y >= getY() && y < getBottom();
    }

    protected boolean scrollable() {
        return maxScrollAmount() > 0;
    }

    public int maxScrollAmount() {
        return Math.max(0, this.contentHeight() - this.height);
    }

    @Override
    @Deprecated
    public final int getMaxScroll() {
        return maxScrollAmount();
    }

    protected int scrollerHeight() {
        return Mth.clamp((int) ((float) (this.height * this.height) / this.contentHeight()), 32, this.height - 8);
    }

    @Override
    @Deprecated
    protected final void ensureVisible(T entry) {
    }

    public boolean updateScrolling(double mouseX, double mouseY, int button) {
        updateScrollingState(mouseX, mouseY, button);
        return ((AbstractSelectionListAccess) this).fidgetz$getScrolling();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            setFocused(null);
        }
    }

    @Override
    @Deprecated
    public final @Nullable T getSelected() {
        return null;
    }

    @Override
    @Deprecated
    public final void setSelected(@Nullable T selected) {
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (getFocused() != focused) {
            super.setFocused(focused);
        }
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        return addScrollEffectOnFocus(navigationEvent, super.nextFocusPath(navigationEvent));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (((AbstractSelectionListAccess) this).fidgetz$getScrolling()) {
            if (mouseY < this.getY()) {
                this.setScrollAmount(0.0);
            } else if (mouseY > this.getBottom()) {
                this.setScrollAmount(this.maxScrollAmount());
            } else {
                double max = Math.max(1, this.maxScrollAmount());
                int barHeight = this.scrollerHeight();
                double yDragScale = Math.max(1.0, max / (this.height - barHeight));
                this.setScrollAmount(this.scrollAmount() + dragY * yDragScale);
            }

            return true;
        } else if (this.isValidClickButton(button)) {
            this.onDrag(mouseX, mouseY, dragX, dragY);
        }

        return ContainerEventHandlerPatch.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (isActive() && getChildAt(mx, my).filter(child -> child.mouseScrolled(mx, my, scrollX, scrollY)).isPresent()) {
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY) && scrollable();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean scrolling = updateScrolling(mouseX, mouseY, button);
        return ContainerEventHandlerPatch.super.mouseClicked(mouseX, mouseY, button) || scrolling;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        updateScrollingState(0, 0, -1); // reset scrolling state
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    protected abstract static class Entry<T extends Entry<T>> extends AbstractSelectionList.Entry<T> implements ContainerEventHandlerPatch {
        @Nullable
        private GuiEventListener focused;
        private boolean isDragging;

        @Override
        public boolean isDragging() {
            return this.isDragging;
        }

        @Override
        public void setDragging(boolean dragging) {
            this.isDragging = dragging;
        }

        @Override
        public @Nullable GuiEventListener getFocused() {
            return this.focused;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener listener) {
            if (getFocused() != listener) {
                if (this.focused != null) {
                    this.focused.setFocused(false);
                }
                if (listener != null) {
                    listener.setFocused(true);
                }
                this.focused = listener;
            }
        }

        @Override
        public void setFocused(boolean focused) {
            if (!focused) {
                setFocused(null);
            }
        }

        @Override
        @Deprecated
        public final void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return ContainerEventHandlerPatch.super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return ContainerEventHandlerPatch.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return ContainerEventHandlerPatch.super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return getRectangle().containsPoint((int) mouseX, (int) mouseY);
        }
    }
}
