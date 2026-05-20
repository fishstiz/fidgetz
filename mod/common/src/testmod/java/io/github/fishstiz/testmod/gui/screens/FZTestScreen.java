package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import io.github.fishstiz.fidgetz.v0.gui.layouts.Justification;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;

public class FZTestScreen extends FZScreen {
    public FZTestScreen() {
        super(Component.literal("FZTestScreen"));
    }

    @Override
    protected void onInitialize(GuiComponentCollector collector) {
        collector.renderableOnly(Renderables.fill(ARGB.color(0.5f, CommonColors.BLACK)).toRenderable(this.getRectangle()));

        FZLayouts.flexVertical(this).spacing(8).also(root -> {
            collector.renderableOnly(Renderables.fill(ARGB.color(0.2f, CommonColors.GREEN)).toPopover(root::getRectangle));
            root.addChild(FZLayouts.flexHorizontal().spacing(8), root.flexChildHorizontalSettings()).also(header -> {
                header.justifyContents(Justification.SPACE_EVENLY);
                header.alignContents(Justification.END);

                StringWidget titleWidget = header.addChild(new StringWidget(title, font), header.flexChildVerticalSettings());
                header.addChild(FZButton.builder().size(20, 20).build());
                header.addChild(FZButton.builder().size(50, 20).build());
                header.addChild(FZButton.builder().size(20, 10).build());
                collector.renderableOnly(Renderables.fill(ARGB.color(0.3f, CommonColors.BLUE)).toPopover(titleWidget::getRectangle));
                collector.renderableOnly(Renderables.outline(CommonColors.RED).toPopover(header::getRectangle));
            });
            root.addChild(FZLayouts.flexVertical().spacing(8), root.flexChildSettings()).also(body -> {
                body.addChild(Button.builder(Component.literal("Top Align"), btn -> IO.println(btn.getMessage())).build());
                body.addSpacer(body.newChildSettings().flexMain());
                body.addChild(Button.builder(Component.literal("Center Align"), btn -> IO.println(btn.getMessage())).build());
                body.addChild(Button.builder(Component.literal("Center Align"), btn -> IO.println(btn.getMessage())).build());
                body.addChild(Button.builder(Component.literal("Center Align"), btn -> IO.println(btn.getMessage())).build());
                body.addSpacer(body.newChildSettings().flexMain());
                body.addChild(Button.builder(Component.literal("Bot Align"), btn -> IO.println(btn.getMessage())).build());
                collector.renderableOnly(Renderables.outline(CommonColors.RED).toPopover(body::getRectangle));
            });
            root.addChild(FZLayouts.flexHorizontal(), root.flexChildHorizontalSettings()).also(footer -> {
                footer.justifyContents(Justification.CENTER);
                footer.addChild(Button.builder(Component.literal("Close"), _ -> onClose()).build());
                collector.renderableOnly(Renderables.outline(CommonColors.RED).toPopover(footer::getRectangle));
            });

            FZLayouts.composer(this, root)
                    .padded(8)
                    .clamped()
                    .arrange()
                    .get()
                    .visitWidgets(collector::renderableWidget);
        });
    }
}
