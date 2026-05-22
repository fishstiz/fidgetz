package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.components.events.ScrollableContainer;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

abstract class AbstractListWidget extends AbstractContainerWidget implements ScrollableContainer {
    protected static final int DEFAULT_MAX_CONTENT_WIDTH = 270;
    protected static final int DEFAULT_SCROLL_RATE = 10;
    protected static final int SEPARATOR_HEIGHT = 2;
    private static final RenderableRectangle BACKGROUND = Renderables.texture(Identifier.withDefaultNamespace("textures/gui/menu_list_background.png"), 32, 32);
    private static final RenderableRectangle INWORLD_BACKGROUND = Renderables.texture(Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png"), 32, 32);
    private static final RenderableRectangle HEADER_SEPARATOR = Renderables.texture(Screen.HEADER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private static final RenderableRectangle INWORLD_HEADER_SEPARATOR = Renderables.texture(Screen.INWORLD_HEADER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private static final RenderableRectangle FOOTER_SEPARATOR = Renderables.texture(Screen.FOOTER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private static final RenderableRectangle INWORLD_FOOTER_SEPARATOR = Renderables.texture(Screen.INWORLD_FOOTER_SEPARATOR, 32, SEPARATOR_HEIGHT, 32, SEPARATOR_HEIGHT);
    private final Minecraft minecraft;

    protected AbstractListWidget(Minecraft minecraft, int x, int y, int width, int height, Component message, ScrollbarSettings scrollbarSettings) {
        super(x, y, width, height, message, scrollbarSettings);
        this.minecraft = minecraft;
    }

    protected AbstractListWidget(Minecraft minecraft, int x, int y, int width, int height, Component message) {
        this(minecraft, x, y, width, height, message, AbstractScrollArea.defaultSettings(DEFAULT_SCROLL_RATE));
    }

    protected AbstractListWidget(int x, int y, int width, int height, Component message, ScrollbarSettings scrollbarSettings) {
        this(Minecraft.getInstance(), x, y, width, height, message, scrollbarSettings);
    }

    protected AbstractListWidget(int x, int y, int width, int height, Component message) {
        this(Minecraft.getInstance(), x, y, width, height, message);
    }

    protected void extractBackgroundRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        RenderableRectangle background = minecraft.level == null ? BACKGROUND : INWORLD_BACKGROUND;
        background.extractRenderState(graphics, getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, partialTick);
    }

    protected void extractSeparatorsRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        RenderableRectangle header = minecraft.level == null ? HEADER_SEPARATOR : INWORLD_HEADER_SEPARATOR;
        RenderableRectangle footer = minecraft.level == null ? FOOTER_SEPARATOR : INWORLD_FOOTER_SEPARATOR;
        header.extractRenderState(graphics, getX(), getY() - SEPARATOR_HEIGHT, getWidth(), SEPARATOR_HEIGHT, mouseX, mouseY, partialTick);
        footer.extractRenderState(graphics, getX(), getBottom(), getWidth(), SEPARATOR_HEIGHT, mouseX, mouseY, partialTick);
    }

    protected abstract void extractEntriesRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick);

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractBackgroundRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.enableScissor(getX(), getY(), getRight(), getBottom());
        extractEntriesRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.disableScissor();
        extractSeparatorsRenderState(graphics, mouseX, mouseY, partialTick);
        extractScrollbar(graphics, mouseX, mouseY);
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
    protected abstract int contentHeight();

    protected final int contentLeft() {
        return Math.min(
                getX() + (getWidth() / 2 - contentWidth() / 2) + contentPaddingLeft() - contentPaddingRight(),
                getRight() - scrollbarReserve() - contentWidth()
        );
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

    @Override
    protected int scrollBarX() {
        return Math.min(contentLeft() + contentWidth() + contentPaddingRight(), getRight() - scrollbarWidth());
    }

    @Override
    public double scrollRate() {
        return super.scrollRate();
    }

    @Override
    protected boolean isOverScrollbar(double x, double y) {
        return super.isOverScrollbar(x, y) && isHovered();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
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
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        return addScrollEffectOnFocus(navigationEvent, super.nextFocusPath(navigationEvent));
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
}
