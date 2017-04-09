package li.cil.architect.common.converter;

import li.cil.architect.api.converter.MaterialSource;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public final class MaterialSourceImpl implements MaterialSource {
    private final boolean isCreative;
    private final IItemHandler handler;

    public MaterialSourceImpl(final boolean isCreative, final IItemHandler handler) {
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
    public ItemStack extractItem(final ItemStack wantStack) {
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

    @Override
    public IFluidHandler getFluidHandler() {
        return EmptyFluidHandler.INSTANCE;
    }

    @Override
    @Nullable
    public FluidStack extractFluid(final FluidStack stack) {
        if (isCreative) {
            return stack.copy();
        }

        return null;
    }
}
