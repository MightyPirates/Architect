package li.cil.architect.common.item;

import li.cil.architect.api.API;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

abstract class AbstractItem extends Item {
    // --------------------------------------------------------------------- //
    // Computed data.

    // NBT tag names.
    private static final String TAG_DATA = API.MOD_ID + ":data";

    // --------------------------------------------------------------------- //
    // Used for items that can be converted into other items by using them, to
    // avoid accidentally using the new item right after the conversion finishes.

    private static final int USE_DELAY_AFTER_CONVERSION = 500;
    private static long disableUseBefore;

    static boolean isUseDisabled() {
        return System.currentTimeMillis() < disableUseBefore;
    }

    static void disableUseAfterConversion() {
        AbstractItem.disableUseBefore = System.currentTimeMillis() + USE_DELAY_AFTER_CONVERSION;
    }

    // --------------------------------------------------------------------- //

    static NBTTagCompound getDataTag(final ItemStack stack) {
        NBTTagCompound stackNbt = stack.getTagCompound();
        if (stackNbt == null) {
            stackNbt = new NBTTagCompound();
            stack.setTagCompound(stackNbt);
        }

        final NBTTagCompound dataNbt = stackNbt.getCompoundTag(TAG_DATA);
        stackNbt.setTag(TAG_DATA, dataNbt); // In case it's not there yet.
        return dataNbt;
    }
}
