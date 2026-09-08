package io.github.fishstiz.testmod.gui.screens;

import io.github.fishstiz.fidgetz.v0.gui.components.*;
import io.github.fishstiz.fidgetz.v0.gui.components.color.FZColorPicker;
import io.github.fishstiz.fidgetz.v0.gui.components.color.ColorWidgets;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZComposedLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.fidgetz.v0.gui.layouts.Justification;
import io.github.fishstiz.fidgetz.v0.gui.renderables.Renderables;
import io.github.fishstiz.fidgetz.v0.gui.screens.FZScreen;
import io.github.fishstiz.fidgetz.v0.gui.state.FZMutableRef;
import io.github.fishstiz.fidgetz.v0.gui.color.HSVA;
import io.github.fishstiz.fidgetz.v0.gui.color.RGBA;
import io.github.fishstiz.fidgetz.v0.gui.color.FZColor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class ColorPickerScreen extends FZScreen {
    private static final int BLACK = 0xFF000000;
    private static final int RED = 0xFFFF0000;
    private static final int GREEN = 0xFF00FF00;
    private static final int BLUE = 0xFF0000FF;
    private static final int YELLOW = 0xFFFFFF00;
    private final FZMutableRef<FZColor> colorRef = new FZMutableRef<>(FZColor.fromRGBA(new RGBA(0, 0, 0, 1f)));

    public ColorPickerScreen() {
        super(CommonComponents.EMPTY);
    }

    private void handleHueChange(float hue) {
        colorRef.set(color -> color.withHue(hue));
    }

    private void handleSaturationChange(float saturation) {
        colorRef.set(color -> color.withSaturation(saturation));
    }

    private void handleValueChange(float value) {
        colorRef.set(color -> color.withValue(value));
    }

    private void handleRedChange(int red) {
        colorRef.set(color -> color.withRed(red));
    }

    private void handleGreenChange(int green) {
        colorRef.set(color -> color.withGreen(green));
    }

    private void handleBlueChange(int blue) {
        colorRef.set(color -> color.withBlue(blue));
    }

    private void handleAlphaChange(float alpha) {
        colorRef.set(color -> color.withAlpha(alpha));
    }

    private FZIconButton createSwatch(TextColor textColor) {
        int argb = textColor.getValue() | 0xFF000000;
        return FZIconButton.builder()
                .background(ColorWidgets.colorPreviewSprites(argb))
                .size(16, 16)
                .tooltip(Component.literal(textColor.toString()))
                .onPress(() -> colorRef.set(FZColor.fromARGB(argb)))
                .build();
    }

    @Override
    protected void collectChildren(GuiComponentCollector collector) {

        FZFlexLayout root = FZFlexLayout.horizontal(this).spacing(8).wrap();

        root.child(ColorWidgets.sv2DSlider(HSVA.fromARGB(RED)).build());
        root.child(ColorWidgets.sv2DSlider(HSVA.fromARGB(GREEN)).build());
        root.child(ColorWidgets.sv2DSlider(HSVA.fromARGB(BLUE)).build());
        root.child(ColorWidgets.sv2DSlider(HSVA.fromARGB(YELLOW)).build());

        {
            FZFlexLayout oneDSliders = root.child(FZFlexLayout.vertical().spacing(8));
            oneDSliders.child(ColorWidgets.hueSlider(0).build());
            oneDSliders.child(ColorWidgets.alphaSlider(() -> RED).build());
            oneDSliders.child(ColorWidgets.alphaSlider(() -> GREEN).build());
            oneDSliders.child(ColorWidgets.alphaSlider(() -> BLUE).build());
        }

        root.child(FZDropdown.builder(this)
                .message(Component.literal("Color Picker"))
                .closeOnBlur(false)
                .width(100)
                .leftIcon(new WidgetElements(ColorWidgets.colorPreviewSprites(() -> colorRef.value().toARGB()), 10, 10))
                .containerBackground(Renderables.sprite(Identifier.withDefaultNamespace("popup/background")))
                .containerBorder(8, 6, 8, 8)
                .maxContainerHeight(0)
                .minContainerWidth(150)
                .maxContainerWidth(150)
                .entryWidget(ctx -> {
                    FZFlexLayout header = FZFlexLayout.horizontal().spacing(8);
                    header.justifyContents(Justification.SPACE_BETWEEN).defaultChildSettings().alignVerticallyMiddle();

                    header.child(FZText.builder(Component.literal("Pick a color").withStyle(ChatFormatting.BOLD)).build());

                    header.child(FZIconButton.builder()
                            .size(16, 16)
                            .background(WidgetRenderables.sprites(new WidgetSprites(
                                    Identifier.withDefaultNamespace("widget/cross_button"),
                                    Identifier.withDefaultNamespace("widget/cross_button_highlighted")
                            )))
                            .onPress(ctx::closeMenu)
                            .build());

                    return WrappedLayout.wrap(header);
                })
                .entryWidget(
                        ignored -> FZColorPicker.bind(
                                "ColorPickerPopover",
                                colorRef.map(color -> FZColorPicker.builder()
                                        .color(color)
                                        .row(FZColorPicker.Widget.SV, FZColorPicker.Widget.PREVIEW)
                                        .row(FZColorPicker.Widget.HSV)
                                        .row(FZColorPicker.Widget.ALPHA)
                                        .row(FZColorPicker.Widget.HEX)
                                        .onChange(e -> colorRef.set(e.color()))
                                        .toProps()
                                )
                        ),
                        settings -> settings.withCloseOnInteract(false).withAutoDividerAfter(false)
                )
                .build());

        FZFlexLayout boundContainer = root.child(FZFlexLayout.vertical().spacing(8));
        boundContainer.child(FZText.bind(
                "ColorText",
                colorRef.map(color -> FZText.builder(Component.literal(color.toString()))
                        .width(400)
                        .toProps()
                )
        ));

        boundContainer.child(FZText.bind(
                "HexString",
                colorRef.map(value -> FZText.builder(Component.literal(Integer.toHexString(value.toARGB())))
                        .width(250)
                        .toProps()
                )
        ));

        FZFlexLayout colorPicker = boundContainer.child(FZFlexLayout.vertical()).spacing(8);
        {
            FZFlexLayout channels = colorPicker.child(FZFlexLayout.horizontal()).spacing(8);
            {
                channels.child(FZCustom2DSlider.bind(
                        "SaturationValueArea",
                        colorRef.map(color -> ColorWidgets.sv2DSlider(color.hsva())
                                .onChange(e -> colorRef.set(prev -> prev.withSV((float) e.x(), (float) e.y())))
                                .toProps()
                        )
                ));

                FZFlexLayout hsv = channels.child(FZFlexLayout.vertical()).spacing(8);
                {
                    FZFlexLayout h = hsv.child(FZFlexLayout.horizontal()).spacing(6);
                    {
                        h.defaultChildSettings().alignVerticallyMiddle();

                        h.child(FZText.builder(Component.literal("H:")).width(8).build());
                        h.child(FZCustomSlider.bind(
                                "HueSlider",
                                colorRef.map(FZColor::hue).map(hue -> ColorWidgets.hueSlider(hue)
                                        .height(16)
                                        .onChange(e -> handleHueChange((float) e.value()))
                                        .toProps()
                                )
                        ));
                        h.child(ColorWidgets.hueField("HueField", colorRef.map(FZColor::hue), this::handleHueChange));
                    }

                    FZFlexLayout s = hsv.child(FZFlexLayout.horizontal()).spacing(6);
                    {
                        s.defaultChildSettings().alignVerticallyMiddle();

                        s.child(FZText.builder(Component.literal("S:")).width(8).build());
                        s.child(FZCustomSlider.bind(
                                "SaturationSlider",
                                colorRef.map(color -> ColorWidgets.saturationSlider(color.hsva())
                                        .height(16)
                                        .onChange(e -> handleSaturationChange((float) e.value()))
                                        .toProps()
                                )
                        ));
                        s.child(ColorWidgets.saturationField(
                                "SaturationField",
                                colorRef.map(FZColor::saturation),
                                this::handleSaturationChange
                        ));
                    }

                    FZFlexLayout v = hsv.child(FZFlexLayout.horizontal()).spacing(6);
                    {
                        v.defaultChildSettings().alignVerticallyMiddle();
                        v.child(FZText.builder(Component.literal("V:")).width(8).build());
                        v.child(FZCustomSlider.bind(
                                "ValueSlider",
                                colorRef.map(color -> ColorWidgets.valueSlider(color.hsva())
                                        .height(16)
                                        .onChange(e -> handleValueChange((float) e.value()))
                                        .toProps()
                                )
                        ));
                        v.child(ColorWidgets.valueField(
                                "ValueField",
                                colorRef.map(FZColor::value),
                                this::handleValueChange
                        ));
                    }
                }

                FZFlexLayout rgb = channels.child(FZFlexLayout.vertical()).spacing(8);
                {
                    FZFlexLayout r = rgb.child(FZFlexLayout.horizontal()).spacing(6);
                    {
                        r.defaultChildSettings().alignVerticallyMiddle();

                        r.child(FZText.builder(Component.literal("R:")).width(8).build());
                        r.child(FZCustomSlider.bind(
                                "RedSlider",
                                colorRef.map(color -> ColorWidgets.redSlider(color.rgba())
                                        .height(16)
                                        .onChange(e -> handleRedChange((int) e.value()))
                                        .toProps()
                                )
                        ));
                        r.child(ColorWidgets.redField("RedField", colorRef.map(FZColor::red), this::handleRedChange));
                    }

                    FZFlexLayout g = rgb.child(FZFlexLayout.horizontal()).spacing(6);
                    {
                        g.defaultChildSettings().alignVerticallyMiddle();

                        g.child(FZText.builder(Component.literal("G:")).width(8).build());
                        g.child(FZCustomSlider.bind(
                                "GreenSlider",
                                colorRef.map(color -> ColorWidgets.greenSlider(color.rgba())
                                        .height(16)
                                        .onChange(e -> handleGreenChange((int) e.value()))
                                        .toProps()
                                )
                        ));
                        g.child(ColorWidgets.greenField(
                                "GreenField",
                                colorRef.map(FZColor::green),
                                this::handleGreenChange
                        ));
                    }

                    FZFlexLayout b = rgb.child(FZFlexLayout.horizontal()).spacing(6);
                    {
                        b.defaultChildSettings().alignVerticallyMiddle();

                        b.child(FZText.builder(Component.literal("B:")).width(8).build());
                        b.child(FZCustomSlider.bind(
                                "BlueSlider",
                                colorRef.map(color -> ColorWidgets.blueSlider(color.rgba())
                                        .height(16)
                                        .onChange(e -> handleBlueChange((int) e.value()))
                                        .toProps()
                                )
                        ));
                        b.child(ColorWidgets.blueField(
                                "BlueField",
                                colorRef.map(FZColor::blue),
                                this::handleBlueChange
                        ));
                    }
                }
            }

            FZFlexLayout misc = colorPicker.child(
                    FZFlexLayout.horizontal(),
                    colorPicker.flexChildHorizontalSettings()
            );
            {
                misc.spacing(8);

                misc.child(FZIcon.bind(
                        "ColorPreview",
                        colorRef.map(color -> ColorWidgets.colorPreview(color.toARGB())
                                .height(16)
                                .toProps())
                ));

                FZFlexLayout hex = misc.child(FZFlexLayout.horizontal(), misc.flexChildHorizontalSettings());
                {
                    hex.spacing(8).defaultChildSettings().alignVerticallyMiddle();

                    hex.child(FZText.builder(Component.literal("Hex:")).width(20).build());
                    hex.child(
                            ColorWidgets.hexField(
                                    "HexField",
                                    true,
                                    colorRef.map(FZColor::toARGB),
                                    argb -> colorRef.set(FZColor.fromARGB(argb))
                            ),
                            misc.flexChildHorizontalSettings()
                    );
                }

                FZFlexLayout alpha = misc.child(FZFlexLayout.horizontal()).spacing(6);
                {
                    alpha.defaultChildSettings().alignVerticallyMiddle();

                    alpha.child(FZText.builder(Component.literal("A:")).width(8).build());
                    alpha.child(FZCustomSlider.bind(
                            "AlphaSlider",
                            colorRef.map(color -> ColorWidgets.alphaSlider(color.rgba())
                                    .height(16)
                                    .onChange(e -> handleAlphaChange((float) e.value()))
                                    .toProps()
                            )
                    ));
                    alpha.child(ColorWidgets.alphaField(
                            "AlphaField",
                            colorRef.map(FZColor::alpha),
                            this::handleAlphaChange
                    ));
                }
            }
        }

        root.child(FZColorPicker.builder()
                .width(250)
                .build());

        root.child(FZColorPicker.bind(
                "ColorPicker",
                colorRef.map(color -> FZColorPicker.builder()
                        .color(color)
                        .onChange(e -> colorRef.set(e.color()))
                        .toProps()
                )
        ));

        FZComposedLayout.contain(this, root)
                .visitWidgets(collector::renderableWidget)
                .padding(24)
                .clamp()
                .arrange();
    }

    @Override
    protected FZContextMenu.Builder buildContextMenu() {
        return super.buildContextMenu().border(8);
    }

    @Override
    public void fidgetz$updateContextEntries(double x, double y, FZContextMenu.Collector collector) {
        collector.addWidget(
                ignored -> FZColorPicker.bind(
                        "ColorPickerMenu",
                        colorRef.map(color -> FZColorPicker.builder()
                                .alphaDisabled()
                                .color(color)
                                .row(FZColorPicker.Widget.SV, FZColorPicker.Widget.PREVIEW)
                                .row(FZColorPicker.Widget.HUE)
                                .row(FZColorPicker.Widget.ALPHA_SLIDER)
                                .row(FZColorPicker.Widget.HEX_FIELD)
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.BLACK)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.DARK_BLUE)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.DARK_AQUA)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.DARK_RED)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.GOLD)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.GRAY)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.BLUE)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.GREEN)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.AQUA)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.RED)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.LIGHT_PURPLE)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.YELLOW)))
                                .swatch(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.WHITE)))
                                .onChange(e -> colorRef.set(e.color()))
                                .toProps()
                        )
                ),
                s -> s.withCloseOnInteract(false)
        );
    }
}
