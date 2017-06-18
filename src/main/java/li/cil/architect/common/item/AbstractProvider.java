package li.cil.architect.common.item;

import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class AbstractProvider extends AbstractItem {
    // --------------------------------------------------------------------- //
    // Computed data.

    // NBT tag names.
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_POSITION = "position";
    private static final String TAG_ENTITY_UUID = "entity";
    private static final String TAG_SIDE = "side";

    // --------------------------------------------------------------------- //

    static {
        MinecraftForge.EVENT_BUS.register(new Object() {
            // We use this event because itemInteractionForEntity only applies to living entities, not minecarts.
            @SubscribeEvent
            public void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
                final EntityPlayer player = event.getEntityPlayer();
                if (!player.isSneaking()) {
                    return;
                }

                final World world = player.world;
                if (world.isRemote) {
                    return;
                }

                final Entity entity = event.getTarget();
                if (entity == null) {
                    return;
                }

                final ItemStack stack = event.getItemStack();
                if (stack.isEmpty()) {
                    return;
                }

                final boolean isValidTarget = stack.getItem() instanceof AbstractProvider &&
                                              ((AbstractProvider) stack.getItem()).isValidTarget(entity);
                if (isValidTarget) {
                    final NBTTagCompound dataNbt = getDataTag(stack);
                    dataNbt.removeTag(TAG_POSITION);
                    dataNbt.removeTag(TAG_SIDE);
                    dataNbt.setInteger(TAG_DIMENSION, world.provider.getDimension());
                    dataNbt.setUniqueId(TAG_ENTITY_UUID, entity.getUniqueID());
                    player.inventory.markDirty();
                    event.setCanceled(true);
                }
            }
        });
    }

    AbstractProvider() {
        setMaxStackSize(1);
    }

    /**
     * Check whether this provider is currently bound to a position.
     *
     * @param stack the provider to check for.
     * @return <code>true</code> if the provider is bound; <code>false</code> otherwise.
     */
    public static boolean isBoundToBlock(final ItemStack stack) {
        final NBTTagCompound dataNbt = getDataTag(stack);
        return dataNbt.hasKey(TAG_DIMENSION, NBT.TAG_INT) &&
               dataNbt.hasKey(TAG_POSITION, NBT.TAG_LONG) &&
               dataNbt.hasKey(TAG_SIDE, NBT.TAG_BYTE);
    }

    /**
     * Check whether this provider is currently bound to an entity.
     *
     * @param stack the provider to check for.
     * @return <code>true</code> if the provider is bound; <code>false</code> otherwise.
     */
    public static boolean isBoundToEntity(final ItemStack stack) {
        final NBTTagCompound dataNbt = getDataTag(stack);
        return dataNbt.hasKey(TAG_DIMENSION, NBT.TAG_INT) &&
               dataNbt.hasUniqueId(TAG_ENTITY_UUID);
    }

    /**
     * Get the dimension the position the provider is bound to is in.
     * <p>
     * Behavior is undefined if {@link #isBoundToBlock(ItemStack)} returns <code>false</code>.
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
     * Behavior is undefined if {@link #isBoundToBlock(ItemStack)} returns <code>false</code>.
     *
     * @param stack the provider to get the position for.
     * @return the position the provider is bound to.
     */
    public static BlockPos getPosition(final ItemStack stack) {
        final NBTTagCompound dataNbt = getDataTag(stack);
        return BlockPos.fromLong(dataNbt.getLong(TAG_POSITION));
    }

    /**
     * Get the entity the provider is bound to.
     * <p>
     * Behavior is undefined if {@link #isBoundToBlock(ItemStack)} returns <code>false</code>.
     *
     * @param stack the provider to get the position for.
     * @param world the world to try to get the entity in.
     * @return the position the provider is bound to.
     */
    @Nullable
    public static Entity getEntity(final ItemStack stack, final World world) {
        final NBTTagCompound dataNbt = getDataTag(stack);
        if (dataNbt.hasUniqueId(TAG_ENTITY_UUID)) {
            final UUID entityId = dataNbt.getUniqueId(TAG_ENTITY_UUID);
            if (world instanceof WorldServer) {
                //noinspection ConstantConditions
                return ((WorldServer) world).getEntityFromUuid(entityId);
            } else {
                for (final Entity entity : world.getLoadedEntityList()) {
                    if (entity.getPersistentID().equals(entityId)) {
                        return entity;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the side of the block the provider is bound to.
     * <p>
     * Behavior is undefined if {@link #isBoundToBlock(ItemStack)} returns <code>false</code>.
     *
     * @param stack the provider to get the bound-to side for.
     * @return the side the provider is bound to.
     */
    public static EnumFacing getSide(final ItemStack stack) {
        final NBTTagCompound dataNbt = getDataTag(stack);
        return EnumFacing.VALUES[dataNbt.getByte(TAG_SIDE) & 0xFF];
    }

    // --------------------------------------------------------------------- //

    abstract public boolean isValidTarget(final TileEntity tileEntity, final EnumFacing side);

    abstract public boolean isValidTarget(final Entity entity);

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
    static <T> List<T> findProviders(final Vec3d consumerPos, final IItemHandler inventory, final Predicate<ItemStack> providerFilter, final BiFunction<ItemStack, TileEntity, T> capabilityGetter, final BiFunction<ItemStack, Entity, T> entityCapabilityGetter) {
        final List<T> result = new ArrayList<>();

        final float rangeSquared = Settings.maxProviderRadius * Settings.maxProviderRadius;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (!providerFilter.test(stack) || !(isBoundToBlock(stack) || isBoundToEntity(stack))) {
                continue;
            }

            final int dimension = getDimension(stack);
            final World world = DimensionManager.getWorld(dimension);
            if (world == null) {
                continue;
            }

            final BlockPos pos;
            final T capability;

            final Entity entity = getEntity(stack, world);
            if (entity != null) {
                pos = entity.getPosition();
            } else {
                pos = getPosition(stack);
            }

            if (consumerPos.squareDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > rangeSquared) {
                continue;
            }

            if (entity != null) {
                capability = entityCapabilityGetter.apply(stack, entity);
            } else {
                if (!world.isBlockLoaded(pos)) {
                    continue;
                }

                final TileEntity tileEntity = world.getTileEntity(pos);
                if (tileEntity == null) {
                    continue;
                }

                capability = capabilityGetter.apply(stack, tileEntity);
            }

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
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<String> tooltip, final ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;
        final FontRenderer fontRenderer = mc.fontRenderer;

        final String info = I18n.format(getTooltip());
        tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH));

        if (isBoundToBlock(stack)) {
            final BlockPos pos = getPosition(stack);
            final int distance = MathHelper.ceil(player.getDistanceSqToCenter(pos));
            tooltip.add(I18n.format(Constants.TOOLTIP_PROVIDER_BLOCK, distance));

            if (flag.isAdvanced() && !player.hasReducedDebug()) {
                tooltip.add(I18n.format(Constants.TOOLTIP_PROVIDER_TARGET, pos.getX(), pos.getY(), pos.getZ()));
            }
        } else if (isBoundToEntity(stack) && world != null) {
            final Entity entity = getEntity(stack, world);
            if (entity != null) {
                final int distance = MathHelper.ceil(player.getDistanceToEntity(entity));
                tooltip.add(I18n.format(Constants.TOOLTIP_PROVIDER_ENTITY, entity.getDisplayName().getFormattedText(), distance));
            }
        }
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null && isValidTarget(tileEntity, side)) {
            if (player.isSneaking() && !world.isRemote) {
                final ItemStack stack = player.getHeldItem(hand);
                final NBTTagCompound dataNbt = getDataTag(stack);
                dataNbt.removeTag(TAG_ENTITY_UUID + "Most");
                dataNbt.removeTag(TAG_ENTITY_UUID + "Least");
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
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote) {
                final NBTTagCompound dataNbt = getDataTag(stack);
                dataNbt.removeTag(TAG_DIMENSION);
                dataNbt.removeTag(TAG_POSITION);
                dataNbt.removeTag(TAG_SIDE);
                dataNbt.removeTag(TAG_ENTITY_UUID + "Most");
                dataNbt.removeTag(TAG_ENTITY_UUID + "Least");
                player.inventory.markDirty();
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public boolean itemInteractionForEntity(final ItemStack stack, final EntityPlayer playerIn, final EntityLivingBase target, final EnumHand hand) {
        // This is required to prevent onItemRightClick being called when interacting with entities.
        return true;
    }
}
