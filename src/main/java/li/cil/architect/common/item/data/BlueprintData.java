package li.cil.architect.common.item.data;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.Architect;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import li.cil.architect.common.jobs.JobManager;
import li.cil.architect.util.AxisAlignedBBUtils;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class BlueprintData extends AbstractPatternData implements INBTSerializable<NBTTagCompound> {
    // --------------------------------------------------------------------- //
    // Computed data.

    private static final AxisAlignedBB EMPTY_BOUNDS = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

    // NBT tag names.
    private static final String TAG_BLOCK_DATA = "data";
    private static final String TAG_BLOCK_POSITIONS = "positions";
    private static final String TAG_BLOCK_REFERENCES = "references";
    private static final String TAG_SHIFT = "shift";
    private static final String TAG_ROTATION = "rotation";

    // --------------------------------------------------------------------- //
    // Persisted data.

    private final List<NBTTagCompound> blockData = new ArrayList<>();
    private final BitSet blockPositions = new BitSet((1 << (NUM_BITS * 3)) - 1);
    @Nullable
    private int[] blockReferences;
    private AxisAlignedBB bounds = EMPTY_BOUNDS;
    private BlockPos shift = BlockPos.ORIGIN;
    private Rotation rotation = Rotation.NONE;

    // --------------------------------------------------------------------- //

    /**
     * Check whether this blueprint is empty. An empty blueprint is basically
     * an invalid blueprint (but can be obtained by cheating in the item, for
     * example).
     *
     * @return whether this is an empty blueprint.
     */
    public boolean isEmpty() {
        return blockData.size() == 0 || blockPositions.cardinality() == 0;
    }

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
            shift = shift.add(size.getX(), 0, 0);
        }
        while (shift.getY() < -size.getY()) {
            shift = shift.add(0, size.getY(), 0);
        }
        while (shift.getZ() < -size.getZ()) {
            shift = shift.add(0, 0, size.getZ());
        }
        while (shift.getX() > size.getX()) {
            shift = shift.add(-size.getX(), 0, 0);
        }
        while (shift.getY() > size.getY()) {
            shift = shift.add(0, -size.getY(), 0);
        }
        while (shift.getZ() > size.getZ()) {
            shift = shift.add(0, 0, -size.getZ());
        }
    }

    /**
     * Rotate this blueprint by the specified amount.
     *
     * @param amount the amount by which to rotate the blueprint.
     */
    public void rotate(final Rotation amount) {
        if (amount == Rotation.NONE) {
            return;
        }

        rotation = rotation.add(amount);

        assert bounds.minX == 0 && bounds.minY == 0 && bounds.minZ == 0;

        final BitSet rotPositions = new BitSet(blockPositions.size());
        final int[] rotMap = blockReferences != null ? new int[blockReferences.length] : null;
        for (int index = blockPositions.nextSetBit(0), count = 0; index >= 0; index = blockPositions.nextSetBit(index + 1), ++count) {
            BlockPos rotPos = fromIndex(index);
            AxisAlignedBB rotBounds = bounds;
            switch (amount) {
                case COUNTERCLOCKWISE_90:
                    rotPos = rotatePosClockwise(rotPos, rotBounds);
                    rotBounds = rotateBoundsClockwise(rotBounds);
                case CLOCKWISE_180:
                    rotPos = rotatePosClockwise(rotPos, rotBounds);
                    rotBounds = rotateBoundsClockwise(rotBounds);
                case CLOCKWISE_90:
                    rotPos = rotatePosClockwise(rotPos, rotBounds);
            }
            final int rotIndex = toIndex(rotPos);
            rotPositions.set(rotIndex);
            if (rotMap != null) {
                rotMap[count] = rotIndex;
            }
            assert index != Integer.MAX_VALUE;
        }

        blockPositions.clear();
        blockPositions.or(rotPositions);
        if (blockReferences != null) {
            final int[] rotReferences = new int[blockReferences.length];
            for (int index = rotPositions.nextSetBit(0), rotRefIndex = 0; index >= 0; index = rotPositions.nextSetBit(index + 1), ++rotRefIndex) {
                final int refIndex = ArrayUtils.indexOf(rotMap, index);
                rotReferences[rotRefIndex] = blockReferences[refIndex];
            }
            blockReferences = rotReferences;
        }

        switch (amount) {
            case COUNTERCLOCKWISE_90:
                bounds = rotateBoundsClockwise(bounds);
            case CLOCKWISE_180:
                bounds = rotateBoundsClockwise(bounds);
            case CLOCKWISE_90:
                bounds = rotateBoundsClockwise(bounds);
        }
    }

    /**
     * Get the current rotation of this blueprint.
     *
     * @return the current rotation.
     */
    public Rotation getRotation() {
        return rotation;
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
    public AxisAlignedBB getCellBounds(World world, final BlockPos pos) {
        final Vec3i size = AxisAlignedBBUtils.getBlockSize(bounds);
        if (size.getX() == 0 || size.getY() == 0 || size.getZ() == 0) {
            return new AxisAlignedBB(pos); // Corrupted data.
        }
        final BlockPos origin = snapToGrid(world, pos, size);
        return bounds.offset(origin);
    }

    /**
     * Get a list of positions in this blueprint, as they would be placed in
     * the cell containing the specified world position.
     *
     * @param pos the position in the cell defining the origin position.
     * @return the list of positions in the cell.
     */
    public Stream<BlockPos> getBlocks(World world, final BlockPos pos) {
        final Vec3i size = AxisAlignedBBUtils.getBlockSize(bounds);
        if (size.getX() == 0 || size.getY() == 0 || size.getZ() == 0) {
            return Stream.empty(); // Corrupted data.
        }
        final BlockPos origin = snapToGrid(world, pos, size);
        return StreamSupport.stream(new BlockPosSpliterator(this, origin), false);
    }

    /**
     * Compute the total costs in items it would take to deserialize this block.
     * <p>
     * Returns a list of distinct item stacks with their count set accordingly.
     *
     * @return the total costs for deserializing this blueprint.
     */
    @SideOnly(Side.CLIENT)
    public List<String> getCosts() {
        final int[] counts = new int[blockData.size()];
        if (blockData.size() > 1) {
            assert blockReferences != null;
            for (final int id : blockReferences) {
                counts[id]++;
            }
        } else {
            counts[0] = blockPositions.cardinality();
        }

        final List<ItemStack> totalItemCosts = new ArrayList<>();
        final List<FluidStack> totalFluidCosts = new ArrayList<>();
        for (int i = 0; i < blockData.size(); i++) {
            final NBTTagCompound data = blockData.get(i);
            final Iterable<ItemStack> itemCosts = ConverterAPI.getItemCosts(data);
            final Iterable<FluidStack> fluidCosts = ConverterAPI.getFluidCosts(data);

            for (final ItemStack cost : itemCosts) {
                cost.stackSize = cost.stackSize * counts[i];
                boolean found = false;
                for (final ItemStack knownCost : totalItemCosts) {
                    if (knownCost.isItemEqual(cost) && ItemStack.areItemStackTagsEqual(cost, knownCost)) {
                        knownCost.stackSize += cost.stackSize;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    totalItemCosts.add(cost);
                }
            }

            for (final FluidStack cost : fluidCosts) {
                cost.amount = cost.amount * counts[i];
                boolean found = false;
                for (final FluidStack knownCost : totalFluidCosts) {
                    if (knownCost.isFluidEqual(cost) && FluidStack.areFluidStackTagsEqual(cost, knownCost)) {
                        knownCost.amount += cost.amount;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    totalFluidCosts.add(cost);
                }
            }
        }

        final List<Object> totalCosts = new ArrayList<>();
        totalCosts.addAll(totalItemCosts);
        totalCosts.addAll(totalFluidCosts);
        totalCosts.sort(Comparator.comparing(o -> {
            if (o instanceof ItemStack) {
                return ((ItemStack) o).getDisplayName();
            } else {
                return ((FluidStack) o).getLocalizedName();
            }
        }));

        return totalCosts.stream().map(cost -> {
            final int count;
            final String name;
            if (cost instanceof ItemStack) {
                final ItemStack stack = (ItemStack) cost;
                count = stack.stackSize;
                name = stack.getDisplayName();
            } else {
                final FluidStack stack = (FluidStack) cost;
                count = MathHelper.ceil(stack.amount / 1000f);
                name = stack.getLocalizedName();
            }
            return I18n.format(Constants.TOOLTIP_BLUEPRINT_COSTS_LINE, count, name);
        }).collect(Collectors.toList());
    }

    /**
     * Create the jobs required to realize this blueprint in the world.
     * <p>
     * The positions are defined the same way as in {@link #getBlocks(World, BlockPos)}.
     *
     * @param player       the player placing the blueprint.
     * @param allowPartial whether to allow partial placement.
     * @param pos          the position of the cell defining the origin position.
     */
    public void createJobs(final EntityPlayer player, final boolean allowPartial, final BlockPos pos) {
        final Vec3i size = AxisAlignedBBUtils.getBlockSize(bounds);
        if (size.getX() == 0 || size.getY() == 0 || size.getZ() == 0) {
            return; // Corrupted data.
        }
        final BlockPos origin = snapToGrid(player.world, pos, size);
        JobManager.INSTANCE.addJobBatch(player, allowPartial, StreamSupport.stream(new JobAddSpliterator(this, origin), false));
    }

    // --------------------------------------------------------------------- //
    // INBTSerializable

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound nbt = new NBTTagCompound();

        final NBTTagList blockDataNbt = new NBTTagList();
        blockData.forEach(blockDataNbt::appendTag);
        nbt.setTag(TAG_BLOCK_DATA, blockDataNbt);

        nbt.setByteArray(TAG_BLOCK_POSITIONS, blockPositions.toByteArray());
        if (blockReferences != null && blockData.size() > 1) {
            nbt.setIntArray(TAG_BLOCK_REFERENCES, blockReferences);
        }

        nbt.setLong(TAG_SHIFT, shift.toLong());
        nbt.setByte(TAG_ROTATION, (byte) rotation.ordinal());

        return nbt;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        blockData.clear();
        blockPositions.clear();
        blockReferences = null;
        bounds = EMPTY_BOUNDS;
        shift = BlockPos.ORIGIN;
        rotation = Rotation.NONE;

        if (!nbt.hasKey(TAG_BLOCK_DATA, NBT.TAG_LIST) ||
                !nbt.hasKey(TAG_BLOCK_POSITIONS, NBT.TAG_BYTE_ARRAY)) {
            return;
        }

        final NBTTagList blockDataNbt = nbt.getTagList(TAG_BLOCK_DATA, NBT.TAG_COMPOUND);
        for (int index = 0; index < blockDataNbt.tagCount(); index++) {
            blockData.add(blockDataNbt.getCompoundTagAt(index));
        }

        final BitSet loaded = BitSet.valueOf(nbt.getByteArray(TAG_BLOCK_POSITIONS));
        if (loaded.length() > blockPositions.size()) {
            loaded.clear(blockPositions.size(), loaded.size() - 1);
        }
        blockPositions.or(loaded);
        if (nbt.hasKey(TAG_BLOCK_REFERENCES, NBT.TAG_INT_ARRAY) && blockData.size() > 1) {
            blockReferences = nbt.getIntArray(TAG_BLOCK_REFERENCES);
        }

        if (blockReferences == null ? blockData.size() != 1 : blockReferences.length != blockPositions.cardinality()) {
            Architect.getLog().warn("Corrupt blueprint data.");
            blockData.clear();
            blockPositions.clear();
            blockReferences = null;
            return;
        }

        bounds = computeBounds(blockPositions);
        shift = BlockPos.fromLong(nbt.getLong(TAG_SHIFT));
        rotation = Rotation.values()[nbt.getByte(TAG_ROTATION)];
    }

    // --------------------------------------------------------------------- //

    private BlockPos snapToGrid(World world, final BlockPos pos, final Vec3i grid) {
        if (!Settings.enablePlacementGrid) {
            final EnumFacing sideHit = PlayerUtils.getSideHit(world);
            final BlockPos center = new BlockPos(bounds.getCenter());
            BlockPos offset = center;
            if (sideHit != null) {
                offset = offset.add(
                        -center.getX() * sideHit.getFrontOffsetX(),
                        -center.getY() * sideHit.getFrontOffsetY(),
                        -center.getZ() * sideHit.getFrontOffsetZ());
                if (sideHit.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE && isSideEven(sideHit)) {
                    offset = offset.add(
                            sideHit.getFrontOffsetX(),
                            sideHit.getFrontOffsetY(),
                            sideHit.getFrontOffsetZ());
                }
            }
            return pos.subtract(offset);
        }
        final BlockPos adjusted = pos.subtract(shift);
        return new BlockPos(
                MathHelper.floor(adjusted.getX() / (float) grid.getX()) * grid.getX(),
                MathHelper.floor(adjusted.getY() / (float) grid.getY()) * grid.getY(),
                MathHelper.floor(adjusted.getZ() / (float) grid.getZ()) * grid.getZ()
        ).add(shift);
    }

    private boolean isSideEven(EnumFacing side) {
        switch (side.getAxis()) {
            case X:
                return bounds.maxX % 2 == 0;
            case Y:
                return bounds.maxY % 2 == 0;
            case Z:
                return bounds.maxZ % 2 == 0;
            default:
                return false;
        }
    }

    private static BlockPos rotatePosClockwise(final BlockPos pos, final AxisAlignedBB bounds) {
        final int sizeZ = (int) (bounds.maxZ - bounds.minZ);
        return new BlockPos(sizeZ - 1 - pos.getZ(), pos.getY(), pos.getX());
    }

    private static AxisAlignedBB rotateBoundsClockwise(final AxisAlignedBB bounds) {
        return new AxisAlignedBB(bounds.minX, bounds.minY, bounds.minZ, bounds.maxZ, bounds.maxY, bounds.maxX);
    }

    // --------------------------------------------------------------------- //

    public static final class Builder {
        private final BlueprintData data = new BlueprintData();
        private final TObjectIntMap<NBTTagCompound> nbtToId = new TObjectIntHashMap<>();
        private final TIntIntMap blocks = new TIntIntHashMap();

        public BlueprintData getData(final BlockPos origin) {
            final int[] keys = blocks.keys();
            Arrays.sort(keys);
            data.blockPositions.clear();
            data.blockReferences = new int[keys.length];
            for (int i = 0; i < keys.length; i++) {
                data.blockPositions.set(keys[i]);
                data.blockReferences[i] = blocks.get(keys[i]);
            }
            final Vec3i size = AxisAlignedBBUtils.getBlockSize(data.bounds);
            data.shift = new BlockPos(origin.getX() % size.getX(), origin.getY() % size.getY(), origin.getZ() % size.getZ());
            return data;
        }

        public void add(final BlockPos pos, final NBTTagCompound nbt) {
            final int id = getId(nbt);
            blocks.put(AbstractPatternData.toIndex(pos), id);
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

    private static final class BlockPosSpliterator extends Spliterators.AbstractSpliterator<BlockPos> {
        private final BitSet positions;
        private final BlockPos origin;
        private int index = -1;

        BlockPosSpliterator(final BlueprintData data, final BlockPos origin) {
            super(data.blockPositions.cardinality(), SIZED);
            this.positions = data.blockPositions;
            this.origin = origin;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super BlockPos> action) {
            if (origin != null && (index = positions.nextSetBit(index + 1)) >= 0) {
                final BlockPos relPos = fromIndex(index);
                final BlockPos worldPos = relPos.add(origin);
                action.accept(worldPos);
                return true;
            }
            return false;
        }
    }

    private static final class JobAddSpliterator extends Spliterators.AbstractSpliterator<JobManager.JobSupplier> implements JobManager.JobSupplier {
        private final List<NBTTagCompound> blockData;
        private final BitSet positions;
        @Nullable
        private final int[] references;
        private final BlockPos origin;
        private final Rotation rotation;
        private int count = 0;
        private int index = -1;

        JobAddSpliterator(final BlueprintData data, final BlockPos origin) {
            super(data.blockPositions.cardinality(), SIZED);
            this.blockData = data.blockData;
            this.positions = data.blockPositions;
            this.references = data.blockReferences;
            this.origin = origin;
            this.rotation = data.rotation;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super JobManager.JobSupplier> action) {
            if (origin != null && (index = positions.nextSetBit(index + 1)) >= 0) {
                action.accept(this);
                ++count;
                return true;
            }
            return false;
        }

        @Override
        public void get(final JobManager.JobConsumer consumer) {
            final BlockPos relPos = fromIndex(index);
            final BlockPos worldPos = relPos.add(origin);
            final NBTTagCompound nbt = blockData.get(references != null ? references[count] : 0);
            consumer.accept(worldPos, rotation, nbt);
        }
    }
}
