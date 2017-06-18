package li.cil.architect.common.init;

import li.cil.architect.common.ProxyCommon;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.ItemProviderFluid;
import li.cil.architect.common.item.ItemProviderItem;
import li.cil.architect.common.item.ItemSketch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.function.Predicate;

/**
 * Manages setup, registration and lookup of items.
 */
public final class Items {
    public static Item sketch;
    public static Item blueprint;
    public static Item providerItem;
    public static Item providerFluid;

    // --------------------------------------------------------------------- //

    public static boolean isSketch(final ItemStack stack) {
        return isItem(stack, sketch);
    }

    public static boolean isBlueprint(final ItemStack stack) {
        return isItem(stack, blueprint);
    }

    public static boolean isItemProvider(final ItemStack stack) {
        return isItem(stack, providerItem);
    }

    public static boolean isFluidProvider(final ItemStack stack) {
        return isItem(stack, providerFluid);
    }

    public static boolean isProvider(final ItemStack stack) {
        return isItemProvider(stack) || isFluidProvider(stack);
    }

    public static ItemStack getHeldItem(final EntityPlayer player, final Predicate<ItemStack> filter) {
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (filter.test(stack)) {
            return stack;
        }
        stack = player.getHeldItem(EnumHand.OFF_HAND);
        if (filter.test(stack)) {
            return stack;
        }
        return ItemStack.EMPTY;
    }

    // --------------------------------------------------------------------- //

    public static void register(final ProxyCommon proxy) {
        sketch = proxy.registerItem(Constants.NAME_ITEM_SKETCH, ItemSketch::new);
        blueprint = proxy.registerItem(Constants.NAME_ITEM_BLUEPRINT, ItemBlueprint::new);
        providerItem = proxy.registerItem(Constants.NAME_ITEM_PROVIDER_ITEM, ItemProviderItem::new);
        providerFluid = proxy.registerItem(Constants.NAME_ITEM_PROVIDER_FLUID, ItemProviderFluid::new);
    }

    // --------------------------------------------------------------------- //

    private static boolean isItem(final ItemStack stack, final Item item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
