package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class Screenz extends FZScreen {
    private final FZMutableRef<@Nullable String> active = new FZMutableRef<>(null);
    private final FZMutableRef<State> state = new FZMutableRef<>(new State(0, 0));
    private final FZMutableRef<String> text = new FZMutableRef<>("");

    public Screenz() {
        super(CommonComponents.EMPTY);
    }

    private record State(int a, int b) {
        State incrementA() {
            return new State(a + 1, b);
        }

        State decrementA() {
            return new State(a - 1, b);
        }

        State incrementB() {
            return new State(a, b + 1);
        }

        State decrementB() {
            return new State(a, b - 1);
        }
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
        collector.renderableWidget(FZButton.bind("deactivate", active.map(s -> FZButton.builder()
                .active(s != null)
                .message(Component.literal("Deactivate"))
                .onPress(() -> active.set((String) null))
                .toProps())));
        collector.renderableWidget(FZButton.bind("activate", active.map(s -> FZButton.builder()
                .active(s == null)
                .message(Component.literal("Activate"))
                .onPress(() -> active.set(""))
                .toProps()))).setPosition(0, 40);

        FZFlexLayout layout = FZFlexLayout.vertical();
        {
            FZFlexLayout aLayout = layout.child(FZFlexLayout.horizontal());
            aLayout.child(FZText.bind("A", state.map(State::a).map(a -> FZText
                    .builder(Component.literal("A: " + a))
                    .toProps())));
            aLayout.child(FZButton.builder()
                    .square()
                    .message(Component.literal("-"))
                    .onPress(() -> state.set(State::decrementA))
                    .build());
            aLayout.child(FZButton.builder()
                    .square()
                    .message(Component.literal("+"))
                    .onPress(() -> state.set(State::incrementA))
                    .build());
        }
        {
            FZFlexLayout bLayout = layout.child(FZFlexLayout.horizontal());
            bLayout.child(FZText.bind("B", state.map(State::b).map(b -> FZText
                    .builder(Component.literal("B: " + b))
                    .toProps())));
            bLayout.child(FZButton.builder()
                    .square()
                    .message(Component.literal("-"))
                    .onPress(() -> state.set(State::decrementB))
                    .build());
            bLayout.child(FZButton.builder()
                    .square()
                    .message(Component.literal("+"))
                    .onPress(() -> state.set(State::incrementB))
                    .build());
        }

        layout.child(new WrappedComponent(FZTextField.bind("text", text.map(value -> FZTextField.builder()
                .text(value)
                .onChange(e -> text.set(e.value()))
                .toProps()))));
        layout.child(FZTextField.builder().build());

        layout.arrangeElements();
        layout.setPosition(0, 60);
        layout.visitWidgets(collector::renderableWidget);
    }
}
