package li.cil.architect.common.item.data;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import li.cil.architect.api.BlueprintAPI;
import li.cil.architect.common.blueprint.JobManager;
import li.cil.architect.util.AxisAlignedBBUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class BlueprintData extends AbstractPatternData implements INBTSerializable<NBTTagCompound> {
    // --------------------------------------------------------------------- //
    // Computed data.

    private static final AxisAlignedBB EMPTY_AABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    // NBT tag names.
    private static final String TAG_BLOCK_DATA = "data";
    private static final String TAG_BLOCKS = "blocks";
    private static final String TAG_SHIFT = "shift";
    private static final String TAG_ROTATION = "rotation";

    // --------------------------------------------------------------------- //
    // Persisted data.

    private final List<NBTTagCompound> blockData = new ArrayList<>();
    private final TIntIntMap blocks = new TIntIntHashMap();
    private AxisAlignedBB bounds = EMPTY_AABB;
    private BlockPos shift = BlockPos.ORIGIN;
    private Rotation rotation = Rotation.NONE;

    // --------------------------------------------------------------------- //

    /**
     * Adjust the current shift of this blueprint by the specified amount.
     * <p>
     * This will automatically wrap the shift if it exceeds the blueprint's
     * bounds.
     *
     * @param value the amount by which to shift the blueprint.
     */
    public void addShift(final Vec3i value) {
        shift = shift.add(value);

        final Vec3i size = AxisAlignedBBUtils.getBlockSize(bounds);
        while (shift.getX() < -size.getX()) {
            shift = shift.add(size.getX() + 1, 0, 0);
        }
        while (shift.getY() < -size.getY()) {
            shift = shift.add(0, size.getY() + 1, 0);
        }
        while (shift.getZ() < -size.getZ()) {
            shift = shift.add(0, 0, size.getZ() + 1);
        }
        while (shift.getX() > size.getX()) {
            shift = shift.add(-size.getX() - 1, 0, 0);
        }
        while (shift.getY() > size.getY()) {
            shift = shift.add(0, -size.getY() - 1, 0);
        }
        while (shift.getZ() > size.getZ()) {
            shift = shift.add(0, 0, -size.getZ() - 1);
        }
    }

    public void rotate(final Rotation amount) {
        rotation = rotation.add(amount);
    }

    /**
     * Get the bounds of the blueprint placement cell containing the specified
     * world position.
     * <p>
     * This is the bounds of the blueprint snapped to a grid defined by the
     * size of those very bounds.
     *
     * @param pos the position in the cell to get the bounds for.
     * @return the bounds of the cell.
     */
    public AxisAlignedBB getCellBounds(final BlockPos pos) {
        final Vec3i size = AxisAlignedBBUtils.getBlockSize(bounds);
        if (size.getX() == 0 || size.getY() == 0 || size.getZ() == 0) {
            return new AxisAlignedBB(pos); // Corrupted data.
        }
        final BlockPos origin = snapToGrid(pos, size);
        return bounds.offset(origin);
    }

    /**
     * Get a list of positions in this blueprint, as they would be placed in
     * the cell containing the specified world position.
     *
     * @param pos the position in the cell defining the origin position.
     * @return the list of positions in the cell.
     */
    public Stream<BlockPos> getBlocks(final BlockPos pos) {
        final Vec3i size = AxisAlignedBBUtils.getBlockSize(bounds);
        if (size.getX() == 0 || size.getY() == 0 || size.getZ() == 0) {
            return Stream.empty(); // Corrupted data.
        }
        final BlockPos origin = snapToGrid(pos, size).add(shift);
        return StreamSupport.stream(new BlockPosSpliterator(this, origin), false);
    }

    /**
     * Compute the total costs in items it would take to deserialize this block.
     * <p>
     * Returns a list of distinct item stacks with their count set accordingly.
     *
     * @return the total costs for deserializing this blueprint.
     */
    public List<ItemStack> getCosts() {
        final int[] counts = new int[blockData.size()];
        blocks.forEachValue(id -> {
            counts[id]++;
            return true;
        });

        final List<ItemStack> knownCosts = new ArrayList<>();
        for (int i = 0; i < blockData.size(); i++) {
            final NBTTagCompound data = blockData.get(i);
            final Iterable<ItemStack> costs = BlueprintAPI.getMaterialCosts(data);
            for (final ItemStack cost : costs) {
                cost.setCount(counts[i]);
                boolean found = false;
                for (final ItemStack knownCost : knownCosts) {
                    if (ItemStack.areItemsEqual(cost, knownCost) && ItemStack.areItemStackTagsEqual(cost, knownCost)) {
                        knownCost.grow(cost.getCount());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    knownCosts.add(cost);
                }
            }
        }

        return knownCosts;
    }

    /**
     * Create the jobs required to realize this blueprint in the world.
     * <p>
     * The positions are defined the same way as in {@link #getBlocks(BlockPos)}.
     *
     * @param player the player placing the blueprint.
     * @param pos    the position of the cell defining the origin position.
     */
    public void createJobs(final EntityPlayer player, final BlockPos pos) {
        final Vec3i size = AxisAlignedBBUtils.getBlockSize(bounds);
        if (size.getX() == 0 || size.getY() == 0 || size.getZ() == 0) {
            return; // Corrupted data.
        }
        final BlockPos origin = snapToGrid(pos, size).add(shift);
        JobManager.INSTANCE.addJobBatch(player, StreamSupport.stream(new JobAddSpliterator(this, origin), false));
    }

    // --------------------------------------------------------------------- //
    // INBTSerializable

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound nbt = new NBTTagCompound();

        final NBTTagList blockDataNbt = new NBTTagList();
        blockData.forEach(blockDataNbt::appendTag);
        nbt.setTag(TAG_BLOCK_DATA, blockDataNbt);

        final int[] blocksZipped = new int[blocks.size() * 2];
        int offset = 0;
        for (final TIntIntIterator it = blocks.iterator(); it.hasNext(); ) {
            it.advance();
            blocksZipped[offset++] = it.key();
            blocksZipped[offset++] = it.value();
        }
        nbt.setIntArray(TAG_BLOCKS, blocksZipped);

        nbt.setLong(TAG_SHIFT, shift.toLong());
        nbt.setByte(TAG_ROTATION, (byte) rotation.ordinal());

        return nbt;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        blockData.clear();
        blocks.clear();

        final NBTTagList blockDataNbt = nbt.getTagList(TAG_BLOCK_DATA, Constants.NBT.TAG_COMPOUND);
        for (int index = 0; index < blockDataNbt.tagCount(); index++) {
            blockData.add(blockDataNbt.getCompoundTagAt(index));
        }

        final int[] blocksZipped = nbt.getIntArray(TAG_BLOCKS);
        for (int offset = 0; offset < blocksZipped.length; offset += 2) {
            final int key = blocksZipped[offset];
            final int value = blocksZipped[offset + 1];
            blocks.put(key, value);
            bounds = bounds.union(new AxisAlignedBB(fromIndex(key)));
        }

        shift = BlockPos.fromLong(nbt.getLong(TAG_SHIFT));
        rotation = Rotation.values()[nbt.getByte(TAG_ROTATION)];
    }

    // --------------------------------------------------------------------- //

    private static BlockPos snapToGrid(final BlockPos pos, final Vec3i grid) {
        return new BlockPos(
                MathHelper.floor(pos.getX() / (float) grid.getX()) * grid.getX(),
                MathHelper.floor(pos.getY() / (float) grid.getY()) * grid.getY(),
                MathHelper.floor(pos.getZ() / (float) grid.getZ()) * grid.getZ()
        );
    }

    // --------------------------------------------------------------------- //

    public static final class Builder {
        private final BlueprintData data = new BlueprintData();
        private final TObjectIntMap<NBTTagCompound> nbtToId = new TObjectIntHashMap<>();

        public BlueprintData getData() {
            return data;
        }

        public void add(final BlockPos pos, final NBTTagCompound nbt) {
            final int id = getId(nbt);
            data.blocks.put(AbstractPatternData.toIndex(pos), id);
            data.bounds = data.bounds.union(new AxisAlignedBB(pos));
        }

        private int getId(final NBTTagCompound nbt) {
            if (nbtToId.containsKey(nbt)) {
                return nbtToId.get(nbt);
            } else {
                final int id = data.blockData.size();
                nbtToId.put(nbt, id);
                data.blockData.add(nbt);
                return id;
            }
        }
    }

    // --------------------------------------------------------------------- //

    public static final class JobData {

    }

    private static final class BlockPosSpliterator extends Spliterators.AbstractSpliterator<BlockPos> {
        private final BlockPos origin;
        private final TIntIntIterator iterator;

        BlockPosSpliterator(final BlueprintData data, final BlockPos origin) {
            super(data.blocks.size(), SIZED);
            this.origin = origin;
            this.iterator = data.blocks.iterator();
        }

        @Override
        public boolean tryAdvance(final Consumer<? super BlockPos> action) {
            if (iterator.hasNext()) {
                iterator.advance();
                final BlockPos relPos = fromIndex(iterator.key());
                // TODO Rotate by rotation around center of bounds.
                final BlockPos worldPos = relPos.add(origin);
                action.accept(worldPos);
                return true;
            }
            return false;
        }
    }

    private static final class JobAddSpliterator extends Spliterators.AbstractSpliterator<JobManager.JobSupplier> implements JobManager.JobSupplier {
        private final List<NBTTagCompound> blockData;
        private final BlockPos origin;
        private final TIntIntIterator iterator;

        JobAddSpliterator(final BlueprintData data, final BlockPos origin) {
            super(data.blocks.size(), SIZED);
            this.blockData = data.blockData;
            this.origin = origin;
            this.iterator = data.blocks.iterator();
        }

        @Override
        public boolean tryAdvance(final Consumer<? super JobManager.JobSupplier> action) {
            if (iterator.hasNext()) {
                iterator.advance();
                action.accept(this);
                return true;
            }
            return false;
        }

        @Override
        public void get(final JobManager.JobConsumer consumer) {
            final BlockPos relPos = fromIndex(iterator.key());
            // TODO Rotate by rotation around center of bounds.
            final BlockPos worldPos = relPos.add(origin);
            final NBTTagCompound nbt = blockData.get(iterator.value());
            consumer.accept(worldPos, Rotation.NONE, nbt);
        }
    }
}
