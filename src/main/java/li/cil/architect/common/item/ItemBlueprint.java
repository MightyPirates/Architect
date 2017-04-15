package li.cil.architect.common.item;

import li.cil.architect.common.config.Constants;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;

public final class ItemBlueprint extends AbstractItem {
    // --------------------------------------------------------------------- //
    // Computed data.

    private static final int COSTS_PER_PAGE = 10;
    private static WeakReference<ItemStack> tooltipStack;
    private static long tooltipStart;
    private static long tooltipLast;

    // NBT tag names.
    private static final String TAG_BLUEPRINT = "blueprint";

    // --------------------------------------------------------------------- //

    public ItemBlueprint() {
        setMaxStackSize(1);
    }

    public static BlueprintData getData(final ItemStack stack) {
        final BlueprintData data = new BlueprintData();
        final NBTTagCompound nbt = getDataTag(stack);
        if (nbt.hasKey(TAG_BLUEPRINT, NBT.TAG_COMPOUND)) {
            data.deserializeNBT(nbt.getCompoundTag(TAG_BLUEPRINT));
        }
        return data;
    }

    public static void setData(final ItemStack stack, final BlueprintData data) {
        final NBTTagCompound nbt = getDataTag(stack);
        nbt.setTag(TAG_BLUEPRINT, data.serializeNBT());
    }

    // --------------------------------------------------------------------- //
    // Item

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer playerIn, final List<String> tooltip, final boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        final BlueprintData data = getData(stack);
        final String info = I18n.format(Constants.TOOLTIP_BLUEPRINT);
        tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH));

        if (data.isEmpty()) {
            return;
        }

        final KeyBinding keyBind = Minecraft.getMinecraft().gameSettings.keyBindSneak;
        if (Keyboard.isKeyDown(keyBind.getKeyCode())) {
            addCosts(stack, tooltip, data);
        } else {
            tooltip.add(I18n.format(Constants.TOOLTIP_BLUEPRINT_COSTS_HINT, Keyboard.getKeyName(keyBind.getKeyCode())));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player, final EnumHand hand) {
        if (player.isSneaking()) {
            player.setActiveHand(hand);
        } else if (!world.isRemote) {
            handleInput(player, hand, PlayerUtils.getLookAtPos(player));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!world.isRemote) {
            handleInput(player, hand, pos);
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
                getLookVec().
                scale(-1);
        for (int i = 0; i < 10; i++) {
            final Vec3d lookAt = lookAtBase.addVector(itemRand.nextGaussian(), itemRand.nextGaussian(), itemRand.nextGaussian());
            final Vec3d speed = speedBase.addVector(itemRand.nextGaussian(), itemRand.nextGaussian(), itemRand.nextGaussian());
            player.getEntityWorld().spawnParticle(EnumParticleTypes.PORTAL, lookAt.xCoord, lookAt.yCoord, lookAt.zCoord, speed.xCoord, speed.yCoord, speed.zCoord);
        }
    }

    @Override
    public ItemStack onItemUseFinish(final ItemStack stack, final World world, final EntityLivingBase entity) {
        disableUseAfterConversion();
        return new ItemStack(Items.sketch);
    }

    @Override
    public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    // --------------------------------------------------------------------- //

    @SideOnly(Side.CLIENT)
    private void addCosts(final ItemStack stack, final List<String> tooltip, final BlueprintData data) {
        final List<ItemStack> costs = data.getCosts();
        if (costs.isEmpty()) {
            tooltip.add(I18n.format(Constants.TOOLTIP_BLUEPRINT_COSTS_UNKNOWN));
            return;
        }

        costs.sort(Comparator.comparing(ItemStack::getDisplayName));

        final int pages = MathHelper.ceil(costs.size() / (float) COSTS_PER_PAGE);

        // Rewind to first page if we're showing a tooltip for a different stack
        // or didn't show a tooltip in a while -- avoids page changes soon after
        // starting to show a tooltip, which feels unpolished.
        if (pages > 1) {
            final ItemStack previousStack = tooltipStack != null ? tooltipStack.get() : null;
            if ((System.currentTimeMillis() - tooltipLast) > 100 || previousStack != stack) {
                tooltipStack = new WeakReference<>(stack);
                tooltipStart = System.currentTimeMillis();
            }
            tooltipLast = System.currentTimeMillis();
        }

        final int page = ((int) (System.currentTimeMillis() - tooltipStart) / 2500) % pages;

        if (pages > 1) {
            tooltip.add(I18n.format(Constants.TOOLTIP_BLUEPRINT_COSTS_TITLE_PAGED, page + 1, pages));
        } else {
            tooltip.add(I18n.format(Constants.TOOLTIP_BLUEPRINT_COSTS_TITLE));
        }

        final int offset = page * COSTS_PER_PAGE;
        for (int i = offset, end = Math.min(costs.size(), offset + COSTS_PER_PAGE); i < end; i++) {
            final ItemStack cost = costs.get(i);
            tooltip.add(I18n.format(Constants.TOOLTIP_BLUEPRINT_COSTS_LINE, cost.stackSize, cost.getDisplayName()));
        }
    }

    private void handleInput(final EntityPlayer player, final EnumHand hand, final BlockPos pos) {
        if (isUseDisabled()) {
            return;
        }

        final ItemStack stack = player.getHeldItem(hand);
        assert stack != null;
        final BlueprintData data = getData(stack);
        data.createJobs(player, pos);
    }
}
