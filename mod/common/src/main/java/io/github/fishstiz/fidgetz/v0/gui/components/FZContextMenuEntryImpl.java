package io.github.fishstiz.fidgetz.v0.gui.components;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.fishstiz.fidgetz.v0.Fidgetz;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.utils.GuiGraphicsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

final class FZContextMenuEntryImpl implements FZContextMenuEntry {
    private static final Component CHEVRON_RIGHT = Component.literal(">");
    private static final int CHEVRON_RIGHT_WIDTH = Minecraft.getInstance().font.width(CHEVRON_RIGHT);
    private static final int DEFAULT_TEXT_COLOR = CommonColors.WHITE;
    private static final int DEFAULT_INACTIVE_TEXT_COLOR = CommonColors.LIGHT_GRAY;
    private static final int SPACING = 8;
    private final List<FZContextMenuEntry> children;
    private final Supplier<@Nullable WidgetRenderables> backgroundSupplier;
    private final Supplier<@Nullable WidgetElements> iconSupplier;
    private final Supplier<@Nullable Component> messageSupplier;
    private final WidgetTooltipHolder tooltipHolder;
    private final BooleanSupplier activeSupplier;
    private final Builder.MouseAction mouseAction;
    private final Builder.KeyboardAction keyboardAction;
    private final boolean closeOnInteraction;
    private final boolean takeFocusAfterInteraction;
    private final boolean allowAutoDivideAfterEntry;
    private final boolean playClickSoundOnInteraction;
    private final boolean applyCursorWhenActive;
    private final int height;
    private final int minWidth;

    FZContextMenuEntryImpl(
            List<FZContextMenuEntry> children,
            Supplier<@Nullable WidgetRenderables> backgroundSupplier,
            Supplier<@Nullable WidgetElements> iconSupplier,
            Supplier<@Nullable Component> messageSupplier,
            WidgetTooltipHolder tooltipHolder,
            BooleanSupplier activeSupplier,
            Builder.MouseAction mouseAction,
            Builder.KeyboardAction keyboardAction,
            boolean closeOnInteraction,
            boolean takeFocusAfterInteraction,
            boolean allowAutoDivideAfterEntry,
            boolean playClickSoundOnInteraction,
            boolean applyCursorWhenActive,
            int height,
            int minWidth
    ) {
        this.children = children;
        this.backgroundSupplier = backgroundSupplier;
        this.iconSupplier = iconSupplier;
        this.messageSupplier = messageSupplier;
        this.tooltipHolder = tooltipHolder;
        this.activeSupplier = activeSupplier;
        this.mouseAction = mouseAction;
        this.keyboardAction = keyboardAction;
        this.closeOnInteraction = closeOnInteraction;
        this.takeFocusAfterInteraction = takeFocusAfterInteraction;
        this.allowAutoDivideAfterEntry = allowAutoDivideAfterEntry;
        this.playClickSoundOnInteraction = playClickSoundOnInteraction;
        this.applyCursorWhenActive = applyCursorWhenActive;
        this.height = height;
        this.minWidth = minWidth;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, FZContextMenuEntry.Context context, int mouseX, int mouseY, float partialTick) {
        if (applyCursorWhenActive && context.isActive() && context.isHovered()) {
            graphics.requestCursor(CursorTypes.POINTING_HAND);
        }

        ScreenRectangle bounds = context.getRectangle();
        final int left = bounds.left();
        final int top = bounds.top();
        final int width = bounds.width();
        final int height = bounds.height();

        WidgetRenderables background = backgroundSupplier.get();
        if (background != null) {
            background.get(context.isActive(), context.isFocusedOrHovered())
                    .extractRenderState(graphics, left, top, width, height, mouseX, mouseY, partialTick);
        }

        int x = left;

        WidgetElements icon = iconSupplier.get();
        if (icon != null) {
            x += SPACING + icon.margin().left();
            int iconWidth = icon.width();
            int iconHeight = icon.height();
            int iconY = (top + SPACING + ((height - SPACING * 2) - iconHeight) / 2) + icon.margin().top() - icon.margin().bottom();

            icon.elements()
                    .get(context.isActive(), context.isFocusedOrHovered())
                    .extractRenderState(graphics, x, iconY, iconWidth, iconHeight, mouseX, mouseY, partialTick);
            x += iconWidth + icon.margin().right();
        }

        int innerRight = left + width - SPACING;

        if (!children.isEmpty()) {
            int chevronX = (left + width) - CHEVRON_RIGHT_WIDTH - SPACING;
            innerRight = chevronX - SPACING;
            Font font = Minecraft.getInstance().font;
            int chevronHeight = font.lineHeight;
            int chevronColor = activeSupplier.getAsBoolean() ? DEFAULT_TEXT_COLOR : DEFAULT_INACTIVE_TEXT_COLOR;
            int chevronY = top + (height - chevronHeight) / 2;
            graphics.text(font, CHEVRON_RIGHT, chevronX, chevronY + 1, chevronColor);
        }

        Component text = messageSupplier.get();
        if (text != null) {
            x += SPACING;
            int textWidth = Math.max(0, innerRight - x);
            GuiGraphicsUtils.scrollingText(graphics, text, x, top + 1, x + textWidth, top + height + 1, activeSupplier.getAsBoolean()
                    ? DEFAULT_TEXT_COLOR
                    : DEFAULT_INACTIVE_TEXT_COLOR
            );
        }

        if (context.isHovered()) {
            tooltipHolder.refreshTooltipForNextRenderPass(graphics, mouseX, mouseY, true, false, bounds);
        }
    }

