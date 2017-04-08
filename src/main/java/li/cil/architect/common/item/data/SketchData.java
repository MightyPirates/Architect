package li.cil.architect.common.item.data;

import li.cil.architect.api.BlueprintAPI;
import li.cil.architect.common.Settings;
import li.cil.architect.util.AxisAlignedBBUtils;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Data representing a currently being edited sketch.
 * <p>
 * As such, this merely consists of an origin coordinate (minimum corner of the
 * bounding box around the being edited sketch), and the set positions,
 * relative to the origin coordinate. No actual block state data is stored here.
 */
public final class SketchData extends AbstractPatternData implements INBTSerializable<NBTTagCompound> {
    // --------------------------------------------------------------------- //
    // Computed data.

    private static final int WORLD_RADIUS = 30000000;
    private static final AxisAlignedBB WORLD_BOUNDS = new AxisAlignedBB(-WORLD_RADIUS, -WORLD_RADIUS, -WORLD_RADIUS, WORLD_RADIUS, WORLD_RADIUS, WORLD_RADIUS);

    // NBT tag names.
    private static final String TAG_BLOCKS = "blocks";
    private static final String TAG_ORIGIN = "origin";

    // --------------------------------------------------------------------- //
    // Persisted data.

    private final BitSet blocks = new BitSet((1 << (NUM_BITS * 3)) - 1);
    @Nullable
    private BlockPos origin;
    @Nullable
    private AxisAlignedBB bounds;

    // --------------------------------------------------------------------- //

    /**
     * Whether the sketch is currently empty, i.e. not a single block is set.
     *
     * @return <code>true</code> if the sketch is empty;
     * <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return origin == null;
    }

    /**
     * Get the current origin of the sketch in world space.
     *
     * @return the origin of the sketch.
     */
    @Nullable
    public BlockPos getOrigin() {
        return origin;
    }

    /**
     * Get the current bounds of the sketch in world space.
     * <p>
     * May be <code>null</code> if no block is set in the sketch.
     *
     * @return the bounds of the sketch.
     */
    @Nullable
    public AxisAlignedBB getBounds() {
        return bounds;
    }

    /**
     * Checks whether the position can be added to this sketch.
     *
     * @param pos the position to check.
     * @return <code>true</code> if it can be added;
     * <code>false</code> otherwise.
     */
    public boolean isValid(final BlockPos pos) {
        return getPotentialBounds().intersectsWith(new AxisAlignedBB(pos));
    }

    /**
     * Get the <em>potential</em> bounds of the sketch in world space.
     * <p>
     * This is the bounds box indicating in what are blocks may be added to the
     * sketch without it exceeding the maximum blueprint size.
     *
     * @return the potential bounds of the sketch.
     */
    public AxisAlignedBB getPotentialBounds() {
        if (isEmpty()) {
            return WORLD_BOUNDS;
        }

        assert origin != null;
        assert bounds != null;

        final int max = Settings.maxBlueprintSize;
        final Vec3i size = AxisAlignedBBUtils.getBlockSize(bounds);
        return bounds.expand(max - size.getX(), max - size.getY(), max - size.getZ());
    }

    /**
     * Get the <em>potential</em> bounds of the sketch in world space,
     * assuming the specified position is also added to the sketch.
     * <p>
     * This is the bounds box indicating in what are blocks may be added to the
     * sketch without it exceeding the maximum blueprint size.
     * <p>
     * This variant is used to determine the potential bounds during a range
     * selection (where the specified position is the start position of the
     * range).
     *
     * @param including the position to assume also to belong to the sketch.
     * @return the potential bounds of the extended sketch.
     * @throws IllegalArgumentException if the specified coordinate does not
     *                                  lie in the bounds returned by
     *                                  {@link #getPotentialBounds()}.
     */
    public AxisAlignedBB getPotentialBounds(final BlockPos including) {
        final AxisAlignedBB extraBounds = new AxisAlignedBB(including);
        if (!getPotentialBounds().intersectsWith(extraBounds)) {
            throw new IllegalArgumentException();
        }

        final AxisAlignedBB mainBounds;
        if (isEmpty()) {
            mainBounds = extraBounds;
        } else {
            assert bounds != null;
            mainBounds = bounds.union(extraBounds);
        }

        final int max = Settings.maxBlueprintSize;
        final int sx = (int) (mainBounds.maxX - mainBounds.minX);
        final int sy = (int) (mainBounds.maxY - mainBounds.minY);
        final int sz = (int) (mainBounds.maxZ - mainBounds.minZ);
        return mainBounds.expand(max - sx, max - sy, max - sz);
    }

