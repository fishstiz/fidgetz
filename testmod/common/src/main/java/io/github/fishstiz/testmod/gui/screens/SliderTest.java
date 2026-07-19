package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.FZSlider;
import io.github.fishstiz.fidgetz.v0.gui.components.GuiComponentCollector;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SliderTest extends FZScreen {
    private final FZMutableRef<Double> value = new FZMutableRef<>(1d);
    private final FZMutableRef<Double> fixedValue = new FZMutableRef<>(2.5d);

    public SliderTest() {
        super(CommonComponents.EMPTY);
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
        FZFlexLayout layout = FZFlexLayout.vertical();
        layout.child(FZSlider.builder().build());
        layout.child(FZSlider.builder()
                .label(Component.literal("Label"))
                .build());
        layout.child(FZSlider.builder()
                .label(Component.literal("Min"))
                .min(0.5)
                .build());
        layout.child(FZSlider.builder()
                .label(Component.literal("Max"))
                .max(0.5)
                .build());
        layout.child(FZSlider.builder()
                .label(Component.literal("Value"))
                .value(0.5)
                .build());
        layout.child(FZSlider.builder()
                .label(Component.literal("Step"))
                .step(0.2)
                .build());
        layout.child(FZSlider.builder()
                .label(Component.literal("All"))
                .min(1)
                .max(5)
                .step(0.5)
                .value(3)
                .build());
        layout.child(FZSlider.builder()
                .label(Component.literal("Negative Min"))
                .min(-1)
                .value(-0.9)
                .step(0.1)
                .build());

        layout.child(FZSlider.bind("FixedScaleSlider", fixedValue.map(value -> FZSlider.builder()
                .label(Component.literal("Fixed Value"))
                .step(0.5)
                .max(5)
                .value(value)
                .toProps())));

        layout.child(FZSlider.builder()
                .label(Component.literal("Unreactive Value"))
                .max(8)
                .step(1)
                .value(value.value())
                .onChange(this::handleScaleChange)
                .onFormat(this::handleScaleFormat)
                .build());
        layout.child(FZSlider.bind("ScaleSlider", value.map(value -> FZSlider.builder()
                .label(Component.literal("Reactive Value"))
                .max(8)
                .step(1)
                .value(value)
                .onChange(this::handleScaleChange)
                .onFormat(this::handleScaleFormat)
                .toProps())));

        layout.arrangeElements();
        layout.visitWidgets(collector::renderableWidget);
    }

    private void handleScaleFormat(FZSlider.FormatEvent event) {
        if (event.target().getValue() == 0) {
            event.format(Component.literal("Auto"));
        }
    }

    private void handleScaleChange(FZSlider.ChangeEvent event) {
        value.set(event.value());
    }
}
