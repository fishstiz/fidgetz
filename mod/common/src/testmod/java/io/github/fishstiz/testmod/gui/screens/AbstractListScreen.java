package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.FZText;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZComposedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import io.github.fishstiz.testmod.gui.components.AbstractListTest;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AbstractListScreen extends FZScreen {
    public AbstractListScreen() {
        super(Component.literal("AbstractListScreen"));
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
        FZComposedLayout.contain(this, FZFlexLayout.vertical(this).spacing(8).also(root -> {
            root.child(FZText.builder(title).build(), root.newChildSettings().alignHorizontallyCenter());
            root.child(new AbstractListTest(), root.flexChildSettings());
            root.child(
                    FZButton.builder().bigWidth().message(CommonComponents.GUI_DONE).onPress(this::onClose).build(),
                    root.newChildSettings().alignHorizontallyCenter()
            );
        })).padding(0, 8, 0, 8).center().clamp().arrange().visitWidgets(collector::renderableWidget);
    }
}
