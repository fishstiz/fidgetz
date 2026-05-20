package io.github.fishstiz.fidgetz.v0.utils;

import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public final class ScreenRectangleUtils {
    public static boolean isAreaEmpty(int width, int height) {
        return width <= 0 || height <= 0;
    }

    public static boolean isAreaEmpty(ScreenRectangle rectangle) {
        return isAreaEmpty(rectangle.width(), rectangle.height());
    }

    public static boolean unequal(ScreenRectangle rectangle, int left, int top, int width, int height) {
        return left != rectangle.left() ||
               top != rectangle.top() ||
               width != rectangle.width() ||
               height != rectangle.height();
    }

    public static boolean unequal(ScreenRectangle rectangle, LayoutElement element) {
        return unequal(rectangle, element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public static boolean containsPoint(ScreenRectangle rectangle, int x, int y) {
        return !isAreaEmpty(rectangle) && rectangle.containsPoint(x, y);
    }

    public static boolean containsPoint(int left, int top, int width, int height, int x, int y) {
        return !isAreaEmpty(width, height) && x >= left && x < left + width && y >= top && y < top + height;
    }

    public static ScreenRectangle union(ScreenRectangle a, ScreenRectangle b) {
        if (isAreaEmpty(a)) return b;
        if (isAreaEmpty(b)) return a;

        int left = Math.min(a.left(), b.left());
        int top = Math.min(a.top(), b.top());
        int right = Math.max(a.right(), b.right());
        int bottom = Math.max(a.bottom(), b.bottom());

        return new ScreenRectangle(left, top, right - left, bottom - top);
    }

    public static boolean isInsetsEmpty(ScreenRectangle padding) {
        return padding.left() <= 0 && padding.top() <= 0 && padding.right() <= 0 && padding.bottom() <= 0;
    }

    public static ScreenRectangle insets(int left, int top, int right, int bottom) {
        return new ScreenRectangle(left, top, right - left, bottom - top);
    }

    public static ScreenRectangle insets(int padding) {
        return new ScreenRectangle(padding, padding, 0, 0);
    }

    public static ScreenRectangle expand(ScreenRectangle rectangle, ScreenRectangle insets) {
        int left = rectangle.left() - insets.left();
        int top = rectangle.top() - insets.top();
        int right = rectangle.right() + insets.right();
        int bottom = rectangle.bottom() + insets.bottom();
        return new ScreenRectangle(left, top, right - left, bottom - top);
    }

    public static ScreenRectangle shrink(ScreenRectangle rectangle, ScreenRectangle insets) {
        int left = rectangle.left() + insets.left();
        int top = rectangle.top() + insets.top();
        int width = rectangle.width() - (insets.left() + insets.right());
        int height = rectangle.height() - (insets.top() + insets.bottom());
        return new ScreenRectangle(left, top, width, height);
    }

    private ScreenRectangleUtils() {
    }
}
