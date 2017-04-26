package li.cil.architect.util;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public final class ItemHandlerUtils {
    public static IItemHandler copy(final IItemHandler itemHandler) {
        final ItemStackHandler result = new ItemStackHandler(itemHandler.getSlots());
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            result.setStackInSlot(slot, itemHandler.getStackInSlot(slot));
        }
        return result;
    }

    private ItemHandlerUtils() {
    }
}
