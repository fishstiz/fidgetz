package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZComposedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZAutoGridLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.Justification;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import java.util.Arrays;

public class GridScreen extends FZScreen {
    private Justification justification = Justification.START;
    private Justification alignment = Justification.START;

    public GridScreen() {
        super(CommonComponents.EMPTY);
    }

    private static FZIcon createSquare() {
        return FZIcon.builder(Renderables.fill(CommonColors.RED))
                .size(27, 27)
                .build();
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
        FZFlexLayout root = FZFlexLayout.vertical(this).spacing(8);

        {
            FZFlexLayout opts = root.child(
                    FZFlexLayout.horizontal().spacing(8),
                    root.flexChildHorizontalSettings()
            );

            opts.child(
                    FZDropdown.builder(this)
                            .message(Component.literal("justify: " + this.justification.name()))
                            .entries(Arrays.stream(Justification.values())
                                    .map(j -> FZPopoverMenuItem.fromWidget(FZButton.builder()
                                            .sprites(null)
                                            .message(Component.literal(j.name()))
                                            .leftAlignedMessage()
                                            .onPress(() -> {
                                                if (this.justification != j) {
                                                    this.justification = j;
                                                    rebuildWidgets();
                                                }
                                            })
                                            .build()))
                                    .toList())
                            .build(),
                    opts.flexChildHorizontalSettings()
            );

            opts.child(
                    FZDropdown.builder(this)
                            .message(Component.literal("align: " + this.alignment.name()))
                            .entries(Arrays.stream(Justification.values())
                                    .map(j -> FZPopoverMenuItem.fromWidget(FZButton.builder()
                                            .sprites(null)
                                            .leftAlignedMessage()
                                            .message(Component.literal(j.name()))
                                            .onPress(() -> {
                                                if (this.alignment != j) {
                                                    this.alignment = j;
                                                    rebuildWidgets();
                                                }
                                            })
                                            .build()))
                                    .toList())
                            .build(),
                    opts.flexChildHorizontalSettings()
            );
        }

        {
            FZFlexLayout grids = root.child(FZFlexLayout.horizontal().spacing(8), root.flexChildSettings());
            grids.defaultChildSettings().flexBoth();

            {
                FZAutoGridLayout vertical = grids.child(FZAutoGridLayout.vertical().rowSpacing(16).colSpacing(8));
                vertical.justifyContents(this.justification).alignContents(this.alignment);

                for (int i = 0; i < 20; i++) {
                    vertical.child(createSquare());
                }

                collector.renderableOnly(Renderables.fill(0x7F00FF00).toPopover(vertical::getRectangle));
            }

            {
                FZAutoGridLayout horizontal = grids.child(FZAutoGridLayout.horizontal().rowSpacing(8).colSpacing(16));
                horizontal.justifyContents(this.justification).alignContents(this.alignment);

                for (int i = 0; i < 20; i++) {
                    horizontal.child(createSquare());
                }

                collector.renderableOnly(Renderables.fill(0x7F0000FF).toPopover(horizontal::getRectangle));
            }
        }

        FZComposedLayout.contain(this, root)
                .padding(8)
                .center()
                .clamp()
                .arrange()
                .visitWidgets(collector::renderableWidget);
    }
}
