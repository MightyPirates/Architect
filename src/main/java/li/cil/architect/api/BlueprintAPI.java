package li.cil.architect.api;

import li.cil.architect.api.blueprint.Converter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * API entry point for registering and looking up {@link Converter}s.
 * <p>
 * When trying to serialize a block in the world, all registered converters will
 * be queried, until one returns something not <tt>null</tt> from their
 * {@link Converter#serialize(World, BlockPos)} method.
 * <p>
 * This is made available in the init phase, so you'll either have to (soft)
 * depend on the mod, or you must not make calls to this before the init phase.
 */
public final class BlueprintAPI {
    /**
     * Register the specified converter.
     *
     * @param converter the convert to register.
     */
    public static void addConverter(final Converter converter) {
        if (API.blueprintAPI != null) {
            API.blueprintAPI.addConverter(converter);
        }
    }

    /**
     * Test whether the block at the specified world position can be serialized.
     *
     * @param world the world containing the block to serialize.
     * @param pos   the position of the block to serialize.
     * @return <code>true</code> if the block can be serialized;
     * <code>false</code> otherwise.
     */
    public static boolean canSerialize(final World world, final BlockPos pos) {
        if (API.blueprintAPI != null) {
            return API.blueprintAPI.canSerialize(world, pos);
        }
        return false;
    }

    /**
     * Serialize the block at the specified world position.
     *
     * @param world the world containing the block to serialize.
     * @param pos   the position of the block to serialize.
     * @return the serialized block data; <tt>null</tt> if no converter exists.
     */
    @Nullable
    public static NBTTagCompound serialize(final World world, final BlockPos pos) {
        if (API.blueprintAPI != null) {
            return API.blueprintAPI.serialize(world, pos);
        }
        return null;
    }

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
    public static boolean canDeserialize(final World world, final BlockPos pos, final NBTTagCompound nbt) {
        if (API.blueprintAPI != null) {
            return API.blueprintAPI.canDeserialize(world, pos, nbt);
        }
        return false;
    }

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
    public static boolean deserialize(final IItemHandler materials, final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound nbt) {
        if (API.blueprintAPI != null) {
            return API.blueprintAPI.deserialize(materials, world, pos, rotation, nbt);
        }
        return false;
    }

    // --------------------------------------------------------------------- //

    private BlueprintAPI() {
    }
}
