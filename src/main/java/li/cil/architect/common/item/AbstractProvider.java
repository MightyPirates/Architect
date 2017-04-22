package li.cil.architect.common.item;

import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class AbstractProvider extends AbstractItem {
    // --------------------------------------------------------------------- //
    // Computed data.

    // NBT tag names.
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_POSITION = "position";
    private static final String TAG_SIDE = "side";

    // --------------------------------------------------------------------- //

    AbstractProvider() {
        setMaxStackSize(1);
    }

    /**
     * Check whether this provider is currently bound to a position.
     *
     * @param stack the provider to check for.
     * @return <code>true</code> if the provider is bound; <code>false</code> otherwise.
     */
    public static boolean isBound(final ItemStack stack) {
        final NBTTagCompound dataNbt = getDataTag(stack);
        return dataNbt.hasKey(TAG_DIMENSION, NBT.TAG_INT) &&
               dataNbt.hasKey(TAG_POSITION, NBT.TAG_LONG) &&
               dataNbt.hasKey(TAG_SIDE, NBT.TAG_BYTE);
    }

    /**
     * Get the dimension the position the provider is bound to is in.
     * <p>
     * Behavior is undefined if {@link #isBound(ItemStack)} returns <code>false</code>.
     *
     * @param stack the provider to get the dimension for.
     * @return the dimension of the position the provider is bound to.
     */
    public static int getDimension(final ItemStack stack) {
        final NBTTagCompound dataNbt = getDataTag(stack);
        return dataNbt.getInteger(TAG_DIMENSION);
    }

    /**
     * Get the position the provider is bound to.
     * <p>
     * Behavior is undefined if {@link #isBound(ItemStack)} returns <code>false</code>.
     *
     * @param stack the provider to get the position for.
     * @return the position the provider is bound to.
     */
    public static BlockPos getPosition(final ItemStack stack) {
        final NBTTagCompound dataNbt = getDataTag(stack);
        return BlockPos.fromLong(dataNbt.getLong(TAG_POSITION));
    }

    /**
     * Get the side of the block the provider is bound to.
     * <p>
     * Behavior is undefined if {@link #isBound(ItemStack)} returns <code>false</code>.
     *
     * @param stack the provider to get the bound-to side for.
     * @return the side the provider is bound to.
     */
    public static EnumFacing getSide(final ItemStack stack) {
        final NBTTagCompound dataNbt = getDataTag(stack);
        return EnumFacing.VALUES[dataNbt.getByte(TAG_SIDE) & 0xFF];
    }

    // --------------------------------------------------------------------- //

    abstract protected boolean isValidTarget(final TileEntity tileEntity, final EnumFacing side);

    abstract protected String getTooltip();

    /**
     * Get a list of all valid capabilities accessible via providers in the
     * specified inventory, in range of the specified position.
     *
     * @param consumerPos      the position to base range checks on.
     * @param inventory        the inventory to get providers from.
     * @param providerFilter   filter for legal provider stacks.
     * @param capabilityGetter extracts a capability from a tile entity.
     * @return the list of valid capabilities available.
     */
    static <T> List<T> findProviders(final Vec3d consumerPos, final IItemHandler inventory, final Predicate<ItemStack> providerFilter, BiFunction<ItemStack, TileEntity, T> capabilityGetter) {
        final List<T> result = new ArrayList<>();

        final float rangeSquared = Settings.maxProviderRadius * Settings.maxProviderRadius;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (!providerFilter.test(stack) || !isBound(stack)) {
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

            final T capability = capabilityGetter.apply(stack, tileEntity);
            if (capability == null) {
                continue;
            }

            result.add(capability);
        }
        return result;
    }

    // --------------------------------------------------------------------- //
    // Item

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List<String> tooltip, final boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        final String info = I18n.format(getTooltip());
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH));

        if (isBound(stack)) {
            final BlockPos pos = getPosition(stack);
            tooltip.add(I18n.format(Constants.TOOLTIP_PROVIDER_TARGET, pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    @Override
    public EnumActionResult onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null && isValidTarget(tileEntity, side)) {
            if (player.isSneaking() && !world.isRemote) {
                final NBTTagCompound dataNbt = getDataTag(stack);
                dataNbt.setInteger(TAG_DIMENSION, world.provider.getDimension());
                dataNbt.setLong(TAG_POSITION, pos.toLong());
                dataNbt.setByte(TAG_SIDE, (byte) side.getIndex());
                player.inventory.markDirty();
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player, final EnumHand hand) {
        if (player.isSneaking()) {
            if (!world.isRemote) {
                final NBTTagCompound dataNbt = getDataTag(stack);
                dataNbt.removeTag(TAG_DIMENSION);
                dataNbt.removeTag(TAG_POSITION);
                dataNbt.removeTag(TAG_SIDE);
                player.inventory.markDirty();
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}
