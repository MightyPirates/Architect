package li.cil.architect.api.blueprint;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * An item source provided to {@link Converter}s when deserializing blocks.
 */
public interface ItemSource {
    /**
     * Whether this is a creative mode item source.
     * <p>
     * When this is <code>true</code>, the {@link Converter} should generally
     * not consume any materials. The {@link #extract(ItemStack)} method
     * automatically performs this check and will always succeed when in
     * creative mode.
     *
     * @return whether this is a creative item source.
     */
    boolean isCreative();

    /**
     * Get the underlying item handler feeding this item source.
     * <p>
     * Use this only if {@link #extract(ItemStack)} does not work for the type
     * of item you need to look up. If you do, however, make sure to respect
     * the current {@link #isCreative()} state.
     *
     * @return the underlying item handler.
     */
    IItemHandler getItemHandler();

    /**
     * Try to extract the specified item stack from the underlying item handler.
     * <p>
     * This will look for a stack that is both item and tag equals to the
     * specified stack. The number of items consumed is defined by the size
     * of the provided stack.
     *
     * @param stack the type of item to look for.
     * @return the extracted item stack.
     */
    ItemStack extract(final ItemStack stack);
}
