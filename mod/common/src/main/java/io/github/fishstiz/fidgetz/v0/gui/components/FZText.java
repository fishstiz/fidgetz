package io.github.fishstiz.fidgetz.v0.gui.components;

import io.github.fishstiz.fidgetz.v0.gui.state.FZRef;
import io.github.fishstiz.fidgetz.v0.utils.ScreenRectangleUtils;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class FZText extends StringWidget implements FZComponent, FZContextMenuEntry.Source {
    private final GuiComponentPropsState propsState = new GuiComponentPropsState();
    private ScreenRectangle bounds;

    FZText(Component message) {
        super(message, Minecraft.getInstance().font);
        bounds = super.getRectangle();
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenuEntry.Collector collector) {
        propsState.contextEntries.accept(collector);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        if (isFocused()) {
            graphics.outline(getX(), getY(), getWidth(), getHeight(), ARGB.white(getAlpha()));
        }
        if (propsState.overlay != null) {
            propsState.overlay.extractRenderState(graphics, getX(), getY(), getWidth(), getHeight(), mouseX, mouseY, a);
        }
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return propsState.focusOnNavigation;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

    @Override
    public void setMessage(Component message) {
        this.message = message;
    }

    @Override
    public ScreenRectangle getRectangle() {
        if (ScreenRectangleUtils.unequal(bounds, this)) {
            bounds = super.getRectangle();
        }
        return bounds;
    }

    @Override
    public @Nullable String fidgetz$componentId() {
        return propsState.id;
    }

    void applyProps(Props props) {
        propsState.apply(this, props);
        props.maxWidth().ifPresent(pair -> setMaxWidth(pair.leftInt(), pair.right()));
    }

    public static FZText bind(String key, FZRef<Props> ref) {
        Props props = ref.value();
        FZText text = new FZText(props.message().orElse(CommonComponents.EMPTY));
        text.applyProps(props);
        if (props.width().isEmpty()) {
            text.setWidth(text.getFont().width(text.getMessage()));
        }
        ref.subscribe(key, text::applyProps);
        return text;
    }

    public static Builder builder(Component message) {
        return new Builder(message);
    }

    public interface Props extends GuiComponentProps {
        default Optional<IntObjectPair<TextOverflow>> maxWidth() {
            return Optional.empty();
        }
    }

    private static final class PropsImpl extends GuiComponentPropsBase implements Props {
        private final @Nullable IntObjectPair<TextOverflow> maxWidth;

        private PropsImpl(GuiComponentProps props, @Nullable IntObjectPair<TextOverflow> maxWidth) {
            super(props);
            this.maxWidth = maxWidth;
        }

        @Override
        public Optional<IntObjectPair<TextOverflow>> maxWidth() {
            return Optional.ofNullable(maxWidth);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Props other)) return false;
            return super.equals(o) && Objects.equals(maxWidth(), other.maxWidth());
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), maxWidth);
        }
    }

    public static final class Builder extends GuiComponentPropsBuilder<Builder> {
        private @Nullable IntObjectPair<TextOverflow> maxWidth;

        private Builder(Component message) {
            props.message = message;
        }

        public Builder maxWidth(int maxWidth, TextOverflow overflow) {
            this.maxWidth = IntObjectPair.of(maxWidth, overflow);
            return this;
        }

        public Builder maxWidth(int maxWidth) {
            this.maxWidth = this.maxWidth == null
                    ? IntObjectPair.of(maxWidth, TextOverflow.CLAMPED)
                    : IntObjectPair.of(maxWidth, this.maxWidth.right());

            return this;
        }

        public Props toProps() {
            return new PropsImpl(props, maxWidth);
        }

        public FZText build() {
            FZText text = new FZText(props.message().orElse(CommonComponents.EMPTY));
            Props props = toProps();
            text.applyProps(props);
            if (props.width().isEmpty()) {
                text.setWidth(text.getFont().width(text.getMessage()));
            }
            return text;
        }
    }
}
