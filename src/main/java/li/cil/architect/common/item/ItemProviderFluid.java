package li.cil.architect.common.item;

import li.cil.architect.common.config.Constants;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.integration.railcraft.ProxyRailcraft;
import li.cil.architect.util.FluidHandlerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public final class ItemProviderFluid extends AbstractProvider {
    /**
     * Get a list of all valid fluid handlers accessible via providers in the
     * specified inventory, in range of the specified position.
     *
     * @param consumerPos the position to base range checks on.
     * @param inventory   the inventory to get providers from.
     * @return the list of valid fluid handlers available.
     */
    public static List<IFluidHandler> findProviders(final Vec3d consumerPos, final IItemHandler inventory) {
        return AbstractProvider.findProviders(consumerPos, inventory, Items::isFluidProvider, ItemProviderFluid::getFluidHandlerCapability, ItemProviderFluid::getFluidHandlerCapability);
    }

    // --------------------------------------------------------------------- //
    // AbstractProvider

    @Override
    protected boolean isValidTarget(final TileEntity tileEntity, final EnumFacing side) {
        final IFluidHandler capability = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
        return capability != null && FluidHandlerUtils.canDrain(capability);

    }

    @Override
    protected boolean isValidTarget(final Entity entity) {
        final IFluidHandler capability = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        return capability != null && FluidHandlerUtils.canDrain(capability);

    }

    @Override
    protected String getTooltip() {
        return Constants.TOOLTIP_PROVIDER_FLUID;
    }

    // --------------------------------------------------------------------- //

    @Nullable
    private static IFluidHandler getFluidHandlerCapability(final ItemStack stack, final TileEntity tileEntity) {
        return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getSide(stack));
    }

    @Nullable
    private static IFluidHandler getFluidHandlerCapability(final ItemStack stack, final Entity entity) {
        IFluidHandler fluidHandler = null;
        if (entity instanceof EntityMinecart)
            fluidHandler = ProxyRailcraft.trainHelper.getTrainFluidHandler((EntityMinecart) entity);
        if (fluidHandler == null && entity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            fluidHandler = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        }
        return fluidHandler;
    }
}
