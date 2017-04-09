package li.cil.architect.common.jobs;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import li.cil.architect.api.API;
import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.Architect;
import li.cil.architect.common.config.Settings;
import li.cil.architect.util.ChunkUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class JobManagerImpl extends WorldSavedData {
    // --------------------------------------------------------------------- //
    // Computed data.

    static final String ID = API.MOD_ID + "_blocks";

    private static final String TAG_NEXT_ID = "nextId";
    private static final String TAG_BLOCK_DATA = "blockData";
    private static final String TAG_JOBS = API.MOD_ID + ":jobs";

    // Fields because closures because trove...
    private int totalOps, chunkOps;

    // --------------------------------------------------------------------- //
    // Persisted data.

    // Next ID given to a not currently known serialized block. Yes in
    // theory this means that if enough jobs are created and completed we
    // can overflow and come in back from the bottom, but it's a bloody
    // long. It'd take millions of years for this to realistically happen.
    private long nextId = 1;

    // Lookup when consuming and lookup when inserting.
    private final TLongObjectMap<JobBlockData> idToData = new TLongObjectHashMap<>();
    private final Map<NBTTagCompound, JobBlockData> nbtToData = new HashMap<>();

    // Lookup table for jobs by chunk. Only contains loaded chunks.
    private final TLongObjectMap<JobChunkStorage> chunkData = new TLongObjectHashMap<>();

    JobManagerImpl() {
        super(ID);
    }

    // For creation via reflection in MapStorage#getOrLoadData.
    @SuppressWarnings("unused")
    public JobManagerImpl(final String id) {
        super(id);
    }

    void addJob(final BlockPos pos, final Rotation rotation, final NBTTagCompound nbt) {
        Architect.getLog().debug("Add Job at {}: {}", pos, nbt);

        final Job job = new Job(addReference(nbt), pos, rotation);

        final JobChunkStorage storage = getChunkStorage(new ChunkPos(pos));
        storage.pushJob(job);
    }

    void updateJobs(final World world) {
        totalOps = 0;
        chunkData.forEachEntry((key, storage) -> {
            chunkOps = 0;

            final ChunkPos chunkPos = ChunkUtils.longToChunkPos(key);
            while (!storage.isEmpty()) {
                final Job job = storage.popJob();
                final BlockPos pos = job.getPos(chunkPos);
                Architect.getLog().debug("Process Job at {}: @{}", pos, job.dataReference);
                final NBTTagCompound data = removeReference(job.dataReference);
                if (data != null) {
                    ConverterAPI.deserialize(world, pos, job.getRotation(), data);
                    Architect.getLog().debug("Finished Job at {}: {}", pos, data);
                }

                // Check limits. Total first, to make sure it's always
                // incremented, even in our last chunk iteration.
                if (++totalOps >= Settings.maxWorldOperationsPerTick) {
                    break;
                }
                if (++chunkOps >= Settings.maxChunkOperationsPerTick) {
                    break;
                }
            }

            // Stop once we hit the cap of total operations.
            return totalOps < Settings.maxWorldOperationsPerTick;
        });
    }

    void loadChunk(final ChunkPos chunk, final NBTTagCompound data) {
        final JobChunkStorage storage = getChunkStorage(chunk);
        storage.deserializeNBT(data.getTagList(TAG_JOBS, Constants.NBT.TAG_COMPOUND));
    }

    void saveChunk(final ChunkPos chunk, final NBTTagCompound data) {
        final JobChunkStorage storage = getChunkStorage(chunk);
        data.setTag(TAG_JOBS, storage.serializeNBT());
    }

    void unloadChunk(final ChunkPos chunkPos) {
        final long id = ChunkUtils.chunkPosToLong(chunkPos);
        chunkData.remove(id);
    }

    // --------------------------------------------------------------------- //

    private JobChunkStorage getChunkStorage(final ChunkPos chunkPos) {
        final long chunkId = ChunkUtils.chunkPosToLong(chunkPos);
        JobChunkStorage storage = chunkData.get(chunkId);
        if (storage == null) {
            storage = new JobChunkStorage();
            chunkData.put(chunkId, storage);
        }
        return storage;
    }

    private long addReference(final NBTTagCompound nbt) {
        JobBlockData entry = nbtToData.get(nbt);
        if (entry == null) {
            entry = new JobBlockData(nextId++, nbt);
            idToData.put(entry.id, entry);
            nbtToData.put(nbt, entry);
            Architect.getLog().debug("New Reference @{}: {}", entry.id, entry.data);
        }
        entry.referenceCount++;
        Architect.getLog().debug("Add Reference @{}: #{}", entry.id, entry.referenceCount);
        markDirty();
        return entry.id;
    }

    @Nullable
    private NBTTagCompound removeReference(final long id) {
        final JobBlockData entry = idToData.get(id);
        if (entry == null) {
            Architect.getLog().error("Failed retrieving block data for id, meaning the data was disposed even though there were still references to it. This should be impossible in normal operation. If you're 110% sure your server didn't corrupt some of its save data, e.g. due to a crash, please report this!");
            return null;
        }
        entry.referenceCount--;
        Architect.getLog().debug("Remove Reference @{}: #{}", entry.id, entry.referenceCount);
        markDirty();
        if (entry.referenceCount <= 0) {
            idToData.remove(entry.id);
            nbtToData.remove(entry.data);
            Architect.getLog().debug("Destroy Reference @{}", entry.id);
        }
        return entry.data;
    }

    // --------------------------------------------------------------------- //
    // WorldSavedData

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        nextId = nbt.getLong(TAG_NEXT_ID);

        final NBTTagList dataNbt = nbt.getTagList(TAG_BLOCK_DATA, Constants.NBT.TAG_COMPOUND);
        idToData.clear();
        nbtToData.clear();
        for (int tagIndex = 0; tagIndex < dataNbt.tagCount(); tagIndex++) {
            final JobBlockData entry = new JobBlockData();
            entry.deserializeNBT(dataNbt.getCompoundTagAt(tagIndex));
            idToData.put(entry.id, entry);
            nbtToData.put(entry.data, entry);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        nbt.setLong(TAG_NEXT_ID, nextId);

        final NBTTagList dataNbt = new NBTTagList();
        for (final JobBlockData entry : idToData.valueCollection()) {
            dataNbt.appendTag(entry.serializeNBT());
        }
        nbt.setTag(TAG_BLOCK_DATA, dataNbt);

        return nbt;
    }
}
