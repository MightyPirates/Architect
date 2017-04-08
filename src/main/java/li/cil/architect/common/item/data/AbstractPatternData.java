package li.cil.architect.common.item.data;

import li.cil.architect.common.Settings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

abstract class AbstractPatternData {
    // --------------------------------------------------------------------- //
    // Computed data.

    static final int NUM_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(Settings.maxBlueprintSize));
    private static final int Y_SHIFT = NUM_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_BITS;
    private static final int MASK = (1 << NUM_BITS) - 1;

    // --------------------------------------------------------------------- //

    /**
     * Compute the index of the bit representing the set state for the specified
     * block position in local space.
     *
     * @param pos the position to get the index for.
     * @return the index of the specified position.
     */
    static int toIndex(final BlockPos pos) {
        assert pos.getX() < Settings.maxBlueprintSize;
        assert pos.getY() < Settings.maxBlueprintSize;
        assert pos.getZ() < Settings.maxBlueprintSize;
        return (pos.getX() << X_SHIFT) | (pos.getY() << Y_SHIFT) | pos.getZ();
    }

    /**
     * Compute the block position in local space from the specified bit index.
     *
     * @param index the index to get the position for.
     * @return the block position the specified index represents.
     */
    static BlockPos fromIndex(final int index) {
        return new BlockPos((index >>> X_SHIFT) & MASK, ((index >>> Y_SHIFT) & MASK), index & MASK);
    }
}
