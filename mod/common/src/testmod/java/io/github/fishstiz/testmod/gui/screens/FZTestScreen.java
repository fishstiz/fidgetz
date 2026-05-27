package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZComposedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
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
    protected void collectChildren(GuiComponentCollector collector) {
        collector.renderableOnly(Renderables.fill(ARGB.color(0.5f, CommonColors.BLACK)).toRenderable(this.getRectangle()));

        FZFlexLayout.vertical(this).spacing(8).also(root -> {
            collector.renderableOnly(Renderables.fill(ARGB.color(0.2f, CommonColors.GREEN)).toPopover(root::getRectangle));
            root.child(FZFlexLayout.horizontal().spacing(8), root.flexChildHorizontalSettings()).also(header -> {
                header.justifyContents(Justification.SPACE_EVENLY);
                header.alignContents(Justification.END);

                StringWidget titleWidget = header.child(new StringWidget(title, font), header.flexChildVerticalSettings());
                header.child(FZButton.builder().size(20, 20).build());
                header.child(FZButton.builder().size(50, 20).build());
                header.child(FZButton.builder().size(20, 10).build());
                collector.renderableOnly(Renderables.fill(ARGB.color(0.3f, CommonColors.BLUE)).toPopover(titleWidget::getRectangle));
                collector.renderableOnly(Renderables.outline(CommonColors.RED).toPopover(header::getRectangle));
            });
            root.child(FZFlexLayout.vertical().spacing(8), root.flexChildSettings()).also(body -> {
                body.child(Button.builder(Component.literal("Top Align"), btn -> IO.println(btn.getMessage())).build());
                body.spacer(body.newChildSettings().flexMain());
                body.child(Button.builder(Component.literal("Center Align"), btn -> IO.println(btn.getMessage())).build());
                body.child(Button.builder(Component.literal("Center Align"), btn -> IO.println(btn.getMessage())).build());
                body.child(Button.builder(Component.literal("Center Align"), btn -> IO.println(btn.getMessage())).build());
                body.spacer(body.newChildSettings().flexMain());
                body.child(Button.builder(Component.literal("Bot Align"), btn -> IO.println(btn.getMessage())).build());
                collector.renderableOnly(Renderables.outline(CommonColors.RED).toPopover(body::getRectangle));
            });
            root.child(FZFlexLayout.horizontal(), root.flexChildHorizontalSettings()).also(footer -> {
                footer.justifyContents(Justification.CENTER);
                footer.child(Button.builder(Component.literal("Close"), _ -> onClose()).build());
                collector.renderableOnly(Renderables.outline(CommonColors.RED).toPopover(footer::getRectangle));
            });

            FZComposedLayout.contain(this, root)
                    .padding(8)
                    .clamp()
                    .arrange()
                    .get()
                    .visitWidgets(collector::renderableWidget);
        });
    }
}
