package li.cil.architect.api.detail;

import li.cil.architect.api.blueprint.Converter;
import li.cil.architect.api.blueprint.SortIndex;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * Allows registering and looking up {@link Converter}s.
 * <p>
 * When trying to serialize a block in the world, all registered converters will
 * be queried, until one returns <code>true</code> from {@link #canSerialize}.
 */
public interface BlueprintAPI {
    /**
     * Register the specified converter.
     *
     * @param converter the converter to register.
     */
    void addConverter(Converter converter);

    /**
     * A sort index used to control in which order blocks are deserialized.
     * <p>
     * Blocks being deserialized using converters that provide lower numbers
     * here will be processed first.
     * <p>
     * This allows deserialization of solid blocks (e.g. cobble) before non-
     * solid blocks (e.g. levers, water).
     *
     * @param data the serialized representation of the block in question.
     * @return the sort index of the specified data.
     * @see SortIndex
     */
    int getSortIndex(final NBTTagCompound data);

    /**
     * Checks if the block at the specified world position can be serialized.
     *
     * @param world the world containing the block to serialize.
     * @param pos   the position of the block to serialize.
     * @return <code>true</code> if the block can be serialized;
     * <code>false</code> otherwise.
     */
    boolean canSerialize(final World world, final BlockPos pos);

    /**
     * Creates a serialized representation of the block at the specified world
     * position.
     *
     * @param world the world containing the block to serialize.
     * @param pos   the position of the block to serialize.
     * @return a serialized representation of the block.
     */
    @Nullable
    NBTTagCompound serialize(final World world, final BlockPos pos);

    /**
     * Get a list of materials required to deserialize the block described by
     * the specified data.
     * <p>
     * The data passed along is guaranteed to be a value that was previously
     * produced by this converter's {@link #serialize} method.
     * <p>
     * This will typically return a singleton list or an empty list, but for more
     * complex converters, e.g. ones handling multi-part blocks this returns an
     * iterable.
     * <p>
     * This is not used for logic, purely for user feedback, e.g. in tooltips.
     *
     * @return a list of materials missing.
     */
    Iterable<ItemStack> getMaterialCosts(final NBTTagCompound data);

    /**
     * Called when a job for deserialization should be created.
     * <p>
     * The data passed along is guaranteed to be a value that was previously
     * produced by this converter's {@link #serialize} method.
     * <p>
     * This serves as a filter for which blocks in a blueprint actually can be
     * deserialized, in particular with respect to available materials which
     * are consumed from the specified {@link IItemHandler}.
     *
     * @param materials access to building materials available for deserialization.
     * @param world     the world into which to deserialize the block.
     * @param pos       the position at which to deserialize the block.
     * @param rotation  the rotation to deserialize with.
     * @param data      the serialized representation of the block to deserialize.
     * @return <code>true</code> if the data can be deserialized;
     * <code>false</code> otherwise.
     */
    boolean preDeserialize(final IItemHandler materials, final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound data);

    /**
     * Deserialize the specified serialized block data into the world at the
     * specified world position.
     * <p>
     * The passed {@link NBTBase} passed along is guaranteed to be a value that
     * was previously produced by this converter's {@link #serialize} method.
     *
     * @param world    the world to deserialize the block into.
     * @param pos      the position to deserialize the block at.
     * @param rotation the rotation to deserialize with.
     * @param data     the serialized representation of the block to deserialize.
     */
    void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound data);
}
