package li.cil.architect.common.blueprint;

import li.cil.architect.api.blueprint.ItemSource;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public final class ItemSourceImpl implements ItemSource {
    private final boolean isCreative;
    private final IItemHandler handler;

    ItemSourceImpl(final boolean isCreative, final IItemHandler handler) {
        this.isCreative = isCreative;
        this.handler = handler;
    }

    @Override
    public boolean isCreative() {
        return isCreative;
    }

    @Override
    public IItemHandler getItemHandler() {
        return handler;
    }

    @Override
    public ItemStack extract(final ItemStack wantStack) {
        if (isCreative) {
            return wantStack.copy();
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            final ItemStack haveStack = handler.getStackInSlot(slot);
            if (haveStack.isItemEqual(wantStack)) {
                final ItemStack extractedStack = handler.extractItem(slot, 1, false);
                assert extractedStack.isItemEqual(wantStack);
                if (!extractedStack.isEmpty()) {
                    return extractedStack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
