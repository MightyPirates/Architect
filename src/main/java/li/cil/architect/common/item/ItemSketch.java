package li.cil.architect.common.item;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.common.item.data.SketchData;
import li.cil.architect.util.AxisAlignedBBUtils;
import li.cil.architect.util.BlockPosUtils;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public final class ItemSketch extends AbstractItem {
    // --------------------------------------------------------------------- //
    // Computed data.

    // NBT tag names.
    private static final String TAG_RANGE_START = "rangeStart";
    private static final String TAG_SKETCH = "sketch";

    // --------------------------------------------------------------------- //

    public ItemSketch() {
        setMaxStackSize(1);
    }

    public static SketchData getData(final ItemStack stack) {
        final SketchData data = new SketchData();
        final NBTTagCompound nbt = getDataTag(stack);
        if (nbt.hasKey(TAG_SKETCH, NBT.TAG_COMPOUND)) {
            data.deserializeNBT(nbt.getCompoundTag(TAG_SKETCH));
        }
        return data;
    }

    public static boolean hasRangeSelection(final ItemStack stack) {
        return getDataTag(stack).hasKey(TAG_RANGE_START, NBT.TAG_LONG);
    }

    @Nullable
    public static AxisAlignedBB getRangeSelection(final ItemStack stack, final BlockPos pos) {
        if (!hasRangeSelection(stack)) {
            return null;
        }
        final BlockPos startPos = BlockPos.fromLong(getDataTag(stack).getLong(TAG_RANGE_START));
        final SketchData data = getData(stack);

        // Validate, just in case of tampered NBT, weird state glitches, etc.
        if (!data.isValid(startPos)) {
            clearRangeSelection(stack);
            return null;
        }

        // Clamp range to allowed bounds of sketch.
        final AxisAlignedBB potentialBounds = data.getPotentialBounds(startPos);
        final BlockPos clampedEnd = BlockPosUtils.clamp(pos, potentialBounds);

        return new AxisAlignedBB(startPos).union(new AxisAlignedBB(clampedEnd));
    }

    // --------------------------------------------------------------------- //
    // Item

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer playerIn, final List<String> tooltip, final boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        final SketchData data = getData(stack);
        if (!data.isEmpty()) {
            final String info = I18n.format(Constants.TOOLTIP_SKETCH_CONVERT);
            tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH));

            final AxisAlignedBB bounds = data.getBounds();
            assert bounds != null;
            tooltip.add(I18n.format(Constants.TOOLTIP_SKETCH_BOUNDS, (int) bounds.minX, (int) bounds.minY, (int) bounds.minZ, (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ));
        } else {
            final String info = I18n.format(Constants.TOOLTIP_SKETCH_EMPTY);
            tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player, final EnumHand hand) {
        if (!player.isSneaking() && !hasRangeSelection(stack) && !getData(stack).isEmpty()) {
            player.setActiveHand(hand);
        } else if (!world.isRemote) {
            handleInput(player, hand, PlayerUtils.getLookAtPos(player), false);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!world.isRemote) {
            handleInput(player, hand, pos, true);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public int getMaxItemUseDuration(final ItemStack stack) {
        return 30;
    }

    @Override
    public void onUsingTick(final ItemStack stack, final EntityLivingBase player, final int count) {
        final Vec3d lookAtBase = player.
                getPositionEyes(1).
                add(player.getLookVec());
        final Vec3d speedBase = player.
                getLookVec();
        for (int i = 0; i < 10; i++) {
            final Vec3d lookAt = lookAtBase.addVector(itemRand.nextGaussian(), itemRand.nextGaussian(), itemRand.nextGaussian());
            final Vec3d speed = speedBase.addVector(itemRand.nextGaussian(), itemRand.nextGaussian(), itemRand.nextGaussian());
            player.getEntityWorld().spawnParticle(EnumParticleTypes.PORTAL, lookAt.xCoord, lookAt.yCoord, lookAt.zCoord, speed.xCoord, speed.yCoord, speed.zCoord);
        }
    }

    // TODO Convert blocks continuously while using the sketch to balance the serialization across several frames.

//    @Override
//    public void onUsingTick(final ItemStack stack, final EntityLivingBase player, final int count) {
//        super.onUsingTick(stack, player, count);
//    }

//    @Override
//    public void onPlayerStoppedUsing(final ItemStack stack, final World worldIn, final EntityLivingBase entityLiving, final int timeLeft) {
//        super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
//    }

    @Override
    public ItemStack onItemUseFinish(final ItemStack stack, final World world, final EntityLivingBase entity) {
        final BlueprintData.Builder builder = new BlueprintData.Builder();

        final SketchData sketchData = getData(stack);
        final BlockPos origin = sketchData.getOrigin();
        if (origin == null) {
            return stack;
        }

        sketchData.getBlocks().forEach(pos -> {
            final NBTTagCompound nbt = ConverterAPI.serialize(world, pos);
            if (nbt != null) {
                builder.add(pos.subtract(origin), nbt);
            }
        });

        final ItemStack result = new ItemStack(Items.blueprint);
        ItemBlueprint.setData(result, builder.getData(origin));
        ItemBlueprint.setColor(result, EnumDyeColor.byMetadata(world.rand.nextInt(16)));
        disableUseAfterConversion();
        return result;
    }

    @Override
    public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    // --------------------------------------------------------------------- //

    private static void setData(final ItemStack stack, final SketchData data) {
        final NBTTagCompound nbt = getDataTag(stack);
        nbt.setTag(TAG_SKETCH, data.serializeNBT());
    }

    private static boolean beginRangeSelection(final ItemStack stack, final BlockPos pos) {
        final SketchData data = getData(stack);
        if (data.isValid(pos)) {
            getDataTag(stack).setLong(TAG_RANGE_START, pos.toLong());
            return true;
        } else {
            return false;
        }
    }

    private static void clearRangeSelection(final ItemStack stack) {
        getDataTag(stack).removeTag(TAG_RANGE_START);
    }

    private void handleInput(final EntityPlayer player, final EnumHand hand, final BlockPos pos, final boolean canToggleSingle) {
        if (isUseDisabled()) {
            return;
        }

        final World world = player.getEntityWorld();
        final ItemStack stack = player.getHeldItem(hand);
        assert stack != null;
        if (hasRangeSelection(stack)) {
            final AxisAlignedBB range = getRangeSelection(stack, pos);
            if (range == null) {
                return;
            }

            final SketchData data = getData(stack);
            if (player.isSneaking()) {
                AxisAlignedBBUtils.getTouchedBlocks(range).forEach(data::reset);
            } else {
                AxisAlignedBBUtils.getTouchedBlocks(range).forEach(pos1 -> data.set(world, pos1));
            }

            clearRangeSelection(stack);

            setData(stack, data);
            player.inventory.markDirty();
        } else {
            if (player.isSneaking()) {
                if (beginRangeSelection(stack, pos)) {
                    player.inventory.markDirty();
                }
            } else if (canToggleSingle) {
                final SketchData data = getData(stack);
                if (data.toggle(world, pos)) {
                    setData(stack, data);
                    player.inventory.markDirty();
                }
            }
        }
    }
}
