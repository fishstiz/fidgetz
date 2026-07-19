package io.github.fishstiz.testmod.gui;

import io.github.fishstiz.fidgetz.v0.gui.layouts.FZFlexLayout;
import io.github.fishstiz.testmod.gui.screens.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class Screens {
    public static void addScreenButtons(FZFlexLayout layout) {
        Minecraft minecraft = Minecraft.getInstance();
        layout.child(Button.builder(Component.literal("Test Screen"), _ -> minecraft.setScreen(new TestmodScreen())).build());
        layout.child(Button.builder(Component.literal("Flex Screen"), _ -> minecraft.setScreen(new FlexScreen())).build());
        layout.child(Button.builder(Component.literal("FZ Screen"), _ -> minecraft.setScreen(new FZTestScreen())).build());
        layout.child(Button.builder(Component.literal("Wrap Screen"), _ -> minecraft.setScreen(new FlexWrapScreen())).build());
        layout.child(Button.builder(Component.literal("State Screen"), _ -> minecraft.setScreen(new StatefulScreen())).build());
        layout.child(Button.builder(Component.literal("List Screen"), _ -> minecraft.setScreen(new ListScreen())).build());
        layout.child(Button.builder(Component.literal("AbstractListScreen"), _ -> minecraft.setScreen(new AbstractListScreen())).build());
        layout.child(Button.builder(Component.literal("GradientScreen"), _ -> minecraft.setScreen(new GradientScreen())).build());
        layout.child(Button.builder(Component.literal("Screenz"), _ -> minecraft.setScreen(new Screenz())).build());
        layout.child(Button.builder(Component.literal("SliderTest"), _ -> minecraft.setScreen(new SliderTest())).build());
    }
}
