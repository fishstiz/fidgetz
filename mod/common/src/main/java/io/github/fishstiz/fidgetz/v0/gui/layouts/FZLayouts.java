package io.github.fishstiz.fidgetz.v0.gui.layouts;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FZLayouts {
    private FZLayouts() {
    }

    private static FZFlexLayout flex(Supplier<ScreenRectangle> screenArea, ScreenAxis axis) {
        return new FZFlexLayout(screenArea, axis);
    }

    public static FZFlexLayout flexVertical() {
        return FZFlexLayout.auto(ScreenAxis.VERTICAL);
    }

    public static FZFlexLayout flexVertical(LayoutElement container) {
        return flex(container::getRectangle, ScreenAxis.VERTICAL);
    }

    public static FZFlexLayout flexVertical(GuiEventListener container) {
        return flex(container::getRectangle, ScreenAxis.VERTICAL);
    }

    public static FZFlexLayout flexHorizontal() {
        return FZFlexLayout.auto(ScreenAxis.HORIZONTAL);
    }

    public static FZFlexLayout flexHorizontal(LayoutElement container) {
        return flex(container::getRectangle, ScreenAxis.HORIZONTAL);
    }

    public static FZFlexLayout flexHorizontal(GuiEventListener container) {
        return flex(container::getRectangle, ScreenAxis.HORIZONTAL);
    }

    public static FZScrollableLayout scrollable(Supplier<ScreenRectangle> screenArea, Layout layout) {
        return new FZScrollableLayout(screenArea, layout);
    }

    public static FZScrollableLayout scrollable(LayoutElement container, Layout layout) {
        return scrollable(container::getRectangle, layout);
    }

    public static FZScrollableLayout scrollable(GuiEventListener container, Layout layout) {
        return scrollable(container::getRectangle, layout);
    }

    public static FZPaddedLayout padded(Layout layout) {
        return new FZPaddedLayout(layout);
    }

    private static FZAlignedLayout aligned(Supplier<ScreenRectangle> screenArea, Layout layout, float alignX, float alignY) {
        return new FZAlignedLayout(screenArea, layout).align(alignX, alignY);
    }

    public static FZAlignedLayout aligned(LayoutElement container, Layout layout, float alignX, float alignY) {
        return aligned(container::getRectangle, layout, alignX, alignY);
    }

    public static FZAlignedLayout aligned(GuiEventListener container, Layout layout, float alignX, float alignY) {
        return aligned(container::getRectangle, layout, alignX, alignY);
    }

    private static FZAlignedLayout centered(Supplier<ScreenRectangle> screenArea, Layout layout) {
        return new FZAlignedLayout(screenArea, layout).centered();
    }

    public static FZAlignedLayout centered(LayoutElement container, Layout layout) {
        return centered(container::getRectangle, layout);
    }

    public static FZAlignedLayout centered(GuiEventListener container, Layout layout) {
        return centered(container::getRectangle, layout);
    }

    private static FZClampedLayout clamped(Supplier<ScreenRectangle> screenArea, Layout layout) {
        return new FZClampedLayout(screenArea, layout);
    }

    public static FZClampedLayout clamped(LayoutElement container, Layout layout) {
        return clamped(container::getRectangle, layout);
    }

    public static FZClampedLayout clamped(GuiEventListener container, Layout layout) {
        return clamped(container::getRectangle, layout);
    }

    public static <T extends Layout> Composer<T> composer(GuiEventListener container, T base) {
        return new Composer<>(base, container::getRectangle);
    }

    public static <T extends Layout> Composer<T> composer(LayoutElement container, T base) {
        return new Composer<>(base, container::getRectangle);
    }

    public static final class Composer<T extends Layout> {
        private final Supplier<ScreenRectangle> screenArea;
        private final T current;

        private Composer(T current, Supplier<ScreenRectangle> screenArea) {
            this.screenArea = screenArea;
            this.current = current;
        }

        public Composer<FZAlignedLayout> aligned(Consumer<FZAlignedLayout> configurator) {
            FZAlignedLayout layout = FZLayouts.aligned(screenArea, current, 0.0f, 0.0f);
            configurator.accept(layout);
            return new Composer<>(layout, screenArea);
        }

        public Composer<FZAlignedLayout> aligned(float alignX, float alignY) {
            return new Composer<>(FZLayouts.aligned(screenArea, current, alignX, alignY), screenArea);
        }

        public Composer<FZAlignedLayout> centered() {
            return new Composer<>(FZLayouts.centered(screenArea, current), screenArea);
        }

        public Composer<FZAlignedLayout> centered(Consumer<FZAlignedLayout> configurator) {
            FZAlignedLayout layout = FZLayouts.centered(screenArea, current);
            configurator.accept(layout);
            return new Composer<>(layout, screenArea);
        }

        public Composer<FZClampedLayout> clamped() {
            return new Composer<>(FZLayouts.clamped(screenArea, current), screenArea);
        }

        public Composer<FZClampedLayout> clamped(Consumer<FZClampedLayout> configurator) {
            FZClampedLayout layout = FZLayouts.clamped(screenArea, current);
            configurator.accept(layout);
            return new Composer<>(layout, screenArea);
        }

        public Composer<FZScrollableLayout> scrollable() {
            return new Composer<>(FZLayouts.scrollable(screenArea, current), screenArea);
        }

        public Composer<FZScrollableLayout> scrollable(Consumer<FZScrollableLayout> configurator) {
            FZScrollableLayout layout = FZLayouts.scrollable(screenArea, current);
            configurator.accept(layout);
            return new Composer<>(layout, screenArea);
        }

        public Composer<FZScrollableLayout> scrollable(int maxHeight) {
            return new Composer<>(FZLayouts.scrollable(screenArea, current).maxHeight(maxHeight), screenArea);
        }

        public Composer<FZPaddedLayout> padded(Consumer<FZPaddedLayout> configurator) {
            FZPaddedLayout layout = FZLayouts.padded(current);
            configurator.accept(layout);
            return new Composer<>(layout, screenArea);
        }

        public Composer<FZPaddedLayout> padded(int padding) {
            return new Composer<>(FZLayouts.padded(current).padding(padding), screenArea);
        }

        public Composer<FZPaddedLayout> padded(int left, int top, int right, int bottom) {
            return new Composer<>(FZLayouts.padded(current).padding(left, top, right, bottom), screenArea);
        }

        public Composer<T> arrange() {
            current.arrangeElements();
            return this;
        }

        public Composer<T> visitChildren(Consumer<LayoutElement> visitor) {
            current.visitChildren(visitor);
            return this;
        }

        public Composer<T> visitWidgets(Consumer<AbstractWidget> visitor) {
            current.visitWidgets(visitor);
            return this;
        }

        public <L extends Layout> Composer<L> compose(Function<T, L> composer) {
            return new Composer<>(composer.apply(current), screenArea);
        }

        public T get() {
            return current;
        }
    }
}
