package li.cil.architect.common.item;

import li.cil.architect.common.Constants;
import li.cil.architect.common.blueprint.JobManagerClient;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public final class ItemBlueprint extends AbstractPatternItem {
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
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        final BlueprintData data = getData(stack);
        final String info = I18n.format(Constants.TOOLTIP_BLUEPRINT);
        tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH));

        // TODO Costs while sneaking.
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        if (player.isSneaking()) {
            player.setActiveHand(hand);
        } else if (!world.isRemote) {
            handleInput(player, hand, PlayerUtils.getLookAtPos(player));
        } else {
            JobManagerClient.INSTANCE.setJobsDirty();
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!world.isRemote) {
            handleInput(player, hand, pos);
        } else {
            JobManagerClient.INSTANCE.setJobsDirty();
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public int getMaxItemUseDuration(final ItemStack stack) {
        return 60;
    }

    @Override
    public ItemStack onItemUseFinish(final ItemStack stack, final World world, final EntityLivingBase entity) {
        return new ItemStack(Items.sketch);
    }

    @Override
    public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    // --------------------------------------------------------------------- //

    private void handleInput(final EntityPlayer player, final EnumHand hand, final BlockPos pos) {
        final World world = player.getEntityWorld();
        final ItemStack stack = player.getHeldItem(hand);
        final BlueprintData data = getData(stack);
        data.createJobs(world, pos);
    }
}
