package li.cil.architect.common.item;

import li.cil.architect.api.API;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

abstract class AbstractPatternItem extends Item {
    // --------------------------------------------------------------------- //
    // Computed data.

    // NBT tag names.
    private static final String TAG_DATA = API.MOD_ID + ":data";

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
