package io.github.fishstiz.fidgetz.v0.gui.components;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.*;
import java.util.function.Consumer;

public final class GuiComponentCollector {
    private static final Comparator<OrderedEntry<?>> WIDGET_COMPARATOR = Comparator.comparingInt(OrderedEntry::order);
    private static final Comparator<OrderedEntry<?>> RENDERABLE_COMPARATOR = WIDGET_COMPARATOR.reversed();
    private final List<OrderedEntry<? extends GuiEventListener>> popoverWidgets = new ArrayList<>();
    private final List<OrderedEntry<Renderable>> popoverRenderables = new ArrayList<>();
    private final List<GuiEventListener> widgets = new ArrayList<>();
    private final List<Renderable> renderables = new ArrayList<>();

    private record OrderedEntry<T>(T value, int order) {
    }

    @SuppressWarnings("unchecked")
    private static <T extends GuiEventListener & NarratableEntry> T asWidget(GuiEventListener widget) {
        return (T) widget;
    }

    private void collectPopover(FZPopover popover, boolean includeWidgets, boolean includeRenderables) {
        if (popover instanceof FZPopoverContainer container) {
            container.fidgetz$visitPopovers(nested -> collectPopover(nested, includeWidgets, includeRenderables));
        }

        int order = popover.fidgetz$popoverOrder();
        if (includeWidgets) {
            popover.fidgetz$visitWidgets(new WidgetVisitor() {
                @Override
                public <T extends GuiEventListener & NarratableEntry> void visitWidget(T widget) {
                    popoverWidgets.add(new OrderedEntry<>(widget, order));
                }
            });
        }
        if (includeRenderables) {
            popover.fidgetz$visitRenderables(r -> popoverRenderables.add(new OrderedEntry<>(r, order)));
        }
    }

    public <T extends GuiEventListener & NarratableEntry & Renderable> T renderableWidget(T widget) {
        if (widget instanceof FZPopover popover) {
            collectPopover(popover, true, true);
            return widget;
        }

        widgets.add(widget);
        renderables.add(widget);

        if (widget instanceof FZPopoverContainer container) {
            container.fidgetz$visitPopovers(popover -> collectPopover(popover, true, true));
        }

        return widget;
    }

    public <T extends GuiEventListener & NarratableEntry> T widgetOnly(T widget) {
        if (widget instanceof FZPopover popover) {
            collectPopover(popover, true, false);
            return widget;
        }

        widgets.add(widget);

        if (widget instanceof FZPopoverContainer container) {
            container.fidgetz$visitPopovers(popover -> collectPopover(popover, true, false));
        }

        return widget;
    }

    public <T extends Renderable> T renderableOnly(T renderable) {
        if (renderable instanceof FZPopover popover) {
            collectPopover(popover, false, true);
            return renderable;
        }

        renderables.add(renderable);

        if (renderable instanceof FZPopoverContainer container) {
            container.fidgetz$visitPopovers(popover -> collectPopover(popover, false, true));
        }

        return renderable;
    }

    public void flushTo(WidgetVisitor widgetSink, Consumer<Renderable> renderableSink) {
        if (!popoverWidgets.isEmpty()) {
            popoverWidgets.sort(WIDGET_COMPARATOR);
            Iterator<OrderedEntry<? extends GuiEventListener>> iterator = popoverWidgets.iterator();
            while (iterator.hasNext()) {
                OrderedEntry<? extends GuiEventListener> entry = iterator.next();
                widgetSink.visitWidget(asWidget(entry.value));
                iterator.remove();
            }
        }

        Iterator<GuiEventListener> widgetsIterator = widgets.iterator();
        while (widgetsIterator.hasNext()) {
            GuiEventListener widget = widgetsIterator.next();
            widgetSink.visitWidget(asWidget(widget));
            widgetsIterator.remove();
        }

        Iterator<Renderable> renderablesIterator = renderables.iterator();
        while (renderablesIterator.hasNext()) {
            Renderable renderable = renderablesIterator.next();
            renderableSink.accept(renderable);
            renderablesIterator.remove();
        }

        if (!popoverRenderables.isEmpty()) {
            popoverRenderables.sort(RENDERABLE_COMPARATOR);
            Iterator<OrderedEntry<Renderable>> iterator = popoverRenderables.iterator();
            while (iterator.hasNext()) {
                renderableSink.accept(iterator.next().value);
                iterator.remove();
            }
        }
    }
}
