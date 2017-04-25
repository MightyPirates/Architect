package li.cil.architect.util;

import net.minecraft.client.renderer.GlStateManager;

public final class RenderUtils {
    public static void setColor(final int argb) {
        final int a = (argb >> 24) & 0xFF;
        final int r = (argb >> 16) & 0xFF;
        final int g = (argb >> 8) & 0xFF;
        final int b = argb & 0xFF;

        final float af = a / 255f;
        final float rf = r / 255f;
        final float gf = g / 255f;
        final float bf = b / 255f;

        GlStateManager.color(rf, gf, bf, af);
    }

    private RenderUtils() {
    }
}
