package io.github.fishstiz.testmod.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.components.FZDialogContainer;
import io.github.fishstiz.fidgetz.v0.gui.components.FZContextMenu;
import io.github.fishstiz.fidgetz.v0.gui.components.FZContextMenuEntry;
import io.github.fishstiz.fidgetz.v0.gui.components.events.FZHoverableContainer;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

import static io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables.*;

public class FlexScreen extends Screen implements FZDialogContainer, FZHoverableContainer, FZContextMenuEntry.Source {
    private @Nullable FZContextMenu contextMenu;

    public FlexScreen() {
        super(CommonComponents.EMPTY);
    }

    private static Button btn(String message) {
        return Button.builder(Component.literal(message), btn -> IO.println(btn.getMessage())).build();
    }

    private static Button.Builder btnBuilder(String message) {
        return Button.builder(Component.literal(message), btn -> IO.println(btn.getMessage()));
    }

    @Override
    protected void init() {
        GuiComponentCollector collector = new GuiComponentCollector();
        contextMenu = collector.renderableWidget(FZContextMenu.builder(this).build());

        FZFlexLayout rootLayout = FZLayouts.flexVertical(this).spacing(8).also(root -> {
            root.addChild(FZLayouts.flexHorizontal().spacing(8), root.flexChildHorizontalSettings()).also(header -> {
                header.addChild(btn("header-1:flex-h"), header.flexChildHorizontalSettings());
                header.addChild(btn("header-2:flex-h"), header.flexChildHorizontalSettings());
                header.addChild(btnBuilder("header-3").size(70, 20).build());
                header.addChild(btn("header-4:flex-h"), header.flexChildHorizontalSettings());
                header.addChild(btn("header-5:flex-h"), header.flexChildHorizontalSettings());
                collector.renderableOnly(fill(ARGB.color(0.2f, CommonColors.GREEN)).toPopover(header::getRectangle));
            });
            root.addChild(FZLayouts.flexVertical(this).spacing(8), root.flexChildSettings()).also(body -> {
                body.addChild(btn("body-1:flex-both"), body.flexChildSettings());
                body.addChild(btn("body-2:flex-h"), body.flexChildHorizontalSettings());
                body.addChild(FZLayouts.flexHorizontal().spacing(8), body.flexChildSettings()).also(center -> {
                    center.addChild(FZLayouts.flexVertical().spacing(8), center.flexChildSettings()).also(cLeft -> {
                        cLeft.addChild(btn("cLeft-1"));
                        cLeft.addChild(btn("cLeft-2:flex-both"), cLeft.flexChildSettings());
                        collector.renderableOnly(outline(CommonColors.RED).toPopover(cLeft::getRectangle, 1));
                    });
                    center.addChild(FZLayouts.flexVertical().spacing(8), center.flexChildSettings()).also(cRight -> {
                        cRight.addChild(btn("cRight-1:flex-v"), cRight.flexChildSettings());
                        cRight.addChild(btn("cRight-2:flex-h"), cRight.flexChildHorizontalSettings());
                        collector.renderableOnly(outline(CommonColors.RED).toPopover(cRight::getRectangle));
                    });
                    collector.renderableOnly(outline(CommonColors.BLUE).toPopover(center::getRectangle));
                });
                body.addChild(FZLayouts.flexHorizontal().spacing(8), body.flexChildSettings()).also(bBtm -> {
                    bBtm.addChild(btn("bBtm-btm-1:flex-v"), bBtm.flexChildVerticalSettings());
                    bBtm.addSpacer(bBtm.flexChildSettings());
                    bBtm.addChild(btn("bBtm-btm-2:flex-h"), bBtm.flexChildHorizontalSettings());
                    collector.renderableOnly(outline(CommonColors.BLUE).toPopover(bBtm::getRectangle));
                });
                collector.renderableOnly(fill(ARGB.color(0.2f, CommonColors.GREEN)).toPopover(body::getRectangle));
            });
            root.addChild(FZLayouts.flexHorizontal().spacing(8), root.flexChildHorizontalSettings()).also(footer -> {
                footer.addChild(FZLayouts.flexHorizontal().spacing(8), footer.flexChildHorizontalSettings()).also(fLeft -> {
                    fLeft.addChild(btn("fLeft-1:flex-h"), fLeft.flexChildHorizontalSettings());
                    fLeft.addChild(btnBuilder("fLeft-2").size(40, 20).build());
                    fLeft.addChild(btn("fLeft-3:flex-h"), fLeft.flexChildHorizontalSettings());
                    collector.renderableOnly(outline(CommonColors.BLUE).toPopover(fLeft::getRectangle));
                });
                footer.addChild(btn("fRight-1:flex-h"), footer.flexChildHorizontalSettings());
                collector.renderableOnly(fill(ARGB.color(0.2f, CommonColors.GREEN)).toPopover(footer::getRectangle));
            });
        });

        FZLayouts.composer(this, rootLayout)
                .padded(8)
                .centered()
                .clamped()
                .arrange()
                .get()
                .visitWidgets(collector::renderableWidget);

        collector.flushTo(this::addWidget, this::addRenderableOnly);
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenuEntry.Collector collector) {
        collector.addEntry(FZContextMenuEntry.builder()
                .message(Component.literal("Hello World!"))
                .onClick(() -> IO.println("Hello World!"))
                .build());
        collector.nextSection();
        collector.addEntry(FZContextMenuEntry.builder()
                .message(Component.literal("Goodbye World!"))
                .onClick(() -> IO.println("Goodbye World!"))
                .build());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (fidgetz$captureEventForDialogs(event)) {
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && contextMenu != null) {
            contextMenu.open(event.x(), event.y(), fidgetz$collectContextEntries(event.x(), event.y()));
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        fidgetz$updateHovered(mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }
}
