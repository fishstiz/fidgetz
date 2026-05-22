package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.Fidgetz;
import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import io.github.fishstiz.fidgetz.v0.gui.layouts.Justification;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public class FlexWrapScreen extends FZScreen {
    public FlexWrapScreen() {
        super(CommonComponents.EMPTY);
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
        FZLayouts.flexHorizontal(this).spacing(4).maxWidth(200).wrap().also(root -> {
            root.justifyContents(Justification.SPACE_EVENLY);
            root.alignContents(Justification.SPACE_EVENLY);

            for (int i = 1; i <= 13; i++) {
                root.child(FZButton.builder()
                        .message(Component.literal(String.valueOf(i)))
                        .onPress(e -> Fidgetz.LOG.info(e.target().getMessage().getString()))
                        .size(60, 20)
                        .build());
            }

            root.spacer(root.flexChildHorizontalSettings());
            root.child(
                    FZButton.builder()
                            .message(Component.literal("flex-h"))
                            .onPress(e -> Fidgetz.LOG.info(e.target().getMessage().getString()))
                            .build(),
                    root.flexChildHorizontalSettings()
                            .minFlexWidth(70)
                            .maxFlexWidth(100)
            );
            root.child(
                    FZButton.builder()
                            .message(Component.literal("flex-v"))
                            .onPress(e -> Fidgetz.LOG.info(e.target().getMessage().getString()))
                            .size(100, 40)
                            .build(),
                    root.flexChildVerticalSettings().alignHorizontallyLeft()
            );
            root.child(
                    FZButton.builder()
                            .message(Component.literal("fixed"))
                            .onPress(e -> Fidgetz.LOG.info(e.target().getMessage().getString()))
                            .size(60, 20)
                            .build(),
                    root.newChildSettings().alignVerticallyMiddle()
            );

            FZLayouts.composer(this, root)
                    .padded(8)
                    .centered()
                    .clamped()
                    .arrange()
                    .get()
                    .visitWidgets(collector::renderableWidget);

            collector.renderableOnly(Renderables.outline(CommonColors.RED).toRenderable(root::getRectangle));
        });
    }
}
