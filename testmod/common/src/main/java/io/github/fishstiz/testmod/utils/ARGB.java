package io.github.fishstiz.testmod.utils;

import net.minecraft.util.Mth;

public final class ARGB {
    public static int color(float alpha, int color) {
        return Mth.floor(alpha * 255.0F) << 24 | color & 16777215;
    }

    private ARGB() {
    }
}
