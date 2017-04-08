package li.cil.architect.api.detail;

import li.cil.architect.api.blueprint.Converter;
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
 * be queried, until one returns something not <tt>null</tt> from their
 * {@link Converter#serialize(World, BlockPos)} method.
 */
public interface BlueprintAPI {
    /**
     * Register the specified provider.
     *
     * @param converter the provider to register.
     */
    void addConverter(Converter converter);

    /**
     * Test whether the block at the specified world position can be serialized.
     *
     * @param world the world containing the block to serialize.
     * @param pos   the position of the block to serialize.
     * @return <code>true</code> if the block can be serialized;
     * <code>false</code> otherwise.
     */
    boolean canSerialize(final World world, final BlockPos pos);

    /**
     * Serialize the block at the specified world position.
     *
     * @param world the world containing the block to serialize.
     * @param pos   the position of the block to serialize.
     * @return the serialized block data; <tt>null</tt> if no converter exists.
     */
    @Nullable
    NBTTagCompound serialize(final World world, final BlockPos pos);

    /**
     * Test whether the specified data can be deserialized at the specified
     * world position.
     *
     * @param world the world into which to deserialize the block.
     * @param pos   the position at which to deserialize the block.
     * @param nbt   the data to deserialize.
     * @return <code>true</code> if the data can be deserialized;
     * <code>false</code> otherwise.
     */
    boolean canDeserialize(final World world, final BlockPos pos, final NBTTagCompound nbt);

    /**
     * Deserialize the specified data at the specified world position.
     *
     * @param materials access to building materials available for deserialization.
     * @param world     the world into which to deserialize the block.
     * @param pos       the position at which to deserialize the block.
     * @param rotation  the rotation to deserialize the block with.
     * @param nbt       the data to deserialize.
     * @return <code>true</code> on success; <code>false</code> otherwise.
     */
    boolean deserialize(final IItemHandler materials, final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound nbt);
}
