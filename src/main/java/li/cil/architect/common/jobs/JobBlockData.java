package li.cil.architect.common.jobs;

import net.minecraft.nbt.NBTTagCompound;

/**
 * A reference counting wrapper for an {@link NBTTagCompound}.
 * <p>
 * We keep references to equal tags to avoid data duplication in memory,
 * with the intent of keeping memory usage low(er than it'd otherwise be).
 */
final class JobBlockData {
    // --------------------------------------------------------------------- //
    // Computed data.

    // NBT tag names.
    private static final String TAG_ID = "id";
    private static final String TAG_DATA = "data";
    private static final String TAG_REFERENCES = "refs";

    // --------------------------------------------------------------------- //
    // Persisted data.

    long id; // The reference ID of this entry, used in jobs.
    NBTTagCompound data; // The actual data stored in this entry.
    int referenceCount; // The remaining live references to this entry.

    // --------------------------------------------------------------------- //

    JobBlockData() {
    }

    JobBlockData(final long id, final NBTTagCompound data) {
        this.id = id;
        this.data = data.copy();
    }

    // --------------------------------------------------------------------- //

    NBTTagCompound serializeNBT() {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong(TAG_ID, id);
        nbt.setTag(TAG_DATA, data);
        nbt.setInteger(TAG_REFERENCES, referenceCount);
        return nbt;
    }

    void deserializeNBT(final NBTTagCompound nbt) {
        id = nbt.getLong(TAG_ID);
        data = nbt.getCompoundTag(TAG_DATA);
        referenceCount = nbt.getInteger(TAG_REFERENCES);
    }
}
