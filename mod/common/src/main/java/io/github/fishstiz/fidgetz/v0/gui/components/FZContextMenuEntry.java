package io.github.fishstiz.fidgetz.v0.gui.components;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.Fidgetz;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.utils.FunctionUtils;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface FZContextMenuEntry {
    int DEFAULT_HEIGHT = 20;
    int DEFAULT_MIN_WIDTH = 0;

    void extractRenderState(GuiGraphicsExtractor graphics, Context context, int mouseX, int mouseY, float partialTick);

    default int height() {
        return DEFAULT_HEIGHT;
    }

    default int minWidth() {
        return DEFAULT_MIN_WIDTH;
    }

    default boolean active() {
        return true;
    }

    default Component message() {
        return CommonComponents.EMPTY;
    }

    default boolean shouldTakeFocusAfterInteraction() {
        return true;
    }

    default boolean shouldCloseOnInteraction() {
        return true;
    }

    default boolean canAutoDivideAfterEntry() {
        return true;
    }

    default List<? extends FZContextMenuEntry> childEntries() {
        return Collections.emptyList();
    }

    default boolean mouseClicked(MouseButtonEvent event, Context context) {
        return false;
    }

    default boolean keyPressed(KeyEvent keyEvent, Context context) {
        return false;
    }

    default @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent, Context context) {
        return null;
    }

    default void updateNarration(NarrationElementOutput output, Context context) {
    }

    static Divider sectionDivider() {
        return FZContextMenuEntryImpl.Divider.DEFAULT_SECTION;
    }

    static Divider createDivider(RenderableRectangle rectangle, int height) {
        return new FZContextMenuEntryImpl.Divider(rectangle, height);
    }

    static Builder builder() {
        return new Builder();
    }

    @ApiStatus.NonExtendable
    interface Context {
        void closeMenu();

        boolean isActive();

        boolean isHovered();

        boolean isFocused();

        boolean isChildOpened();

        Component getMessage();

        ScreenRectangle getRectangle();

        default boolean isFocusedOrHovered() {
            return isFocused() || isHovered();
        }
    }

    interface Divider extends FZContextMenuEntry {
        int DEFAULT_HEIGHT = 4;

        @Override
        default int height() {
            return DEFAULT_HEIGHT;
        }

        @Override
        default boolean active() {
            return false;
        }

        default boolean fallback() {
            return false;
        }

        @Override
        default boolean canAutoDivideAfterEntry() {
            return false;
        }
    }

    interface Collector {
        void addEntry(FZContextMenuEntry entry);

        default void addEntry(UnaryOperator<FZContextMenuEntry.Builder> builderConfigurator) {
            FZContextMenuEntry.Builder builder = FZContextMenuEntry.builder();
            builderConfigurator.apply(builder);
            addEntry(builder.build());
        }

        default Collector nextSection() {
            addEntry(sectionDivider());
            return this;
        }
    }

    interface Source {
        default void fidgetz$updateContextEntries(double x, double y, Collector collector) {
            if (this instanceof ContainerEventHandler containerEventHandler) {
                containerEventHandler.getChildAt(x, y)
                        .filter(Source.class::isInstance)
                        .map(Source.class::cast)
                        .ifPresent(child -> child.fidgetz$updateContextEntries(x, y, collector.nextSection()));
            }
        }

        default List<FZContextMenuEntry> fidgetz$collectContextEntries(double x, double y) {
            List<FZContextMenuEntry> entries = new ArrayList<>();
            fidgetz$updateContextEntries(x, y, entries::add);
            return entries;
        }
    }

    final class Builder {
        private static final WidgetRenderables DEFAULT_BACKGROUND = new WidgetRenderables(
                Renderables.sprite(Fidgetz.id("widget/contextmenu_entry")),
                Renderables.sprite(Fidgetz.id("widget/contextmenu_entry")),
                Renderables.sprite(Fidgetz.id("widget/contextmenu_entry_highlighted"))
        );
        private final List<FZContextMenuEntry> children = new ArrayList<>();
        private @Nullable Supplier<@Nullable WidgetRenderables> backgroundSupplier = () -> DEFAULT_BACKGROUND;
        private @Nullable Supplier<@Nullable WidgetElements> iconSupplier;
        private @Nullable Supplier<@Nullable Component> messageSupplier;
        private FZContextMenuEntryImpl.@Nullable TooltipHolder tooltipHolder;
        private @Nullable BooleanSupplier activeSupplier;
        private @Nullable MouseAction mouseAction;
        private @Nullable KeyboardAction keyboardAction;
        private boolean closeOnInteraction = true;
        private boolean takeFocusOnInteraction = true;
        private boolean allowDivideAfterEntry = true;
        private boolean playClickSoundOnInteraction = true;
        private boolean applyCursorWhenActive = true;
        private int height = DEFAULT_HEIGHT;
        private int minWidth = DEFAULT_MIN_WIDTH;
        private boolean keyboardActionCopied = false;

        private Builder() {
        }

        public Builder background(@Nullable Supplier<@Nullable WidgetRenderables> backgroundSupplier) {
            this.backgroundSupplier = backgroundSupplier;
            return this;
        }

        public Builder background(WidgetRenderables background) {
            Objects.requireNonNull(background, "background cannot be null");
            return background(() -> background);
        }

        public Builder background(RenderableRectangle background) {
            return background(new WidgetRenderables(Objects.requireNonNull(background, "background cannot be null")));
        }

        public Builder icon(@Nullable Supplier<@Nullable WidgetElements> iconSupplier) {
            this.iconSupplier = iconSupplier;
            return this;
        }

        public Builder icon(WidgetElements icon) {
            return icon(() -> icon);
        }

        public Builder icon(WidgetRenderables icon) {
            Objects.requireNonNull(icon, "icon cannot be null");
            return icon(new WidgetElements(icon, 16, 16));
        }

        public Builder icon(RenderableRectangle icon) {
            Objects.requireNonNull(icon, "icon cannot be null");
            return icon(new WidgetRenderables(icon));
        }

        public Builder message(@Nullable Supplier<@Nullable Component> textSupplier) {
            this.messageSupplier = textSupplier;
            return this;
        }

        public Builder message(@Nullable Component message) {
            return message(() -> message);
        }

        public Builder tooltip(@Nullable Supplier<@Nullable Tooltip> tooltipSupplier) {
            this.tooltipHolder = tooltipSupplier == null ? null : new FZContextMenuEntryImpl.TooltipHolder(tooltipSupplier);
            return this;
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            return tooltip(() -> tooltip);
        }

        public Builder tooltip(@Nullable WidgetTooltipHolder tooltipHolder) {
            this.tooltipHolder = tooltipHolder == null ? null : new FZContextMenuEntryImpl.TooltipHolder(tooltipHolder);
            return this;
        }

        public Builder active(@Nullable BooleanSupplier activeSupplier) {
            this.activeSupplier = activeSupplier;
            return this;
        }

        public Builder active(boolean active) {
            return active(() -> active);
        }

        public Builder active() {
            return active(true);
        }

        public Builder child(FZContextMenuEntry child) {
            children.add(child);
            return this;
        }

        public Builder child(UnaryOperator<FZContextMenuEntry.Builder> builderConfigurator) {
            children.add(builderConfigurator.apply(FZContextMenuEntry.builder()).build());
            return this;
        }

        public Builder nextSection() {
            return child(FZContextMenuEntry.sectionDivider());
        }

        public Builder children(List<FZContextMenuEntry> children) {
            this.children.addAll(children);
            return this;
        }

        public Builder closeOnInteraction(boolean closeOnInteraction) {
            this.closeOnInteraction = closeOnInteraction;
            return this;
        }

        public Builder closeOnInteraction() {
            return closeOnInteraction(true);
        }

        public Builder takeFocusOnInteraction(boolean takeFocusOnInteraction) {
            this.takeFocusOnInteraction = takeFocusOnInteraction;
            return this;
        }

        public Builder takeFocusOnInteraction() {
            return takeFocusOnInteraction(true);
        }

        public Builder allowAutoDivideAfterEntry(boolean allowAutoDivideAfterEntry) {
            this.allowDivideAfterEntry = allowAutoDivideAfterEntry;
            return this;
        }

        public Builder allowAutoDivideAfterEntry() {
            this.allowDivideAfterEntry = true;
            return this;
        }

        public Builder playClickSoundOnInteraction(boolean playClickSoundOnInteraction) {
            this.playClickSoundOnInteraction = playClickSoundOnInteraction;
            return this;
        }

        public Builder playClickSoundOnInteraction() {
            return playClickSoundOnInteraction(true);
        }

        public Builder applyCursorChangeWhenActive(boolean applyCursorChangeWhenActive) {
            this.applyCursorWhenActive = applyCursorChangeWhenActive;
            return this;
        }

        public Builder applyCursorChangeWhenActive() {
            return applyCursorChangeWhenActive(true);
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder onClick(@Nullable MouseAction mouseAction) {
            this.mouseAction = mouseAction;
            if (mouseAction == null && keyboardActionCopied) keyboardAction = null;
            return this;
        }

        public Builder onClick(BooleanSupplier mouseAction) {
            Objects.requireNonNull(mouseAction, "mouseAction is null");

            onClick((event, _) -> {
                if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                    return mouseAction.getAsBoolean();
                }
                return false;
            });

            if (keyboardAction == null) {
                onKeyPress((event, _) -> {
                    if (event.isSelection()) {
                        return mouseAction.getAsBoolean();
                    }
                    return false;
                });
                this.keyboardActionCopied = true;
            }

            return this;
        }

        public Builder onClick(Runnable mouseAction) {
            Objects.requireNonNull(mouseAction, "mouseAction is null");

            onClick((event, _) -> {
                if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                    mouseAction.run();
                    return true;
                }
                return false;
            });

            if (keyboardAction == null) {
                onKeyPress((event, _) -> {
                    if (event.isSelection()) {
                        mouseAction.run();
                        return true;
                    }
                    return false;
                });
                this.keyboardActionCopied = true;
            }

            return this;
        }

        public Builder preventClick() {
            this.mouseAction = null;
            if (this.keyboardActionCopied) {
                this.keyboardAction = null;
                this.keyboardActionCopied = false;
            }
            return this;
        }

        public Builder onKeyPress(@Nullable KeyboardAction keyboardAction) {
            this.keyboardAction = keyboardAction;
            this.keyboardActionCopied = false;
            return this;
        }

        public Builder preventKeyPress() {
            this.keyboardAction = null;
            this.keyboardActionCopied = false;
            return this;
        }

        public FZContextMenuEntry build() {
            KeyboardAction keyboardAction = this.keyboardAction;
            KeyboardAction keyboardActionCopy = this.keyboardAction;

            if (keyboardAction != null && keyboardActionCopied) {
                boolean closeOnInteraction = this.closeOnInteraction;
                keyboardAction = (event, ctx) -> {
                    if (keyboardActionCopy.keyPressed(event, ctx)) {
                        if (closeOnInteraction) ctx.closeMenu();
                        return true;
                    }
                    return false;
                };
            }

            return new FZContextMenuEntryImpl(
                    children,
                    backgroundSupplier == null ? FunctionUtils.nullSupplier() : backgroundSupplier,
                    iconSupplier == null ? FunctionUtils.nullSupplier() : iconSupplier,
                    messageSupplier == null ? FunctionUtils.nullSupplier() : messageSupplier,
                    tooltipHolder == null ? new WidgetTooltipHolder() : tooltipHolder,
                    activeSupplier == null ? () -> true : activeSupplier,
                    mouseAction == null ? (_, _) -> false : mouseAction,
                    keyboardAction == null ? (_, _) -> false : keyboardAction,
                    closeOnInteraction,
                    takeFocusOnInteraction,
                    allowDivideAfterEntry,
                    playClickSoundOnInteraction,
                    applyCursorWhenActive && mouseAction != null,
                    height,
                    minWidth
            );
        }

        @FunctionalInterface
        public interface MouseAction {
            boolean mouseClicked(MouseButtonEvent event, Context context);
        }

        @FunctionalInterface
        public interface KeyboardAction {
            boolean keyPressed(KeyEvent event, Context context);
        }
    }
}
