package io.github.fishstiz.fidgetz.v0.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

// backported from 26.1
public abstract class FZAbstractScrollArea extends AbstractWidget {
    public static final int SCROLLBAR_WIDTH = 6;
    private static final int SCROLLBAR_MIN_HEIGHT = 32;
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller_background");
    private final FZAbstractScrollArea.ScrollbarSettings scrollbarSettings;
    private double scrollAmount;
    private boolean scrolling;

    public FZAbstractScrollArea(int x, int y, int width, int height, Component message, ScrollbarSettings scrollbarSettings) {
        super(x, y, width, height, message);
        this.scrollbarSettings = scrollbarSettings;
    }

    @Override
    public boolean mouseScrolled(final double mx, final double my, final double scrollX, final double scrollY) {
        if (!this.visible) {
            return false;
        } else {
            this.setScrollAmount(this.scrollAmount() - scrollY * this.scrollRate());
            return true;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (this.scrolling) {
            if (mouseY < this.getY()) {
                this.setScrollAmount(0.0);
            } else if (mouseY > this.getBottom()) {
                this.setScrollAmount(this.maxScrollAmount());
            } else {
                double max = Math.max(1, this.maxScrollAmount());
                int barHeight = this.scrollerHeight();
                double yDragScale = Math.max(1.0, max / (this.height - barHeight));
                this.setScrollAmount(this.scrollAmount() + dy * yDragScale);
            }

            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dx, dy);
        }
    }

    public void onRelease(double mouseX, double mouseY) {
        this.scrolling = false;
    }

    public double scrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(final double scrollAmount) {
        this.scrollAmount = Mth.clamp(scrollAmount, 0.0, this.maxScrollAmount());
    }

    public boolean updateScrolling(double mouseX, double mouseY, int button) {
        this.scrolling = this.scrollable() && this.isValidClickButton(button) && this.isOverScrollbar(mouseX, mouseY);
        return this.scrolling;
    }

    protected boolean isOverScrollbar(final double x, final double y) {
        return x >= this.scrollBarX() && x <= this.scrollBarX() + this.scrollbarWidth() && y >= this.getY() && y < this.getBottom();
    }

    public void refreshScrollAmount() {
        this.setScrollAmount(this.scrollAmount);
    }

    public int maxScrollAmount() {
        return Math.max(0, this.contentHeight() - this.height);
    }

    protected boolean scrollable() {
        return this.maxScrollAmount() > 0;
    }

    public int scrollbarWidth() {
        return this.scrollbarSettings.scrollbarWidth();
    }

    protected int scrollerHeight() {
        return Mth.clamp((int) ((float) (this.height * this.height) / this.contentHeight()), 32, this.height - 8);
    }

    protected int scrollBarX() {
        return this.getRight() - this.scrollbarWidth();
    }

    public int scrollBarY() {
        return this.maxScrollAmount() == 0
                ? this.getY()
                : Math.max(this.getY(), (int) this.scrollAmount * (this.height - this.scrollerHeight()) / this.maxScrollAmount() + this.getY());
    }

    protected void extractScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
        int scrollbarX = this.scrollBarX();
        int scrollerHeight = this.scrollerHeight();
        int scrollerY = this.scrollBarY();
        if (!this.scrollable() && this.scrollbarSettings.disabledScrollerSprite() != null) {
            graphics.blitSprite(this.scrollbarSettings.backgroundSprite(), scrollbarX, this.getY(), this.scrollbarWidth(), this.getHeight());
            graphics.blitSprite(this.scrollbarSettings.disabledScrollerSprite(), scrollbarX, this.getY(), this.scrollbarWidth(), scrollerHeight);
        }

        if (this.scrollable()) {
            graphics.blitSprite(this.scrollbarSettings.backgroundSprite(), scrollbarX, this.getY(), this.scrollbarWidth(), this.getHeight());
            graphics.blitSprite(this.scrollbarSettings.scrollerSprite(), scrollbarX, scrollerY, this.scrollbarWidth(), scrollerHeight);
        }
    }

    protected abstract int contentHeight();

    protected double scrollRate() {
        return this.scrollbarSettings.scrollRate();
    }

    public static FZAbstractScrollArea.ScrollbarSettings defaultSettings(final int scrollRate) {
        return new FZAbstractScrollArea.ScrollbarSettings(SCROLLER_SPRITE, null, SCROLLER_BACKGROUND_SPRITE, SCROLLBAR_WIDTH, SCROLLBAR_MIN_HEIGHT, scrollRate, true);
    }

    public record ScrollbarSettings(
            ResourceLocation scrollerSprite,
            @Nullable ResourceLocation disabledScrollerSprite,
            ResourceLocation backgroundSprite,
            int scrollbarWidth,
            int scrollbarMinHeight,
            int scrollRate,
            boolean resizingScrollbar
    ) {
    }
}
