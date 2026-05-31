package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.layouts.*;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public class ListScreen extends FZScreen {
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("recipe_book/tab"), Identifier.withDefaultNamespace("recipe_book/tab_selected")
    );
    private static final WidgetSprites SPRITES2 = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/cross_button"), Identifier.withDefaultNamespace("widget/cross_button_highlighted")
    );
    private final FZMutableRef<State> state = new FZMutableRef<>(State.defaults());
    private final FZMutableRef<String> textFieldState = new FZMutableRef<>("");
    private final FZMutableRef<String> textFieldState2 = new FZMutableRef<>("");
    private final FZMutableRef<String> textFieldState3 = new FZMutableRef<>("uneditable");
    private @Nullable FZLayout rootLayout;

    public ListScreen() {
        super(Component.literal("List Screen"));
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {
        this.rootLayout = FZComposedLayout.contain(this, FZFlexLayout.vertical(this).spacing(8).also(root -> {
            root.child(FZFlexLayout.horizontal().spacing(8), root.flexChildHorizontalSettings()).also(header -> {
                header.justifyContents(Justification.CENTER).alignContents(Justification.CENTER).defaultChildSettings().flexCross();

                header.child(FZText.builder(title).build());
                header.child(FZButton.bind("decrement2", state.map(State::count2).map(count -> FZButton.builder()
                        .square()
                        .message(Component.literal("-"))
                        .active(count > 0)
                        .onPress(() -> state.set(prev -> prev.withCount2(prev.count2 - 1)))
                        .toProps())));
                header.child(FZButton.builder()
                        .square()
                        .message(Component.literal("+"))
                        .onPress(() -> state.set(prev -> prev.withCount2(prev.count2 + 1)))
                        .build());
                header.child(FZText.bind("count2", state.map(State::count2).map(count ->
                        FZText.builder(Component.literal("Count2: " + count)).toProps()
                )));
            });

            root.child(FZLayoutList.bind("list", state
                    .map(State::quantity)
                    .map(q -> FZLayoutList.builder().contentPadding(8).onRefresh(refreshEvent -> {
                        FZFlexLayout layout = refreshEvent.layout();
                        FZLayoutList list = refreshEvent.target();
                        refreshEvent.flushComponents();

                        layout.spacing(8).defaultChildSettings().flexCross();

                        layout.child(FZButton.builder()
                                .message(Component.literal("Disabled Button"))
                                .leftAlignedMessage()
                                .inactive()
                                .build());
                        layout.child(FZButton.builder()
                                .message(Component.literal("Disabled Button"))
                                .inactive()
                                .build());

                        layout.child(FZDropdown.builder(list)
                                .message(Component.literal("Dropdown"))
                                .entry(button -> button.message(Component.literal("Option 1")))
                                .entry(button -> button.message(Component.literal("Option 2")))
                                .entry(button -> button.message(Component.literal("Option 3")))
                                .entry(button -> button.message(Component.literal("Option 4")))
                                .entry(button -> button.message(Component.literal("Option 5")))
                                .build());

                        layout.child(FZDropdown.builder(list)
                                .message(Component.literal("Dropdown 2"))
                                .inactive()
                                .entry(button -> button.message(Component.literal("Option 1")))
                                .entry(button -> button.message(Component.literal("Option 2")))
                                .entry(button -> button.message(Component.literal("Option 3")))
                                .entry(button -> button.message(Component.literal("Option 4")))
                                .entry(button -> button.message(Component.literal("Option 5")))
                                .build());

                        layout.child(FZFlexLayout.horizontal()).also(btns -> {
                            btns.spacing(4).justifyContents(Justification.SPACE_BETWEEN);

                            btns.child(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                                    }, false)
                                    .sprite(Identifier.withDefaultNamespace("icon/unseen_notification"), 16, 16)
                                    .size(20, 20)
                                    .build()).active = false;
                            btns.child(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                                    }, false)
                                    .sprite(Identifier.withDefaultNamespace("icon/accessibility"), 16, 16)
                                    .size(20, 20)
                                    .build()).active = false;
                            btns.child(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                                    }, false)
                                    .sprite(Identifier.withDefaultNamespace("icon/language"), 16, 16)
                                    .size(20, 20)
                                    .build()).active = false;

                            btns.child(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                                    }, false)
                                    .sprite(Identifier.withDefaultNamespace("icon/unseen_notification"), 16, 16)
                                    .size(20, 20)
                                    .build());
                            btns.child(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                                    }, false)
                                    .sprite(Identifier.withDefaultNamespace("icon/accessibility"), 16, 16)
                                    .size(20, 20)
                                    .build());
                            btns.child(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                                    }, false)
                                    .sprite(Identifier.withDefaultNamespace("icon/language"), 16, 16)
                                    .size(20, 20)
                                    .build());
                        });

                        layout.child(FZFlexLayout.horizontal()).also(btns -> {
                            btns.spacing(4).justifyContents(Justification.SPACE_BETWEEN);

                            btns.child(FZIconButton.builder()
                                    .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/unseen_notification"), 16, 16))
                                    .size(20, 20)
                                    .build()).active = false;
                            btns.child(FZIconButton.builder()
                                    .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/accessibility"), 16, 16))
                                    .size(20, 20)
                                    .build()).active = false;
                            btns.child(FZIconButton.builder()
                                    .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/language"), 16, 16))
                                    .size(20, 20)
                                    .build()).active = false;

                            btns.child(FZIconButton.builder()
                                    .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/unseen_notification"), 16, 16))
                                    .size(20, 20)
                                    .build());
                            btns.child(FZIconButton.builder()
                                    .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/accessibility"), 16, 16))
                                    .size(20, 20)
                                    .build());
                            btns.child(FZIconButton.builder()
                                    .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/language"), 16, 16))
                                    .size(20, 20)
                                    .build());
                        });

                        layout.child(FZFlexLayout.horizontal()).also(btns -> {
                            btns.spacing(4).justifyContents(Justification.SPACE_BETWEEN);

                            btns.child(FZIconButton.builder(Identifier.withDefaultNamespace("icon/unseen_notification"))
                                    .size(20, 20)
                                    .build()).active = false;
                            btns.child(FZIconButton.builder(Identifier.withDefaultNamespace("icon/accessibility"))
                                    .size(20, 20)
                                    .build()).active = false;
                            btns.child(FZIconButton.builder(Identifier.withDefaultNamespace("icon/language"))
                                    .size(20, 20)
                                    .build()).active = false;

                            btns.child(FZIconButton.builder(Identifier.withDefaultNamespace("icon/unseen_notification"))
                                    .size(20, 20)
                                    .build());
                            btns.child(FZIconButton.builder(Identifier.withDefaultNamespace("icon/accessibility"))
                                    .size(20, 20)
                                    .build());
                            btns.child(FZIconButton.builder(Identifier.withDefaultNamespace("icon/language"))
                                    .size(20, 20)
                                    .build());
                        });

                        layout.child(FZFlexLayout.horizontal()).also(btns -> {
                            btns.spacing(4).justifyContents(Justification.SPACE_BETWEEN);

                            RenderableRectangle whiteOutline = Renderables.outline(CommonColors.WHITE);
                            RenderableRectangle unseenNotification = Renderables.sprite(Identifier.withDefaultNamespace("icon/unseen_notification"));
                            RenderableRectangle accessibility = Renderables.sprite(Identifier.withDefaultNamespace("icon/accessibility"));
                            RenderableRectangle language = Renderables.sprite(Identifier.withDefaultNamespace("icon/language"));

                            btns.child(FZIconButton
                                    .builder(new WidgetRenderables(unseenNotification, unseenNotification, unseenNotification.then(whiteOutline)))
                                    .size(20, 20)
                                    .build()).active = false;
                            btns.child(FZIconButton
                                    .builder(new WidgetRenderables(accessibility, accessibility, accessibility.then(whiteOutline)))
                                    .size(20, 20)
                                    .build()).active = false;
                            btns.child(FZIconButton
                                    .builder(new WidgetRenderables(language, language, language.then(whiteOutline)))
                                    .size(20, 20)
                                    .build()).active = false;

                            btns.child(FZIconButton
                                    .builder(new WidgetRenderables(unseenNotification, unseenNotification, unseenNotification.then(whiteOutline)))
                                    .size(20, 20)
                                    .build());
                            btns.child(FZIconButton
                                    .builder(new WidgetRenderables(accessibility, accessibility, accessibility.then(whiteOutline)))
                                    .size(20, 20)
                                    .build());
                            btns.child(FZIconButton
                                    .builder(new WidgetRenderables(language, language, language.then(whiteOutline)))
                                    .size(20, 20)
                                    .build());
                        });

                        layout.child(FZTextField.builder().inactive().build());
                        layout.child(FZTextField.builder().hint(Component.literal("Hint...")).build());
                        layout.child(FZTextField.builder().hint(Component.literal("Hint...")).inactive().build());

                        var box = layout.child(new EditBox(Minecraft.getInstance().font, 150, 20, CommonComponents.EMPTY));
                        box.active = false;
                        box.setEditable(false);

                        layout.spacer().height(4);
                        layout.child(FZTextField.builder()
                                .onChange(e -> textFieldState.set(e.value()))
                                .hint(Component.literal("update below..."))
                                .build());
                        layout.child(FZTextField.bind("textfield", textFieldState.map(value -> FZTextField.builder()
                                .hint(Component.literal("textfieldstate"))
                                .text(value)
                                .toProps())));

                        layout.spacer().height(4);
                        layout.child(FZText.bind("text-2", textFieldState2.map(value -> FZText
                                .builder(Component.literal("textFieldState2: " + value))
                                .overlay(Renderables.fill(ARGB.color(0.2f, CommonColors.BLUE)))
                                .active()
                                .toProps())));

                        layout.child(FZTextField.bind("textfield2", textFieldState2.map(value -> FZTextField.builder()
                                .hint(Component.literal("textfieldstate2"))
                                .text(value)
                                .onChange(e -> textFieldState2.set(e.value()))
                                .toProps())));

                        layout.spacer().height(4);
                        layout.child(FZTextField.bind("textfield3", textFieldState3.map(value -> FZTextField.builder()
                                .text(value)
                                .toProps())));


                        layout.child(FZTextField.builder()
                                .text("False!")
                                .filter(value -> !value.contains("?"))
                                .build());

                        layout.spacer().height(4);

                        layout.child(FZText.builder(Component.literal("Short entry")).active().height(20).build());
                        layout.child(FZText.builder(Component.literal("Medium entry")).active().height(30).build());
                        layout.child(FZText.builder(Component.literal("Tall entry")).active().height(50).build());
                        layout.child(FZButton.builder().message(Component.literal("Button entry")).build());
                        layout.child(FZText.builder(Component.literal("Another text entry")).height(20).build());
                        layout.child(FZButton.builder().message(Component.literal("Another button entry")).build());
                        layout.child(FZText.builder(Component.literal("Final tall entry")).height(50).build());

                        layout.child(FZFlexLayout.horizontal().spacing(8)).also(row -> {
                            row.child(FZText.builder(Component.literal("Row with button")).build(), row.flexChildSettings());
                            row.child(FZButton.builder().message(Component.literal("Action")).build());
                        });

                        layout.child(FZFlexLayout.horizontal().spacing(8)).also(row -> {
                            row.child(FZButton.builder().message(Component.literal("Option A")).build(), row.flexChildHorizontalSettings());
                            row.child(FZButton.builder().message(Component.literal("Option B")).build(), row.flexChildHorizontalSettings());
                        });

                        layout.child(FZFlexLayout.vertical().spacing(4)).also(row -> {
                            row.child(FZText.builder(Component.literal("Stacked label")).build(), row.newChildSettings().alignHorizontallyCenter());
                            row.child(FZButton.builder().message(Component.literal("Stacked button")).bigWidth().build(), row.newChildSettings().alignHorizontallyCenter());
                        });

                        layout.child(FZFlexLayout.horizontal().spacing(8).also(row -> {
                            row.child(FZButton.builder()
                                    .square()
                                    .message(Component.literal(q.collapsed ? ">" : "v"))
                                    .onPress(() -> state.set(prev -> prev.withCollapse(!prev.quantity.collapsed)))
                                    .build());
                            row.child(FZText.builder(Component.literal("Quantity")).build(), row.flexChildSettings());
                            row.child(FZButton.builder()
                                    .square()
                                    .message(Component.literal("-"))
                                    .active(q.count > 0)
                                    .onPress(() -> state.set(prev -> prev.withCount(prev.quantity.count - 1)))
                                    .build());
                            row.child(new WrappedComponent(FZButton.builder()
                                    .square()
                                    .message(Component.literal("+"))
                                    .contextEntries(FZPopoverMenuItem.builder().message(Component.literal("Hello world!")).build())
                                    .onPress(() -> state.set(prev -> prev.withCount(prev.quantity.count + 1)))
                                    .build()));
                        }));

                        if (!q.collapsed) {
                            layout.child(FZFlexLayout.vertical().spacing(4).also(row -> {
                                row.defaultChildSettings().flexCross();
                                for (int i = 0; i < q.count; i++) {
                                    row.child(FZText.builder(Component.literal("Item: " + i)).active().build());
                                }
                            }));
                        }

                        layout.child(FZText.builder(Component.literal("Another simple entry")).height(20).build());


                        var redRect = Renderables.fill(CommonColors.RED);
                        layout.child(FZIconButton.builder(new WidgetRenderables(redRect, redRect.then(Renderables.outline(-1)))).build());

                        layout.child(FZIconButton.bind("rend", state.map(State::count2).map(count -> {
                            var rect = Renderables.fill(CommonColors.BLUE).then(Renderables.text(Component.literal("Count2: " + count)));
                            return FZIconButton
                                    .builder(new WidgetRenderables(rect, rect.then(Renderables.outline(-1))))
                                    .onPress(() -> state.set(prev -> prev.withCount2(prev.count2 + 1)))
                                    .active()
                                    .toProps();
                        })), layout.newChildSettings().unflex());
                    }).toProps())), root.flexChildSettings());


            root.child(FZFlexLayout.horizontal().spacing(8)).also(row -> {
                row.child(FZDropdown.builder(this)
                        .message(Component.literal("Dropdown"))
                        .entry(option -> option
                                .message(Component.literal("Option 1"))
                                .rightIcon(new WidgetElements(Renderables.sprite(Identifier.withDefaultNamespace("icon/unseen_notification")), 16, 16)))
                        .entry(option -> option
                                .message(Component.literal("Super long dropdown option message that will surely not fit"))
                                .active(false)
                                .rightIcon(new WidgetElements(Renderables.sprite(Identifier.withDefaultNamespace("icon/accessibility")), 16, 16)))
                        .entry(button -> button.message(Component.literal("Option 3")))
                        .entry(button -> button.message(Component.literal("Option 4")))
                        .entry(button -> button.message(Component.literal("Option 5")))
                        .entry(button -> button.message(Component.literal("Option 5")))
                        .entry(button -> button.message(Component.literal("Option 5")))
                        .entry(button -> button.message(Component.literal("Option 5")))
                        .entry(button -> button.message(Component.literal("Option 5")))
                        .entry(button -> button.message(Component.literal("Option 5")))
                        .build());

                row.spacer().width(150);

                row.child(FZDropdown.builder(this)
                        .size(20, 20)
                        .minContainerWidth(150, HorizontalDirection.LEFT)
                        .entry(button -> button.message(Component.literal("Option 5")))
                        .entry(button -> button.message(Component.literal("Option 5")))
                        .entry(button -> button.message(Component.literal("Option 5")))
                        .build());

            });

            root.child(FZFlexLayout.horizontal().spacing(8), root.flexChildHorizontalSettings()).also(row -> {
                row.justifyContents(Justification.CENTER);
                row.child(FZIconButton.bind("sprite-test", state.map(State::count2).map(count -> FZIconButton
                        .builder(count > 5 ? SPRITES2 : SPRITES)
                        .square()
                        .toProps())));
                row.child(FZButton.builder()
                        .message(CommonComponents.GUI_DONE)
                        .bigWidth()
                        .onPress(this::onClose)
                        .build());
            });

        })).padding(0, 8, 0, 8).clamp().arrange().visitWidgets(collector::renderableWidget).get();
    }

    @Override
    protected void repositionElements() {
        if (this.rootLayout != null) {
            this.rootLayout.fidgetz$setSize(width, height);
            this.rootLayout.arrangeElements();
        } else {
            super.repositionElements();
        }
    }

    private record State(Quantity quantity, int count2) {
        State {
            count2 = Math.max(0, count2);
        }

        static State defaults() {
            return new State(new Quantity(0, false), 0);
        }

        State withCount(int count) {
            return new State(new Quantity(count, false), count2);
        }

        State withCollapse(boolean collapsed) {
            return new State(new Quantity(quantity.count, collapsed), count2);
        }

        State withCount2(int count2) {
            return new State(quantity, count2);
        }
    }

    private record Quantity(int count, boolean collapsed) {
        Quantity {
            count = Math.max(0, count);
        }
    }
}
