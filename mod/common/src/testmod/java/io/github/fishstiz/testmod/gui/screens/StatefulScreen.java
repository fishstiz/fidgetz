package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.FZContextMenuEntry;
import io.github.fishstiz.fidgetz.v0.gui.components.FZText;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZClampedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import io.github.fishstiz.fidgetz.v0.gui.layouts.Justification;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public class StatefulScreen extends FZScreen {
    private final FZMutableRef<State> state = new FZMutableRef<>(State.defaults());
    private @Nullable FZClampedLayout rootLayout;

    public StatefulScreen() {
        super(Component.literal("Stateful Screen"));
    }

    private void resetCount() {
        state.set(prev -> prev.withCount(0));
    }

    private void incrementCount() {
        state.set(prev -> prev.withCount(prev.itemCount() + 1));
    }

    private void decrementCount() {
        state.set(prev -> prev.withCount(prev.itemCount() - 1));
    }

    private void handleCycle(FZButton.PressEvent e) {
        if (e.input().hasShiftDown()) {
            state.set(prev -> prev.withOption(prev.option().previous()));
        } else {
            state.set(prev -> prev.withOption(prev.option().next()));
        }
    }

    private void toggleOption() {
        state.set(prev -> prev.withToggle(!prev.toggled()));
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
        FZFlexLayout rootLayout = FZLayouts.flexVertical(this).spacing(8).also(root -> {
            var titleText = root.child(FZText.builder(title).build(), root.newChildSettings().alignHorizontallyCenter());

            collector.renderableOnly(Renderables.fill(ARGB.color(0.3f, CommonColors.BLUE)).toRenderable(titleText::getRectangle));

            root.child(FZLayouts.flexVertical().spacing(8), root.flexChildSettings()).also(items -> {
                items.child(FZLayouts.flexHorizontal().spacing(8), items.flexChildHorizontalSettings()).also(controls -> {
                    controls.child(
                            FZButton.bind("count", state.map(s -> FZButton.builder()
                                    .message(Component.literal("Count: " + s.itemCount()))
                                    .onPress(this::incrementCount)
                                    .contextEntries(FZContextMenuEntry.builder()
                                            .message(Component.literal("Reset Count"))
                                            .onClick(this::resetCount)
                                            .build())
                                    .toProps()
                            )),
                            controls.flexChildHorizontalSettings()
                                    .minFlexWidth(60)
                                    .maxFlexWidth(150)
                    );

                    var spacer = controls.spacer(controls.flexChildSettings());

                    collector.renderableOnly(Renderables.fill(ARGB.color(0.7f, CommonColors.YELLOW)).toRenderable(spacer::getRectangle));
                    collector.renderableOnly(Renderables.text(() -> Component.literal("Spacer:" + spacer.getWidth())
                            .withColor(CommonColors.BLACK).withoutShadow()).toRenderable(spacer::getRectangle));

                    var reset = controls.child(
                            FZButton.builder()
                                    .message(Component.literal("Reset Count"))
                                    .onPress(this::resetCount)
                                    .build(),
                            state.bind(
                                    "reset-settings",
                                    controls.flexChildHorizontalSettings().maxFlexWidth(150),
                                    (s, c) -> c.minFlexWidth(100 + s.itemCount)
                            )
                    );

                    collector.renderableOnly(Renderables.text(() -> Component.literal("width: " + reset.getWidth()))
                            .pose(matrix -> matrix.translate(0, (float) reset.getHeight() / 2 + 4.5f))
                            .toPopover(reset::getRectangle));

                    controls.child(state.bind(
                            "decrement1",
                            FZButton.builder()
                                    .onPress(this::decrementCount)
                                    .square()
                                    .message(Component.literal("-"))
                                    .build(),
                            (s, btn) -> btn.active = s.itemCount > 0
                    ));

                    controls.child(FZButton.bind("decrement2", state.map(s -> FZButton.builder()
                            .square()
                            .message(Component.literal("-"))
                            .active(s.itemCount > 0)
                            .onPress(this::decrementCount)
                            .toProps())));

                    controls.child(FZButton.builder()
                            .square()
                            .message(Component.literal("+"))
                            .onPress(this::incrementCount)
                            .build());

                    collector.renderableOnly(Renderables.fill(ARGB.color(0.3f, CommonColors.GREEN)).toPopover(controls::getRectangle));
                });

                var contents = FZLayouts.flexVertical().spacing(8);
                var scrollableContents = items.child(
                        FZLayouts.scrollable(this, state.bind("contents", contents, (s, c) -> {
                            c.clear();
                            for (int i = 0; i < s.itemCount(); i++) {
                                c.child(FZButton.builder().message(Component.literal("Item: " + i)).build(), c.flexChildHorizontalSettings());
                            }
                        })),
                        items.flexChildSettings()
                );

                collector.renderableOnly(Renderables.fill(ARGB.color(0.3f, CommonColors.BLUE)).toPopover(scrollableContents::getRectangle));
                collector.renderableOnly(Renderables.outline(CommonColors.RED).toPopover(contents::getRectangle));

                state.subscribe("items-layout", items::arrangeElements);
            });

            root.child(FZLayouts.flexHorizontal().spacing(8), root.flexChildHorizontalSettings()).also(buttons -> {
                buttons.justifyContents(Justification.CENTER);

                buttons.child(FZButton.bind("cycling-button", state.map(s -> FZButton.builder()
                        .message(CommonComponents.optionNameValue(Component.literal("Enum"), Component.literal(s.option.toString())))
                        .smallWidth()
                        .onPress(this::handleCycle)
                        .toProps())));

                buttons.child(FZButton.bind("toggle-button", state.map(s -> FZButton.builder()
                        .smallWidth()
                        .message(CommonComponents.optionNameValue(Component.literal("Toggle"), CommonComponents.optionStatus(s.toggled)))
                        .onPress(this::toggleOption)
                        .toProps())));

                collector.renderableOnly(Renderables.fill(ARGB.color(0.3f, CommonColors.BLUE)).toPopover(buttons::getRectangle));
            });

            root.child(FZLayouts.flexHorizontal().spacing(8), root.flexChildHorizontalSettings()).also(footer -> {
                footer.justifyContents(Justification.CENTER);

                footer.child(FZButton.builder()
                        .message(CommonComponents.GUI_CANCEL)
                        .bigWidth()
                        .onPress(this::onClose)
                        .build());

                footer.child(FZButton.builder()
                        .message(CommonComponents.GUI_DONE)
                        .bigWidth()
                        .onPress(this::onClose)
                        .build());

                collector.renderableOnly(Renderables.fill(ARGB.color(0.3f, CommonColors.BLUE)).toPopover(footer::getRectangle));
            });
        });

        this.rootLayout = FZLayouts.composer(this, rootLayout)
                .padded(8)
                .arrange()
                .clamped()
                .arrange()
                .get();
        this.rootLayout.visitWidgets(collector::renderableWidget);
    }

    @Override
    protected void repositionElements() {
        if (rootLayout != null) {
            rootLayout.fidgetz$setSize(width, height);
            rootLayout.arrangeElements();
        } else {
            super.repositionElements();
        }
    }

    record State(int itemCount, OptionEnum option, boolean toggled) {
        static State defaults() {
            return new State(0, OptionEnum.A, true);
        }

        State {
            itemCount = Math.max(0, itemCount);
        }

        State withCount(int count) {
            return new State(count, option, toggled);
        }

        State withOption(OptionEnum option) {
            return new State(itemCount, option, toggled);
        }

        State withToggle(boolean toggled) {
            return new State(itemCount, option, toggled);
        }
    }

    private enum OptionEnum {
        A,
        B,
        C,
        D,
        E,
        F,
        G;

        private static final OptionEnum[] OPTIONS = OptionEnum.values();

        OptionEnum previous() {
            return OPTIONS[(ordinal() - 1 + OPTIONS.length) % OPTIONS.length];
        }

        OptionEnum next() {
            return OPTIONS[(ordinal() + 1) % OPTIONS.length];
        }
    }
}
