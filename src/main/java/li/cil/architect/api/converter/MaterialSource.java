package li.cil.architect.api.converter;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * A material source provided to {@link Converter}s when deserializing blocks.
 */
public interface MaterialSource {
    /**
     * Whether this is a creative mode item source.
     * <p>
     * When this is <code>true</code>, the {@link Converter} should generally
     * not consume any materials. The {@link #extractItem(ItemStack)} method
     * automatically performs this check and will always succeed when in
     * creative mode.
     *
     * @return whether this is a creative item source.
     */
    boolean isCreative();

    /**
     * Get the underlying item handler feeding this material source.
     * <p>
     * Use this only if {@link #extractItem(ItemStack)} does not work for the
     * type of item you need to look up. If you do, however, make sure to
     * respect the current {@link #isCreative()} state.
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
    ItemStack extractItem(final ItemStack stack);

    /**
     * Get the underlying fluid handler feeding this material source.
     * <p>
     * Use this only if {@link #extractFluid(FluidStack)} does not work for the
     * type of fluid you need to look up. If you do, however, make sure to
     * respect the current {@link #isCreative()} state.
     *
     * @return the underlying fluid handler.
     */
    IFluidHandler getFluidHandler();

    /**
     * Try to extract the specified fluid stack from the underlying fluid
     * handler.
     * <p>
     * This will look for a stack that is both fluid and tag equals to the
     * specified stack. The amount of fluid consumed is defined by the size
     * of the provided stack.
     *
     * @param stack the type of fluid to look for.
     * @return the extracted fluid stack.
     */
    @Nullable
    FluidStack extractFluid(final FluidStack stack);
}
