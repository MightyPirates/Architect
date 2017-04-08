package li.cil.architect.common.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

public final class CompoundItemHandler implements IItemHandler {
    private final IItemHandler[] itemHandlers;
    private final int[] endIndices;
    private final int slotCount;

    // --------------------------------------------------------------------- //

    public CompoundItemHandler(final IItemHandler... itemHandlers) {
        this.itemHandlers = itemHandlers;
        this.endIndices = new int[itemHandlers.length];
        int slotCount = 0;
        for (int i = 0; i < itemHandlers.length; i++) {
            slotCount += itemHandlers[i].getSlots();
            endIndices[i] = slotCount;
        }
        this.slotCount = slotCount;
    }

    // --------------------------------------------------------------------- //
    // IItemHandler

    @Override
    public int getSlots() {
        return slotCount;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        final int index = getIndexForSlot(slot);
        final IItemHandler handler = getHandlerFromIndex(index);
        slot = getLocalSlotFromIndex(slot, index);
        return handler.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate) {
        final int index = getIndexForSlot(slot);
        final IItemHandler handler = getHandlerFromIndex(index);
        final int localSlot = getLocalSlotFromIndex(slot, index);
        return handler.insertItem(localSlot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        final int index = getIndexForSlot(slot);
        final IItemHandler handler = getHandlerFromIndex(index);
        final int localSlot = getLocalSlotFromIndex(slot, index);
        return handler.extractItem(localSlot, amount, simulate);
    }

    @Override
    public int getSlotLimit(final int slot) {
        final int index = getIndexForSlot(slot);
        final IItemHandler handler = getHandlerFromIndex(index);
        final int localSlot = getLocalSlotFromIndex(slot, index);
        return handler.getSlotLimit(localSlot);
    }

    // --------------------------------------------------------------------- //

    private int getIndexForSlot(final int slot) {
        if (slot < 0 || slot >= slotCount) {
            return -1;
        }

        for (int i = 0; i < endIndices.length; i++) {
            if (slot < endIndices[i]) {
                return i;
            }
        }

        throw new IllegalStateException();
    }

    private IItemHandler getHandlerFromIndex(final int index) {
        if (index < 0 || index >= itemHandlers.length) {
            return EmptyHandler.INSTANCE;
        }

        return itemHandlers[index];
    }

    private int getLocalSlotFromIndex(final int globalSlot, final int index) {
        if (index < 0 || index >= endIndices.length) {
            return -1;
        }

        final int offset;
        if (index == 0) {
            offset = 0;
        } else {
            offset = endIndices[index - 1];
        }
        return globalSlot - offset;
    }
}
