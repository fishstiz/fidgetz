package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.renderables.RenderableRectangle;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZClampedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZLayouts;
import io.github.fishstiz.fidgetz.v0.gui.layouts.Justification;
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
    private @Nullable FZClampedLayout rootLayout;

    public ListScreen() {
        super(Component.literal("List Screen"));
    }

    @Override
    protected void onInitialize(GuiComponentCollector collector) {
        this.rootLayout = FZLayouts.composer(this, FZLayouts.flexVertical(this).spacing(8).also(root -> {
            root.addChild(FZLayouts.flexHorizontal().spacing(8), root.flexChildHorizontalSettings()).also(header -> {
                header.justifyContents(Justification.CENTER).alignContents(Justification.CENTER).defaultChildSettings().flexCross();

                header.addChild(FZText.builder(title).build());
                header.addChild(FZButton.bind("decrement2", state.map(State::count2).map(count -> FZButton.builder()
                        .square()
                        .message(Component.literal("-"))
                        .active(count > 0)
                        .onPress(() -> state.set(prev -> prev.withCount2(prev.count2 - 1)))
                        .toProps())));
                header.addChild(FZButton.builder()
                        .square()
                        .message(Component.literal("+"))
                        .onPress(() -> state.set(prev -> prev.withCount2(prev.count2 + 1)))
                        .build());
                header.addChild(FZText.bind("count2", state.map(State::count2).map(count ->
                        FZText.builder(Component.literal("Count2: " + count)).toProps()
                )));
            });

            root.addChild(FZList.bind("list", state.map(State::quantity).map(q -> FZList.builder().contentPadding(8).entries((list, layout) -> {
                layout.spacing(8).defaultChildSettings().flexCross();

                layout.addChild(FZButton.builder()
                        .message(Component.literal("Disabled Button"))
                        .leftAlignedMessage()
                        .inactive()
                        .build());
                layout.addChild(FZButton.builder()
                        .message(Component.literal("Disabled Button"))
                        .inactive()
                        .build());

                layout.addChild(FZDropdown.builder(list)
                        .message(Component.literal("Dropdown"))
                        .entry(option -> option.withMessage(Component.literal("Option 1")))
                        .entry(option -> option.withMessage(Component.literal("Option 2")))
                        .entry(option -> option.withMessage(Component.literal("Option 3")))
                        .entry(option -> option.withMessage(Component.literal("Option 4")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .build());

                layout.addChild(FZDropdown.builder(list)
                        .message(Component.literal("Dropdown 2"))
                        .inactive()
                        .entry(option -> option.withMessage(Component.literal("Option 1")))
                        .entry(option -> option.withMessage(Component.literal("Option 2")))
                        .entry(option -> option.withMessage(Component.literal("Option 3")))
                        .entry(option -> option.withMessage(Component.literal("Option 4")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .build());

                layout.addChild(FZLayouts.flexHorizontal()).also(btns -> {
                    btns.spacing(4).justifyContents(Justification.SPACE_BETWEEN);

                    btns.addChild(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                            }, false)
                            .sprite(Identifier.withDefaultNamespace("icon/unseen_notification"), 16, 16)
                            .size(20, 20)
                            .build()).active = false;
                    btns.addChild(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                            }, false)
                            .sprite(Identifier.withDefaultNamespace("icon/accessibility"), 16, 16)
                            .size(20, 20)
                            .build()).active = false;
                    btns.addChild(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                            }, false)
                            .sprite(Identifier.withDefaultNamespace("icon/language"), 16, 16)
                            .size(20, 20)
                            .build()).active = false;

                    btns.addChild(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                            }, false)
                            .sprite(Identifier.withDefaultNamespace("icon/unseen_notification"), 16, 16)
                            .size(20, 20)
                            .build());
                    btns.addChild(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                            }, false)
                            .sprite(Identifier.withDefaultNamespace("icon/accessibility"), 16, 16)
                            .size(20, 20)
                            .build());
                    btns.addChild(SpriteIconButton.builder(CommonComponents.EMPTY, _ -> {
                            }, false)
                            .sprite(Identifier.withDefaultNamespace("icon/language"), 16, 16)
                            .size(20, 20)
                            .build());
                });

                layout.addChild(FZLayouts.flexHorizontal()).also(btns -> {
                    btns.spacing(4).justifyContents(Justification.SPACE_BETWEEN);

                    btns.addChild(FZIconButton.builder()
                            .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/unseen_notification"), 16, 16))
                            .size(20, 20)
                            .build()).active = false;
                    btns.addChild(FZIconButton.builder()
                            .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/accessibility"), 16, 16))
                            .size(20, 20)
                            .build()).active = false;
                    btns.addChild(FZIconButton.builder()
                            .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/language"), 16, 16))
                            .size(20, 20)
                            .build()).active = false;

                    btns.addChild(FZIconButton.builder()
                            .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/unseen_notification"), 16, 16))
                            .size(20, 20)
                            .build());
                    btns.addChild(FZIconButton.builder()
                            .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/accessibility"), 16, 16))
                            .size(20, 20)
                            .build());
                    btns.addChild(FZIconButton.builder()
                            .icon(new WidgetElements(Identifier.withDefaultNamespace("icon/language"), 16, 16))
                            .size(20, 20)
                            .build());
                });

                layout.addChild(FZLayouts.flexHorizontal()).also(btns -> {
                    btns.spacing(4).justifyContents(Justification.SPACE_BETWEEN);

                    btns.addChild(FZIconButton.builder(Identifier.withDefaultNamespace("icon/unseen_notification"))
                            .size(20, 20)
                            .build()).active = false;
                    btns.addChild(FZIconButton.builder(Identifier.withDefaultNamespace("icon/accessibility"))
                            .size(20, 20)
                            .build()).active = false;
                    btns.addChild(FZIconButton.builder(Identifier.withDefaultNamespace("icon/language"))
                            .size(20, 20)
                            .build()).active = false;

                    btns.addChild(FZIconButton.builder(Identifier.withDefaultNamespace("icon/unseen_notification"))
                            .size(20, 20)
                            .build());
                    btns.addChild(FZIconButton.builder(Identifier.withDefaultNamespace("icon/accessibility"))
                            .size(20, 20)
                            .build());
                    btns.addChild(FZIconButton.builder(Identifier.withDefaultNamespace("icon/language"))
                            .size(20, 20)
                            .build());
                });

                layout.addChild(FZLayouts.flexHorizontal()).also(btns -> {
                    btns.spacing(4).justifyContents(Justification.SPACE_BETWEEN);

                    RenderableRectangle whiteOutline = Renderables.outline(CommonColors.WHITE);
                    RenderableRectangle unseenNotification = Renderables.sprite(Identifier.withDefaultNamespace("icon/unseen_notification"));
                    RenderableRectangle accessibility = Renderables.sprite(Identifier.withDefaultNamespace("icon/accessibility"));
                    RenderableRectangle language = Renderables.sprite(Identifier.withDefaultNamespace("icon/language"));

                    btns.addChild(FZIconButton
                            .builder(new WidgetRenderables(unseenNotification, unseenNotification, unseenNotification.then(whiteOutline)))
                            .size(20, 20)
                            .build()).active = false;
                    btns.addChild(FZIconButton
                            .builder(new WidgetRenderables(accessibility, accessibility, accessibility.then(whiteOutline)))
                            .size(20, 20)
                            .build()).active = false;
                    btns.addChild(FZIconButton
                            .builder(new WidgetRenderables(language, language, language.then(whiteOutline)))
                            .size(20, 20)
                            .build()).active = false;

                    btns.addChild(FZIconButton
                            .builder(new WidgetRenderables(unseenNotification, unseenNotification, unseenNotification.then(whiteOutline)))
                            .size(20, 20)
                            .build());
                    btns.addChild(FZIconButton
                            .builder(new WidgetRenderables(accessibility, accessibility, accessibility.then(whiteOutline)))
                            .size(20, 20)
                            .build());
                    btns.addChild(FZIconButton
                            .builder(new WidgetRenderables(language, language, language.then(whiteOutline)))
                            .size(20, 20)
                            .build());
                });

                layout.addChild(FZTextField.builder().inactive().build());
                layout.addChild(FZTextField.builder().hint(Component.literal("Hint...")).build());
                layout.addChild(FZTextField.builder().hint(Component.literal("Hint...")).inactive().build());

                var box = layout.addChild(new EditBox(Minecraft.getInstance().font, 150, 20, CommonComponents.EMPTY));
                box.active = false;
                box.setEditable(false);

                layout.addSpacer().height(4);
                layout.addChild(FZTextField.builder()
                        .onChange(textFieldState::set)
                        .hint(Component.literal("update below..."))
                        .build());
                layout.addChild(FZTextField.bind("textfield", textFieldState.map(value -> FZTextField.builder()
                        .hint(Component.literal("textfieldstate"))
                        .text(value)
                        .toProps())));

                layout.addSpacer().height(4);
                layout.addChild(FZText.bind("text-2", textFieldState2.map(value -> FZText
                        .builder(Component.literal("textFieldState2: " + value))
                        .overlay(Renderables.fill(ARGB.color(0.2f, CommonColors.BLUE)))
                        .active()
                        .toProps())));

                layout.addChild(FZTextField.bind("textfield2", textFieldState2.map(value -> FZTextField.builder()
                        .hint(Component.literal("textfieldstate2"))
                        .text(value)
                        .onChange(textFieldState2::set)
                        .toProps())));

                layout.addSpacer().height(4);
                layout.addChild(FZTextField.bind("textfield3", textFieldState3.map(value -> FZTextField.builder()
                        .text(value)
                        .onChange(_ -> textFieldState3.set(prev -> prev))
                        .toProps())));


                layout.addChild(FZTextField.builder()
                        .text("False!")
                        .filter(value -> !value.contains("?"))
                        .build());

                layout.addSpacer().height(4);

                layout.addChild(FZText.builder(Component.literal("Short entry")).active().height(20).build());
                layout.addChild(FZText.builder(Component.literal("Medium entry")).active().height(30).build());
                layout.addChild(FZText.builder(Component.literal("Tall entry")).active().height(50).build());
                layout.addChild(FZButton.builder().message(Component.literal("Button entry")).build());
                layout.addChild(FZText.builder(Component.literal("Another text entry")).height(20).build());
                layout.addChild(FZButton.builder().message(Component.literal("Another button entry")).build());
                layout.addChild(FZText.builder(Component.literal("Final tall entry")).height(50).build());

                layout.addChild(FZLayouts.flexHorizontal().spacing(8)).also(row -> {
                    row.addChild(FZText.builder(Component.literal("Row with button")).build(), row.flexChildSettings());
                    row.addChild(FZButton.builder().message(Component.literal("Action")).build());
                });

                layout.addChild(FZLayouts.flexHorizontal().spacing(8)).also(row -> {
                    row.addChild(FZButton.builder().message(Component.literal("Option A")).build(), row.flexChildHorizontalSettings());
                    row.addChild(FZButton.builder().message(Component.literal("Option B")).build(), row.flexChildHorizontalSettings());
                });

                layout.addChild(FZLayouts.flexVertical().spacing(4)).also(row -> {
                    row.addChild(FZText.builder(Component.literal("Stacked label")).build(), row.newChildSettings().alignHorizontallyCenter());
                    row.addChild(FZButton.builder().message(Component.literal("Stacked button")).bigWidth().build(), row.newChildSettings().alignHorizontallyCenter());
                });

                layout.addChild(FZLayouts.flexHorizontal().spacing(8).also(row -> {
                    row.addChild(FZButton.builder()
                            .square()
                            .message(Component.literal(q.collapsed ? ">" : "v"))
                            .onPress(() -> state.set(prev -> prev.withCollapse(!prev.quantity.collapsed)))
                            .build());
                    row.addChild(FZText.builder(Component.literal("Quantity")).build(), row.flexChildSettings());
                    row.addChild(FZButton.builder()
                            .square()
                            .message(Component.literal("-"))
                            .active(q.count > 0)
                            .onPress(() -> state.set(prev -> prev.withCount(prev.quantity.count - 1)))
                            .build());
                    row.addChild(new WrappedComponent(FZButton.builder()
                            .square()
                            .message(Component.literal("+"))
                            .contextEntries(FZContextMenuEntry.builder().message(Component.literal("Hello world!")).build())
                            .onPress(() -> state.set(prev -> prev.withCount(prev.quantity.count + 1)))
                            .build()));
                }));

                if (!q.collapsed) {
                    layout.addChild(FZLayouts.flexVertical().spacing(4).also(row -> {
                        row.defaultChildSettings().flexCross();
                        for (int i = 0; i < q.count; i++) {
                            row.addChild(FZText.builder(Component.literal("Item: " + i)).active().build());
                        }
                    }));
                }

                layout.addChild(FZText.builder(Component.literal("Another simple entry")).height(20).build());


                var redRect = Renderables.fill(CommonColors.RED);
                layout.addChild(FZIconButton.builder(new WidgetRenderables(redRect, redRect.then(Renderables.outline(-1)))).build());

                layout.addChild(FZIconButton.bind("rend", state.map(State::count2).map(count -> {
                    var rect = Renderables.fill(CommonColors.BLUE).then(Renderables.text(Component.literal("Count2: " + count)));
                    return FZIconButton
                            .builder(new WidgetRenderables(rect, rect.then(Renderables.outline(-1))))
                            .onPress(() -> state.set(prev -> prev.withCount2(prev.count2 + 1)))
                            .active()
                            .toProps();
                })), layout.newChildSettings().unflex());
            }).toProps())), root.flexChildSettings());


            root.addChild(FZLayouts.flexHorizontal().spacing(8)).also(row -> {
                row.addChild(FZDropdown.builder(this)
                        .message(Component.literal("Dropdown"))
                        .entry(option -> option
                                .withMessage(Component.literal("Option 1"))
                                .withIcon(Renderables.sprite(Identifier.withDefaultNamespace("icon/unseen_notification"))))
                        .entry(option -> option
                                .withMessage(Component.literal("Super long dropdown option message that will surely not fit"))
                                .withActive(false)
                                .withIcon(Renderables.sprite(Identifier.withDefaultNamespace("icon/accessibility"))))
                        .entry(option -> option.withMessage(Component.literal("Option 3")))
                        .entry(option -> option.withMessage(Component.literal("Option 4")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .build());

                row.addChild(FZDropdown.builder(this)
                        .size(20, 20)
                        .minContainerWidth(150, HorizontalDirection.LEFT)
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .entry(option -> option.withMessage(Component.literal("Option 5")))
                        .build());

            });

            root.addChild(FZLayouts.flexHorizontal().spacing(8), root.flexChildHorizontalSettings()).also(row -> {
                row.justifyContents(Justification.CENTER);
                row.addChild(FZIconButton.bind("sprite-test", state.map(State::count2).map(count -> FZIconButton
                        .builder(count > 5 ? SPRITES2 : SPRITES)
                        .square()
                        .toProps())));
                row.addChild(FZButton.builder()
                        .message(CommonComponents.GUI_DONE)
                        .bigWidth()
                        .onPress(this::onClose)
                        .build());
            });

        })).padded(0, 8, 0, 8).clamped().arrange().visitWidgets(collector::renderableWidget).get();
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
