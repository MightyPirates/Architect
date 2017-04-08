package li.cil.architect.api.blueprint;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.UUID;

/**
 * Provides conversions of blocks in the world to a serialized format and back.
 * <p>
 * A converter must always provide a way to serialize <em>and</em> deserialize
 * a block to avoid creation of blueprints that can no longer be deserialized
 * and other potentially unexpected behavior. A converter may however support
 * any number of blocks, as long as this rule is not violated.
 */
public interface Converter {
    /**
     * A constant UUID representing this converter.
     * <p>
     * This is used to look up a converter for deserialization, so the returned
     * value must be the same across any number of game starts.
     *
     * @return the UUID of this converter.
     */
    UUID getUUID();

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
     * @return the sort index of this converter.
     * @see SortIndex
     */
    int getSortIndex(final NBTBase data);

    /**
     * Get a list of materials missing from the specified {@link IItemHandler}
     * that are required to deserialize the block described by the specified
     * {@link NBTBase}.
     * <p>
     * This will typically return a singleton list or an empty list, but for more
     * complex converters, e.g. ones handling multi-part blocks this returns an
     * iterable.
     *
     * @param materials access to materials available for deserialization.
     * @return a list of materials missing.
     */
    Iterable<ItemStack> getMissingMaterials(final IItemHandler materials, final NBTBase data);

    /**
     * Checks if this converter can be used to serialize the block at the
     * specified world position.
     *
     * @param world the world containing the block to serialize.
     * @param pos   the position of the block to serialize.
     * @return <code>true</code> if the converter can serialize the block;
     * <code>false</code> otherwise;
     */
    boolean canSerialize(final World world, final BlockPos pos);

    /**
     * Creates a serialized representation of the block at the specified world
     * position.
     * <p>
     * This is guaranteed to only called if {@link #canSerialize} returned
     * <code>true</code> for the passed parameters.
     *
     * @param world the world containing the block to serialize.
     * @param pos   the position of the block to serialize.
     * @return a serialized representation of the block.
     */
    NBTBase serialize(final World world, final BlockPos pos);

    /**
     * Deserialize the specified serialized block data into the world at the
     * specified world position.
     * <p>
     * The passed {@link NBTBase} passed along is guaranteed to be a value that
     * was previously produced by this converter's {@link #serialize} method.
     * <p>
     * The converter will take care of consuming the items from the specified
     * {@link IItemHandler}.
     * <p>
     * This is called independently of {@link #getMissingMaterials}, that is to
     * say, this may be called even though the list of missing materials is
     * non-empty.
     *
     * @param materials access to building materials available for deserialization.
     * @param world     the world to deserialize the block into.
     * @param pos       the position to deserialize the block at.
     * @param rotation  the rotation to deserialize with.
     * @param data      the serialized representation of the block to deserialize.
     * @return <code>true</code> if the deserialization was successful;
     * <code>false</code> otherwise.
     */
    boolean deserialize(final IItemHandler materials, final World world, final BlockPos pos, final Rotation rotation, final NBTBase data);
}
