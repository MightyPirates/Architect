package li.cil.architect.client.renderer.color;

import li.cil.architect.common.item.ItemBlueprint;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public enum ItemColorBlueprint implements IItemColor {
    INSTANCE;

    @Override
    public int getColorFromItemstack(final ItemStack stack, final int tintIndex) {
        switch (tintIndex) {
            case 0:
                return 0xFFFFFFF;
            case 1: {
                final EnumDyeColor color = ItemBlueprint.getColor(stack);
                return color == null ? 0x00000000 : (0xFF000000 | color.getColorValue());
            }
            default:
                return 0xFFFF00FF;
        }
    }
}
