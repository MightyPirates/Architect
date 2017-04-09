package li.cil.architect.common.item.data;

import li.cil.architect.common.config.Settings;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.BitSet;

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

    /**
     * Recompute the world space bounds of the relative positions defined by
     * the specified bit set (each set bit being a compressed position).
     *
     * @return the bounds of this sketch.
     */
    static AxisAlignedBB computeBounds(final BitSet blocks) {
        assert blocks.cardinality() > 0;

        int index = blocks.nextSetBit(0);
        BlockPos pos = fromIndex(index);
        int minX = pos.getX(), minY = pos.getY(), minZ = pos.getZ(), maxX = pos.getX() + 1, maxY = pos.getY() + 1, maxZ = pos.getZ() + 1;
        while ((index = blocks.nextSetBit(index + 1)) >= 0) {
            pos = fromIndex(index);
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX() + 1);
            maxY = Math.max(maxY, pos.getY() + 1);
            maxZ = Math.max(maxZ, pos.getZ() + 1);

            assert index != Integer.MAX_VALUE;
        }

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
