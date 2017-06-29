package li.cil.architect.common.init;

import li.cil.architect.api.API;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.ItemProviderFluid;
import li.cil.architect.common.item.ItemProviderItem;
import li.cil.architect.common.item.ItemSketch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Manages setup, registration and lookup of items.
 */
@GameRegistry.ObjectHolder(API.MOD_ID)
public final class Items {
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_SKETCH)
    public static final Item sketch = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_BLUEPRINT)
    public static final Item blueprint = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_PROVIDER_ITEM)
    public static final Item providerItem = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_PROVIDER_FLUID)
    public static final Item providerFluid = null;

    public static List<Item> getAllItems() {
        return Arrays.asList(
                sketch,
                blueprint,
                providerItem,
                providerFluid
        );
    }

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

    public static void register(final IForgeRegistry<Item> registry) {
        registerItem(registry, new ItemSketch(), Constants.NAME_ITEM_SKETCH);
        registerItem(registry, new ItemBlueprint(), Constants.NAME_ITEM_BLUEPRINT);
        registerItem(registry, new ItemProviderItem(), Constants.NAME_ITEM_PROVIDER_ITEM);
        registerItem(registry, new ItemProviderFluid(), Constants.NAME_ITEM_PROVIDER_FLUID);
    }

    // --------------------------------------------------------------------- //

    private static void registerItem(final IForgeRegistry<Item> registry, final Item item, final String name) {
        registry.register(item.
                setUnlocalizedName(API.MOD_ID + "." + name).
                setCreativeTab(API.creativeTab).
                setRegistryName(name));
    }

    private static boolean isItem(final ItemStack stack, final Item item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
