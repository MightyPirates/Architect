package li.cil.architect.common.fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public class FluidHandlerItemStackInInventory implements IFluidHandler {
    private final IFluidHandlerItem fluidHandler;
    private final IItemHandlerModifiable inventory;
    private final int slot;

    public FluidHandlerItemStackInInventory(final IFluidHandlerItem fluidHandler, final IItemHandlerModifiable inventory, final int slot) {
        this.fluidHandler = fluidHandler;
        this.inventory = inventory;
        this.slot = slot;
    }

    // --------------------------------------------------------------------- //
    // IFluidHandler

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return fluidHandler.getTankProperties();
    }

    @Override
    public int fill(final FluidStack resource, final boolean doFill) {
        final int result = fluidHandler.fill(resource, doFill);
        updateInventory();
        return result;
    }

    @Nullable
    @Override
    public FluidStack drain(final FluidStack resource, final boolean doDrain) {
        final FluidStack result = fluidHandler.drain(resource, doDrain);
        updateInventory();
        return result;
    }

    @Nullable
    @Override
    public FluidStack drain(final int maxDrain, final boolean doDrain) {
        final FluidStack result = fluidHandler.drain(maxDrain, doDrain);
        updateInventory();
        return result;
    }

    // --------------------------------------------------------------------- //

    private void updateInventory() {
        if (fluidHandler.getContainer() != inventory.getStackInSlot(slot)) {
            inventory.setStackInSlot(slot, fluidHandler.getContainer());
        }
    }
}
