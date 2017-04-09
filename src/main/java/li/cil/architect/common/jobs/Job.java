package li.cil.architect.common.jobs;

import li.cil.architect.util.ChunkUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

/**
 * Represents a single, pending job.
 * <p>
 * A job is a pending deserialization / block placement operation. Jobs are
 * already "paid" for, as the materials are consumed from the placing
 * player's inventory the moment jobs are created.
 */
final class Job {
    // --------------------------------------------------------------------- //
    // Computed data.

    // NBT tag names.
    private static final String TAG_REFERENCE = "ref";
    private static final String TAG_POSITION = "pos";
    private static final String TAG_ROTATION = "rot";

    // --------------------------------------------------------------------- //
    // Persisted data.

    long dataReference;
    private short compressedPos; // 0xZYYX
    private byte rotation;

    // --------------------------------------------------------------------- //

    Job() {
    }

    Job(final long reference, final BlockPos pos, final Rotation rotation) {
        dataReference = reference;
        setPos(pos);
        setRotation(rotation);
    }

    BlockPos getPos(final ChunkPos chunkPos) {
        return ChunkUtils.shortToPos(chunkPos, compressedPos);
    }

    void setPos(final BlockPos pos) {
        compressedPos = ChunkUtils.posToShort(pos);
    }

    Rotation getRotation() {
        return Rotation.values()[rotation];
    }

    void setRotation(final Rotation value) {
        rotation = (byte) value.ordinal();
    }

    // --------------------------------------------------------------------- //

    NBTTagCompound serializeNBT() {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong(TAG_REFERENCE, dataReference);
        nbt.setShort(TAG_POSITION, compressedPos);
        nbt.setByte(TAG_ROTATION, rotation);
        return nbt;
    }

    void deserializeNBT(final NBTTagCompound nbt) {
        dataReference = nbt.getLong(TAG_REFERENCE);
        compressedPos = nbt.getShort(TAG_POSITION);
        rotation = nbt.getByte(TAG_ROTATION);
    }
}
