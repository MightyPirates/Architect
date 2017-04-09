package li.cil.architect.common.item;

import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import li.cil.architect.common.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public final class ItemItemProvider extends AbstractProvider {
    /**
     * Get a list of all valid item-handlers accessible via providers in the
     * specified inventory, in range of the specified position.
     *
     * @param consumerPos the position to base range checks on.
     * @param inventory   the inventory to get providers from.
     * @return the list of valid item handlers available.
     */
    public static List<IItemHandler> findProviders(final Vec3d consumerPos, final IItemHandler inventory) {
        final List<IItemHandler> result = new ArrayList<>();

        final float rangeSquared = Settings.maxProviderRadius * Settings.maxProviderRadius;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (!Items.isProvider(stack) || !isBound(stack)) {
                continue;
            }

            final int dimension = getDimension(stack);
            final World world = DimensionManager.getWorld(dimension);
            if (world == null) {
                continue;
            }

            final BlockPos pos = getPosition(stack);
            if (consumerPos.squareDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > rangeSquared) {
                continue;
            }
            if (!world.isBlockLoaded(pos)) {
                continue;
            }

            final TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity == null) {
                continue;
            }

            final IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getSide(stack));
            if (itemHandler == null) {
                continue;
            }

            result.add(itemHandler);
        }
        return result;
    }

    // --------------------------------------------------------------------- //
    // AbstractProvider

    @Override
    protected boolean isValidTarget(final TileEntity tileEntity, final EnumFacing side) {
        return tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    }

    @Override
    protected String getTooltip() {
        return Constants.TOOLTIP_PROVIDER_ITEM;
    }
}
