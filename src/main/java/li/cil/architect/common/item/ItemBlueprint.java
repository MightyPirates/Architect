package li.cil.architect.common.item;

import li.cil.architect.client.gui.GuiId;
import li.cil.architect.common.Architect;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageBlueprintPlace;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.lang.ref.WeakReference;
import java.util.List;

public final class ItemBlueprint extends AbstractItem {
    // --------------------------------------------------------------------- //
    // Computed data.

    private static final int COSTS_PER_PAGE = 10;
    private static WeakReference<ItemStack> tooltipStack;
    private static List<String> tooltipCosts;
    private static long tooltipStart;
    private static long tooltipLast;

    // NBT tag names.
    private static final String TAG_BLUEPRINT = "blueprint";
    private static final String TAG_COLOR = "color";

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

    public static void setColor(final ItemStack stack, final EnumDyeColor color) {
        final NBTTagCompound nbt = getDataTag(stack);
        nbt.setByte(TAG_COLOR, (byte) color.getMetadata());
    }

    public static EnumDyeColor getColor(final ItemStack stack) {
        final NBTTagCompound nbt = getDataTag(stack);
        return EnumDyeColor.byMetadata(nbt.getByte(TAG_COLOR));
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
            player.openGui(Architect.instance, GuiId.BLUEPRINT.ordinal(), world, 0, 0, 0);
        } else if (world.isRemote) {
            handleInput(hand, PlayerUtils.getLookAtPos(player));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (world.isRemote) {
            handleInput(hand, pos);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem() || getColor(oldStack) != getColor(newStack);
    }

    // --------------------------------------------------------------------- //

    @SideOnly(Side.CLIENT)
    private void addCosts(final ItemStack stack, final List<String> tooltip, final BlueprintData data) {
        final ItemStack previousStack = tooltipStack != null ? tooltipStack.get() : null;
        if (previousStack != stack) {
            tooltipCosts = data.getCosts();
        }

        if (tooltipCosts == null || tooltipCosts.isEmpty()) {
            tooltip.add(I18n.format(Constants.TOOLTIP_BLUEPRINT_COSTS_UNKNOWN));
            return;
        }

        final int pages = MathHelper.ceil(tooltipCosts.size() / (float) COSTS_PER_PAGE);

        // Rewind to first page if we're showing a tooltip for a different stack
        // or didn't show a tooltip in a while -- avoids page changes soon after
        // starting to show a tooltip, which feels unpolished.
        if ((System.currentTimeMillis() - tooltipLast) > 100 || previousStack != stack) {
            tooltipStack = new WeakReference<>(stack);
            tooltipStart = System.currentTimeMillis();
        }
        tooltipLast = System.currentTimeMillis();

        final int page = ((int) (System.currentTimeMillis() - tooltipStart) / 2500) % pages;

        if (pages > 1) {
            tooltip.add(I18n.format(Constants.TOOLTIP_BLUEPRINT_COSTS_TITLE_PAGED, page + 1, pages));
        } else {
            tooltip.add(I18n.format(Constants.TOOLTIP_BLUEPRINT_COSTS_TITLE));
        }

        final int offset = page * COSTS_PER_PAGE;
        for (int i = offset, end = Math.min(tooltipCosts.size(), offset + COSTS_PER_PAGE); i < end; i++) {
            tooltip.add(tooltipCosts.get(i));
        }
    }

    private void handleInput(final EnumHand hand, final BlockPos pos) {
        if (isUseDisabled()) {
            return;
        }

        Network.INSTANCE.getWrapper().sendToServer(new MessageBlueprintPlace(hand, pos, Settings.allowPlacePartial));
    }
}