    /**
     * Get whether the specified block position in world space is set in this
     * sketch.
     *
     * @param pos the position to check.
     * @return <code>true</code> if the position is set; <code>false</code> otherwise.
     */
    public boolean isSet(final BlockPos pos) {
        if (isEmpty()) {
            return false;
        }

        assert origin != null;
        assert bounds != null;

        if (!bounds.intersectsWith(new AxisAlignedBB(pos))) {
            return false;
        }

        final BlockPos relPos = pos.subtract(origin);
        return blocks.get(toIndex(relPos));
    }

    /**
     * Toggle a block in the sketch.
     * <p>
     * This can fail if the specified position is outside the potential bounds
     * of the sketch, in which case this will return <code>false</code>.
     *
     * @param world the world containing the position to add.
     * @param pos   the position of the block to toggle.
     * @return <code>true</code> if the change was applied; <code>false</code> otherwise.
     */
    public boolean toggle(final World world, final BlockPos pos) {
        if (isSet(pos)) {
            return reset(pos);
        } else {
            return set(world, pos);
        }
    }

    /**
     * Add the specified block position in world space the sketch.
     * <p>
     * This can fail if the specified position is outside the potential bounds
     * of the sketch, in which case this will return <code>false</code>.
     *
     * @param world the world containing the position to add.
     * @param pos   the position of the block to add.
     * @return <code>true</code> if the position was added; <code>false</code> otherwise.
     */
    public boolean set(final World world, final BlockPos pos) {
        if (!BlueprintAPI.canSerialize(world, pos)) {
            return false;
        }

        if (isEmpty()) {
            blocks.set(0); // Origin is always the first bit.
            origin = pos;
            bounds = new AxisAlignedBB(pos);
            return true;
        }

        assert origin != null;
        assert bounds != null;

        if (!isValid(pos)) {
            return false;
        }

        final AxisAlignedBB posBounds = new AxisAlignedBB(pos);
        final BlockPos relPos = pos.subtract(origin);

        // If position falls within current bounds we can just set it.
        if (bounds.intersectsWith(posBounds)) {
            blocks.set(toIndex(relPos));
            return true;
        }

        // Otherwise we need to grow our bounds, check which way.
        final BlockPos delta = pos.subtract(origin);
        final BlockPos clampedDelta = new BlockPos(Math.min(0, delta.getX()), Math.min(0, delta.getY()), Math.min(0, delta.getZ()));
        if (clampedDelta.getX() >= 0 && clampedDelta.getY() >= 0 && clampedDelta.getZ() >= 0) {
            // Positive, just grow the bounds.
            blocks.set(toIndex(relPos));
            bounds = bounds.union(posBounds);
        } else {
            // Negative, need to move the origin.
            shiftOrigin(clampedDelta);
            blocks.set(toIndex(relPos.subtract(clampedDelta)));
        }

        return true;
    }

