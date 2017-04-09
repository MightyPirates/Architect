package li.cil.architect.common.init;

import li.cil.architect.common.Constants;
import li.cil.architect.common.ProxyCommon;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.ItemProvider;
import li.cil.architect.common.item.ItemSketch;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Manages setup, registration and lookup of items.
 */
public final class Items {
    public static Item sketch;
    public static Item blueprint;
    public static Item provider;

    // --------------------------------------------------------------------- //

    public static boolean isSketch(final ItemStack stack) {
        return isItem(stack, sketch);
    }

    public static boolean isBlueprint(final ItemStack stack) {
        return isItem(stack, blueprint);
    }

    public static boolean isProvider(final ItemStack stack) {
        return isItem(stack, provider);
    }

    // --------------------------------------------------------------------- //

    public static void register(final ProxyCommon proxy) {
        sketch = proxy.registerItem(Constants.NAME_ITEM_SKETCH, ItemSketch::new);
        blueprint = proxy.registerItem(Constants.NAME_ITEM_BLUEPRINT, ItemBlueprint::new);
        provider = proxy.registerItem(Constants.NAME_ITEM_PROVIDER, () -> new ItemProvider(1, 1));
    }

    public static void addRecipes() {
        GameRegistry.addRecipe(new ShapelessOreRecipe(
                new ItemStack(sketch, 1),
                net.minecraft.init.Items.BOOK,
                "enderpearl",
                net.minecraft.init.Items.COAL,
                "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(provider, 1),
                "ITI",
                "QHQ",
                "IPI",
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
