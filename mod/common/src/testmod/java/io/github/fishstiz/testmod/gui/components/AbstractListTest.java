package io.github.fishstiz.testmod.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.components.FZAbstractListWidget;
import io.github.fishstiz.fidgetz.v0.gui.components.FZButton;
import io.github.fishstiz.fidgetz.v0.gui.components.FZTextField;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class AbstractListTest extends FZAbstractListWidget<AbstractListTest.Entry> {
    public AbstractListTest() {
        for (int i = 0; i < 20; i++) {
            addEntry(new Entry(i));
        }
        repositionEntries();
    }

    @Override
    protected int maxContentWidth() {
        return 0;
    }

    @Override
    protected int rowSpacing() {
        return 8;
    }

    @Override
    protected int contentPaddingLeft() {
        return 8;
    }

    @Override
    protected int contentPaddingRight() {
        return 8;
    }

    @Override
    public double scrollRate() {
        return 20;
    }

    class Entry extends FZAbstractListWidget.Entry {
        private final FZFlexLayout layout = FZLayouts.flexHorizontal();
        private final List<AbstractWidget> children = new ArrayList<>();

        private Entry(int index) {
            super(20);
            layout.child(FZTextField.builder().text(Integer.toString(index)).build(), layout.flexChildHorizontalSettings());
            layout.child(FZButton.builder().message(Component.literal("-")).square().build());
            layout.arrangeElements();
            layout.visitWidgets(children::add);
        }

        @Override
        protected int getMarginTop() {
            return 20;
        }

        @Override
        protected int getMarginBottom() {
            return getIndex() == AbstractListTest.this.children().size() - 1 ? 20 : 0;
        }

        @Override
        protected void setWidth(int width) {
            super.setWidth(width);
            layout.fidgetz$setWidth(width);
        }

        @Override
        protected void setHeight(int height) {
            super.setHeight(height);
            layout.fidgetz$setHeight(height);
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            layout.setX(x);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            layout.setY(y);
        }

        @Override
        protected void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
            layout.setPosition(x, y);
            layout.fidgetz$setSize(width, height);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            for (AbstractWidget child : children) {
                child.extractRenderState(graphics, mouseX, mouseY, a);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }
    }
}