    /**
     * Remove the specified block position in world space from the sketch.
     *
     * @param pos the position of the block to remove.
     * @return <code>true</code> if the position was removed; <code>false</code> otherwise.
     */
    public boolean reset(final BlockPos pos) {
        if (isEmpty()) {
            return false;
        }

        assert origin != null;
        assert bounds != null;

        // Check if coordinate is even contained in this sketch.
        final AxisAlignedBB posBounds = new AxisAlignedBB(pos);
        if (!bounds.intersectsWith(posBounds)) {
            return false;
        }

        // Check if this is the last position in this sketch.
        if (bounds.equals(posBounds)) {
            blocks.clear();
            origin = null;
            bounds = null;
            return true;
        }

        final BlockPos relPos = pos.subtract(origin);
        blocks.clear(toIndex(relPos));

        // Small performance early exit -- no need to change bounds if the
        // removed pos was an "inner" coordinate, i.e. it wasn't on the hull
        // of the sketch's bounds.
        if (relPos.getX() != bounds.minX && relPos.getX() != bounds.maxX &&
            relPos.getY() != bounds.minY && relPos.getY() != bounds.maxY &&
            relPos.getZ() != bounds.minZ && relPos.getZ() != bounds.maxZ) {
            return true;
        }

        // Check if our bounds changed.
        final AxisAlignedBB newBounds = computeBounds();
        if (newBounds.equals(bounds)) {
            return true;
        }

        // They did, check which way.
        final BlockPos oldMin = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
        final BlockPos newMin = new BlockPos(newBounds.minX, newBounds.minY, newBounds.minZ);
        final BlockPos minDelta = newMin.subtract(oldMin);
        if (minDelta.getX() != 0 && minDelta.getY() != 0 && minDelta.getZ() != 0) {
            // Minimum changed, origin needs adjusting.
            shiftOrigin(minDelta);
            assert bounds.equals(newBounds);
        } else {
            // Maximum changed, just set the new bounds.
            bounds = newBounds;
        }

        return true;
    }

    /**
     * Get a stream over all block positions currently set in this sketch.
     *
     * @return a stream of all positions in this sketch.
     */
    public Stream<BlockPos> getBlocks() {
        return StreamSupport.stream(new BlockSpliterator(this), false);
    }

    // --------------------------------------------------------------------- //
    // INBTSerializable

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound nbt = new NBTTagCompound();
        if (origin != null) {
            nbt.setTag(TAG_BLOCKS, new NBTTagByteArray(blocks.toByteArray()));
            nbt.setLong(TAG_ORIGIN, origin.toLong());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        blocks.clear();
        origin = null;
        bounds = null;

        if (!nbt.hasKey(TAG_ORIGIN, Constants.NBT.TAG_LONG) || !nbt.hasKey(TAG_BLOCKS, Constants.NBT.TAG_BYTE_ARRAY)) {
            return;
        }

        final BitSet loaded = BitSet.valueOf(nbt.getByteArray(TAG_BLOCKS));
        if (loaded.length() > blocks.size()) {
            loaded.clear(blocks.size(), loaded.size() - 1);
        }
        blocks.or(loaded);
        if (blocks.cardinality() > 0) { // Just in case...
            origin = BlockPos.fromLong(nbt.getLong(TAG_ORIGIN));
            bounds = computeBounds(); // Requires origin to be set.
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Shift the origin by the specified delta, adjusting all relative block
     * coordinates currently contained in the sketch, as well as the bounds
     * and of course the origin itself.
     *
     * @param delta the delta by which to shift the origin.
     */
    private void shiftOrigin(final BlockPos delta) {
        assert origin != null;
        assert bounds != null;

        // Shift coordinates by negative delta so they're relative to the new
        // origin. Do this before setting the origin to its new value, because
        // toIndex and fromIndex use the origin field in their computations.
        final BitSet newData = new BitSet(blocks.size());
        for (int index = blocks.nextSetBit(0); index >= 0; index = blocks.nextSetBit(index + 1)) {
            newData.set(toIndex(fromIndex(index).subtract(delta)));

            assert index != Integer.MAX_VALUE;
        }

        blocks.clear();
        blocks.or(newData);
        origin = origin.add(delta);
        bounds = new AxisAlignedBB(bounds.minX + delta.getX(), bounds.minY + delta.getY(), bounds.minZ + delta.getZ(), bounds.maxX, bounds.maxY, bounds.maxZ);
    }

    /**
     * Recompute the world space bounds of this sketch from scratch.
     *
     * @return the bounds of this sketch.
     */
    private AxisAlignedBB computeBounds() {
        assert origin != null;
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

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(origin);
    }

    // --------------------------------------------------------------------- //

    private static final class BlockSpliterator extends Spliterators.AbstractSpliterator<BlockPos> {
        private final SketchData data;
        private int index = -1;

        BlockSpliterator(final SketchData data) {
            super(data.blocks.cardinality(), SIZED);
            this.data = data;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super BlockPos> action) {
            if (data.origin != null && (index = data.blocks.nextSetBit(index + 1)) >= 0) {
                action.accept(fromIndex(index).add(data.origin));
                return true;
            }
            return false;
        }
    }
}
