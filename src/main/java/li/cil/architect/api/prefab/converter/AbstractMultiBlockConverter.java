package li.cil.architect.api.prefab.converter;

import li.cil.architect.api.converter.MaterialSource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.UUID;

/**
 * Base class for converters of blocks spanning two block spaces, such as beds,
 * doors and double plants.
 * <p>
 * These typically will only want to actually store <em>one</em> of the two
 * positions (the "primary" on, bottom part of double plants e.g.), and also
 * only require the block item as a material once for both parts.
 */
public abstract class AbstractMultiBlockConverter extends AbstractConverter {
    public AbstractMultiBlockConverter(final UUID uuid, final int sortIndex) {
        super(uuid, sortIndex);
    }

    public AbstractMultiBlockConverter(final UUID uuid) {
        super(uuid);
    }

    // --------------------------------------------------------------------- //

    /**
     * Check whether the specified block state is the secondary part.
     *
     * @param state the state to check.
     * @return <code>true</code> if the state represents the secondary part;
     * <code>false</code> otherwise.
     */
    protected abstract boolean isSecondaryState(final IBlockState state);

    /**
     * Get the block state representing the secondary part from the primary one.
     *
     * @param state the primary block state.
     * @return the secondary block state.
     */
    protected abstract IBlockState getSecondaryState(final IBlockState state);

    /**
     * Get the world position of the secondary part.
     *
     * @param pos   the position of the primary part.
     * @param state the state of the primary part.
     * @return the position of the secondary part.
     */
    protected abstract BlockPos getSecondaryPos(final BlockPos pos, final IBlockState state);

    // --------------------------------------------------------------------- //
    // Converter

    @Override
    public Iterable<ItemStack> getItemCosts(final NBTBase data) {
        if (isSecondaryPart(data)) {
            return Collections.emptyList();
        }
        return super.getItemCosts(data);
    }

    @Override
    public boolean preDeserialize(final MaterialSource materialSource, final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        if (isSecondaryPart(data)) {
            return false;
        }

        if (!isSecondaryPosValid(world, pos, rotation, data)) {
            return false;
        }

        return super.preDeserialize(materialSource, world, pos, rotation, data);
    }

    @Override
    public void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        if (!isSecondaryPosValid(world, pos, rotation, data)) {
            cancelDeserialization(world, pos, rotation, data);
            return;
        }

        super.deserialize(world, pos, rotation, data);
    }

    @Override
    protected void postDeserialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
        super.postDeserialize(world, pos, state, data);

        final IBlockState secondaryState = getSecondaryState(state);

        world.setBlockState(getSecondaryPos(pos, state), secondaryState);
    }

    // --------------------------------------------------------------------- //

    private boolean isSecondaryPart(final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return true;
        }

        return isSecondaryState(state);
    }

    private boolean isSecondaryPosValid(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return false;
        }
        final BlockPos secondaryPos = getSecondaryPos(pos, state.withRotation(rotation));
        return world.isBlockLoaded(secondaryPos) && world.getBlockState(secondaryPos).getBlock().isReplaceable(world, secondaryPos);
    }
}
