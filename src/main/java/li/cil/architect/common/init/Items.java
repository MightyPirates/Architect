package li.cil.architect.common.init;

import li.cil.architect.common.ProxyCommon;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.ItemProviderItem;
import li.cil.architect.common.item.ItemSketch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.function.Predicate;

/**
 * Manages setup, registration and lookup of items.
 */
public final class Items {
    public static Item sketch;
    public static Item blueprint;
    public static Item providerItem;

    // --------------------------------------------------------------------- //

    public static boolean isSketch(final ItemStack stack) {
        return isItem(stack, sketch);
    }

    public static boolean isBlueprint(final ItemStack stack) {
        return isItem(stack, blueprint);
    }

    public static boolean isProvider(final ItemStack stack) {
        return isItem(stack, providerItem);
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
    }

    public static void addRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(sketch, 1),
                "LEL",
                "PPS",
                "LLL",
                'E', "enderpearl",
                'P', "paper",
                'L', "leather",
                'S', "string"));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(providerItem, 1),
                "IPI",
                "QHQ",
                "ITI",
                'I', "ingotIron",
                'P', "enderpearl",
                'Q', "gemQuartz",
                'T', Blocks.TRAPDOOR,
                'H', Blocks.HOPPER));
    }

    // --------------------------------------------------------------------- //

    private static boolean isItem(final ItemStack stack, final Item item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
