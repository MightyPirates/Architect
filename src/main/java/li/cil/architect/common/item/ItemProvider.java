package li.cil.architect.common.item;

import li.cil.architect.common.Constants;
import li.cil.architect.common.Settings;
import li.cil.architect.common.blueprint.ProviderEntityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.List;

public final class ItemProvider extends Item {
    private static final String TAG_ENABLED = "enabled";

    private final float radiusMultiplier;
    private final float operationsPerTickMultiplier;

    // --------------------------------------------------------------------- //

    public ItemProvider(final float radiusMultiplier, final float operationsPerTickMultiplier) {
        this.radiusMultiplier = radiusMultiplier;
        this.operationsPerTickMultiplier = operationsPerTickMultiplier;
    }

    public static boolean isEnabled(final ItemStack stack) {
        final NBTTagCompound nbt = stack.getTagCompound();
        return nbt == null || nbt.getBoolean(TAG_ENABLED);
    }

    public static void setEnabled(final ItemStack stack, final boolean value) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        nbt.setBoolean(TAG_ENABLED, value);
    }

    // --------------------------------------------------------------------- //
    // Item

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List<String> tooltip, final boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        final String info = I18n.format(Constants.TOOLTIP_PROVIDER);
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, li.cil.architect.common.Constants.MAX_TOOLTIP_WIDTH));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            setEnabled(stack, !isEnabled(stack));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUpdate(final ItemStack stack, final World world, final Entity entity, final int itemSlot, final boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);
        if (entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            final float radius = MathHelper.clamp(Settings.maxProviderEntityRadius * radiusMultiplier, 1, 256);
            final int operationsPerTick = MathHelper.clamp((int) (Settings.maxProviderEntityOperationsPerTick * operationsPerTickMultiplier), 1, 64);
            ProviderEntityManager.INSTANCE.updateEntity(entity, radius, operationsPerTick);
        }
    }
}