    private void playClickSound() {
        if (playClickSoundOnInteraction) {
            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, FZContextMenuEntry.Context context) {
        if (context.isActive() && mouseAction.mouseClicked(event, context)) {
            playClickSound();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent, Context context) {
        if (context.isActive() && keyboardAction.keyPressed(keyEvent, context)) {
            playClickSound();
            return true;
        }
        return false;
    }

    @Override
    public List<FZContextMenuEntry> childEntries() {
        return children;
    }

    @Override
    public boolean active() {
        return activeSupplier.getAsBoolean();
    }

    @Override
    public boolean shouldCloseOnInteraction() {
        return closeOnInteraction;
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return takeFocusAfterInteraction;
    }

    @Override
    public boolean canAutoDivideAfterEntry() {
        return allowAutoDivideAfterEntry;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int minWidth() {
        return minWidth;
    }

    @Override
    public Component message() {
        Component message = messageSupplier.get();
        return message == null ? CommonComponents.EMPTY : message;
    }

    @Override
    public void updateNarration(NarrationElementOutput output, FZContextMenuEntry.Context context) {
        tooltipHolder.updateNarration(output);
    }

    record Divider(RenderableRectangle rectangle, int height, boolean fallback) implements FZContextMenuEntry.Divider {
        static final Divider DEFAULT_SECTION = new FZContextMenuEntryImpl.Divider(Renderables.sprite(Fidgetz.id("widget/contextmenu_section_divider")), true);
        static final Divider DEFAULT_ENTRY = new FZContextMenuEntryImpl.Divider(Renderables.sprite(Fidgetz.id("widget/contextmenu_entry_divider")), false);

        Divider(RenderableRectangle rectangle, int height) {
            this(rectangle, height, false);
        }

        Divider(RenderableRectangle rectangle, boolean fallback) {
            this(rectangle, DEFAULT_HEIGHT, fallback);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, Context context, int mouseX, int mouseY, float partialTick) {
            ScreenRectangle bounds = context.getRectangle();
            rectangle.extractRenderState(graphics, bounds.left(), bounds.top(), bounds.width(), bounds.height(), mouseX, mouseY, partialTick);
        }
    }

    static final class TooltipHolder extends WidgetTooltipHolder {
        private final WidgetTooltipHolder delegate;
        private final @Nullable Supplier<@Nullable Tooltip> tooltipSupplier;

        private TooltipHolder(WidgetTooltipHolder delegate, @Nullable Supplier<@Nullable Tooltip> tooltipSupplier) {
            this.delegate = delegate;
            this.tooltipSupplier = tooltipSupplier;
        }

        TooltipHolder(WidgetTooltipHolder delegate) {
            this(delegate, null);
        }

        TooltipHolder(Supplier<@Nullable Tooltip> tooltipSupplier) {
            this(new WidgetTooltipHolder(), tooltipSupplier);
        }

        @Override
        public void setDelay(Duration delay) {
            delegate.setDelay(delay);
        }

        @Override
        public void set(@Nullable Tooltip tooltip) {
            delegate.set(tooltip);
        }

        @Override
        public @Nullable Tooltip get() {
            return delegate.get();
        }

        @Override
        public void refreshTooltipForNextRenderPass(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovered, boolean isFocused, ScreenRectangle screenRectangle) {
            if (tooltipSupplier != null) delegate.set(tooltipSupplier.get());
            delegate.refreshTooltipForNextRenderPass(graphics, mouseX, mouseY, isHovered, isFocused, screenRectangle);
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
            delegate.updateNarration(output);
        }
    }
}
