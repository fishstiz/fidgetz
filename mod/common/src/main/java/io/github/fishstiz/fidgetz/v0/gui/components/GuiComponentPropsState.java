package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.utils.FunctionUtils;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class GuiComponentPropsState {
    public @Nullable String id;
    public boolean focusOnInteraction = true;
    public @Nullable RenderableRectangle overlay = null;
    public Consumer<FZContextMenuEntry.Collector> contextEntries = FunctionUtils.nopConsumer();

    public GuiComponentPropsState() {
    }

    private void apply(GuiComponentProps props) {
        props.id().ifPresent(id -> this.id = id);
        GuiComponentPropsBase.ifNonDefault(props.focusOnInteraction(), f -> this.focusOnInteraction = f.toBoolean(true));
        props.overlay().ifDefined(overlay -> this.overlay = overlay);
        props.contextEntries().ifPresent(entries -> this.contextEntries = entries.value());
    }

    public void apply(AbstractWidget widget, GuiComponentProps props) {
        props.width().ifPresent(widget::setWidth);
        props.height().ifPresent(widget::setHeight);
        GuiComponentPropsBase.ifNonDefault(props.active(), a -> widget.active = a.toBoolean(true));
        GuiComponentPropsBase.ifNonDefault(props.visible(), a -> widget.visible = a.toBoolean(true));
        props.tooltip().ifDefined(widget::setTooltip);
        props.message().ifPresent(widget::setMessage);
        props.tabOrderGroup().ifPresent(widget::setTabOrderGroup);
        apply(props);
    }
}
