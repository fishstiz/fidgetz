package io.github.fishstiz.testmod.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    static {
        MixinEnvironment.getCurrentEnvironment().audit();
    }
}
