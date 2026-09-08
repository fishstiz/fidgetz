package io.github.fishstiz.fidgetz.v0.utils;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.TabNavigation;
import net.minecraft.client.gui.navigation.FocusNavigationEvent.ArrowNavigation;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.input.KeyEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class NavigationUtils {
    public static @Nullable ScreenDirection getDirection(KeyEvent event) {
        if (event.isUp()) {
            return ScreenDirection.UP;
        }
        if (event.isDown()) {
            return ScreenDirection.DOWN;
        }
        if (event.isLeft()) {
            return ScreenDirection.LEFT;
        }
        if (event.isRight()) {
            return ScreenDirection.RIGHT;
        }
        return null;
    }

    public static boolean isUp(FocusNavigationEvent event, boolean includeTab) {
        return (includeTab && event instanceof TabNavigation(boolean forward) && !forward) ||
               (event instanceof ArrowNavigation(ScreenDirection direction, _) && direction == ScreenDirection.UP);
    }

    public static boolean isDown(FocusNavigationEvent event, boolean includeTab) {
        return (includeTab && event instanceof TabNavigation(boolean forward) && forward) ||
               (event instanceof ArrowNavigation(ScreenDirection direction, _) && direction == ScreenDirection.DOWN);
    }

    public static void walk(ComponentPath path, Consumer<GuiEventListener> walker) {
        walker.accept(path.component());
        if (path instanceof ComponentPath.Path(_, ComponentPath childPath)) {
            walk(childPath, walker);
        }
    }

    public static Stream<GuiEventListener> stream(ComponentPath path) {
        Stream.Builder<GuiEventListener> builder = Stream.builder();
        walk(path, builder::add);
        return builder.build();
    }

    public static @Nullable ComponentPath takeWhile(ComponentPath path, Predicate<GuiEventListener> walker) {
        GuiEventListener component = path.component();

        if (!walker.test(component)) {
            return null;
        }

        if (path instanceof ComponentPath.Path(ContainerEventHandler container, ComponentPath childPath)) {
            ComponentPath validPath = takeWhile(childPath, walker);
            if (validPath != null) {
                return ComponentPath.path(container, validPath);
            }
        }

        return ComponentPath.leaf(component);
    }

    public static @Nullable ComponentPath initialFocus(ContainerEventHandler container) {
        GuiEventListener focused = container.getFocused();
        if (focused != null) {
            ComponentPath path = focused.nextFocusPath(new FocusNavigationEvent.InitialFocus());
            return path == null ? ComponentPath.path(focused, container) : ComponentPath.path(container, path);
        }

        List<? extends GuiEventListener> children = container.children();
        if (children.isEmpty()) return null;

        GuiEventListener firstFocusable = null;
        for (GuiEventListener child : children) {
            if (child instanceof NarratableEntry narratable && !narratable.isActive()) {
                continue;
            }
            if (firstFocusable == null || child.getTabOrderGroup() < firstFocusable.getTabOrderGroup()) {
                firstFocusable = child;
            }
        }

        if (firstFocusable == null) return null;

        ComponentPath path = firstFocusable instanceof ContainerEventHandler subContainer
                ? initialFocus(subContainer)
                : firstFocusable.nextFocusPath(new FocusNavigationEvent.InitialFocus());

        return path == null ? ComponentPath.path(firstFocusable, container) : ComponentPath.path(container, path);
    }

    public static @Nullable ComponentPath findPath(ContainerEventHandler container, GuiEventListener targetChild) {
        for (GuiEventListener child : container.children()) {
            if (child == targetChild) {
                return ComponentPath.path(child, container);
            }
            if (child instanceof ContainerEventHandler subContainer) {
                ComponentPath path = findPath(subContainer, targetChild);
                if (path != null) {
                    return ComponentPath.path(container, path);
                }
            }
        }
        return null;
    }

    public static ComponentPath addFocusEffects(
            ComponentPath path,
            @Nullable BiConsumer<GuiEventListener, Boolean> preFocus,
            @Nullable BiConsumer<GuiEventListener, Boolean> postFocus
    ) {
        return new EffectComponentPath(path, preFocus, postFocus);
    }

    public static ComponentPath beforeFocusEffect(ComponentPath path, Consumer<GuiEventListener> effect) {
        Objects.requireNonNull(effect, "effect cannot be null");
        return addFocusEffects(path, null, (component, focused) -> {
            if (focused) effect.accept(component);
        });
    }

    public static ComponentPath afterFocusEffect(ComponentPath path, Consumer<GuiEventListener> effect) {
        Objects.requireNonNull(effect, "effect cannot be null");
        return addFocusEffects(path, null, (component, focused) -> {
            if (focused) effect.accept(component);
        });
    }

    public static ComponentPath afterFocusEffect(ComponentPath path, BiConsumer<GuiEventListener, Boolean> effect) {
        return addFocusEffects(path, null, effect);
    }

    private record EffectComponentPath(
            ComponentPath path,
            @Nullable BiConsumer<GuiEventListener, Boolean> preFocusEffect,
            @Nullable BiConsumer<GuiEventListener, Boolean> postFocusEffect
    ) implements ComponentPath {
        @Override
        public void applyFocus(boolean focused) {
            if (preFocusEffect != null) preFocusEffect.accept(component(), focused);
            path.applyFocus(focused);
            if (postFocusEffect != null) postFocusEffect.accept(component(), focused);
        }

        @Override
        public GuiEventListener component() {
            return path.component();
        }

        @Override
        public GuiEventListener leafComponent() {
            return path.leafComponent();
        }
    }


    private NavigationUtils() {
    }
}
