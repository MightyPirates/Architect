package li.cil.architect.common.blueprint;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import li.cil.architect.api.API;
import li.cil.architect.api.BlueprintAPI;
import li.cil.architect.common.Architect;
import li.cil.architect.common.Settings;
import li.cil.architect.util.ChunkUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Tracks pending block deserialization / placement.
 */
public enum JobManager {
    INSTANCE;

    // --------------------------------------------------------------------- //

    /**
     * Utility interface for {@link #addJobBatch(EntityPlayer, Stream)}.
     */
    public interface JobConsumer {
        void accept(final BlockPos pos, final Rotation rotation, final NBTTagCompound nbt);
    }

    /**
     * Utility interface for {@link #addJobBatch(EntityPlayer, Stream)}.
     */
    public interface JobSupplier {
        void get(final JobConsumer consumer);
    }

    // --------------------------------------------------------------------- //

    /**
     * Add a batch of jobs which will be sorted by their respective sort orders
     * amongst each other.
     * <p>
     * This is a little bit roundabout, as we want to abstract all internal
     * bookkeeping logic needed for the sorting of added jobs. Essentially this
     * is a "for each job you can give me, please tell this object what that
     * job is", with "this object" being the instance with internal bookkeeping.
     *
     * @param player   the player creating the jobs.
     * @param provider the stream to query for jobs.
     */
    public void addJobBatch(final EntityPlayer player, final Stream<JobSupplier> provider) {
        final JobBatch batch = new JobBatch(player);
        provider.forEach(jobProvider -> jobProvider.get(batch));

        final JobManagerImpl manager = getInstance(player.getEntityWorld());
        batch.finish(manager);
    }

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent event) {
        assert !event.world.isRemote;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        final JobManagerImpl manager = getInstance(event.world);
        manager.updateJobs(event.world);
    }

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            return;
        }
        final int dimension = event.getWorld().provider.getDimension();
        MANAGERS.remove(dimension);
    }

    @SubscribeEvent
    public void onChunkLoad(final ChunkDataEvent.Load event) {
        assert !event.getWorld().isRemote;
        final JobManagerImpl manager = getInstance(event.getWorld());
        manager.loadChunk(event.getChunk().getPos(), event.getData());
    }

    @SubscribeEvent
    public void onChunkSave(final ChunkDataEvent.Save event) {
        assert !event.getWorld().isRemote;
        final JobManagerImpl manager = getInstance(event.getWorld());
        manager.saveChunk(event.getChunk().getPos(), event.getData());
    }

    @SubscribeEvent
    public void onChunkUnload(final ChunkEvent.Unload event) {
        if (event.getWorld().isRemote) {
            return;
        }
        final JobManagerImpl manager = getInstance(event.getWorld());
        manager.unloadChunk(event.getChunk().getPos());
    }

    // --------------------------------------------------------------------- //

    /**
     * The list of all currently active managers, one manager per world.
     */
    private final TIntObjectMap<JobManagerImpl> MANAGERS = new TIntObjectHashMap<>();

    /**
     * Get the manager instance responsible for the specified world.
     * <p>
     * Creates a new manager for the specified world if there isn't one yet.
     *
     * @param world the world to get the manager for.
     * @return the manager for that world.
     */
    private JobManagerImpl getInstance(final World world) {
        final int dimension = world.provider.getDimension();
        JobManagerImpl manager = MANAGERS.get(dimension);
        if (manager == null) {
            final MapStorage storage = world.getPerWorldStorage();
            manager = (JobManagerImpl) storage.getOrLoadData(JobManagerImpl.class, JobManagerImpl.ID);
            if (manager == null) {
                manager = new JobManagerImpl();
                storage.setData(JobManagerImpl.ID, manager);
            }
            MANAGERS.put(dimension, manager);
        }
        return manager;
    }

    // --------------------------------------------------------------------- //

    public static final class JobManagerImpl extends WorldSavedData {
        // --------------------------------------------------------------------- //
        // Computed data.

        private static final String ID = API.MOD_ID + "_blocks";

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
                        BlueprintAPI.deserialize(world, pos, job.getRotation(), data);
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

    /**
     * A reference counting wrapper for an {@link NBTTagCompound}.
     * <p>
     * We keep references to equal tags to avoid data duplication in memory,
     * with the intent of keeping memory usage low(er than it'd otherwise be).
     */
    private static final class JobBlockData {
        private static final String TAG_ID = "id";
        private static final String TAG_DATA = "data";
        private static final String TAG_REFERENCES = "refs";

        long id; // The reference ID of this entry, used in jobs.
        NBTTagCompound data; // The actual data stored in this entry.
        int referenceCount; // The remaining live references to this entry.

        JobBlockData() {
        }

        JobBlockData(final long id, final NBTTagCompound data) {
            this.id = id;
            this.data = data.copy();
        }

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

    /**
     * A list of jobs for a single chunk.
     * <p>
     * Jobs are organized per chunk to allow storing them in the chunk's data
     * section in case the chunk gets unloaded before all jobs in it have been
     * processed.
     */
    private static final class JobChunkStorage {
        private final LinkedList<Job> jobs = new LinkedList<>();

        boolean isEmpty() {
            return jobs.isEmpty();
        }

        void pushJob(final Job job) {
            jobs.addLast(job);
        }

        Job popJob() {
            return jobs.removeFirst();
        }

        NBTTagList serializeNBT() {
            final NBTTagList nbt = new NBTTagList();
            for (final Job job : jobs) {
                nbt.appendTag(job.serializeNBT());
            }
            return nbt;
        }

        void deserializeNBT(final NBTTagList nbt) {
            jobs.clear();
            for (int tagIndex = 0; tagIndex < nbt.tagCount(); tagIndex++) {
                final Job job = new Job();
                job.deserializeNBT(nbt.getCompoundTagAt(tagIndex));
                jobs.addLast(job);
            }
        }
    }

    /**
     * Represents a single, pending job.
     * <p>
     * A job is a pending deserialization / block placement operation. Jobs are
     * already "paid" for, as the materials are consumed from the placing
     * player's inventory the moment jobs are created.
     */
    private static final class Job {
        private static final String TAG_REFERENCE = "ref";
        private static final String TAG_POSITION = "pos";
        private static final String TAG_ROTATION = "rot";

        long dataReference;
        private short compressedPos; // 0xZYYX
        private byte rotation;

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

    /**
     * Utility class for adding jobs in batches, which are then internally
     * sorted by the sort index provided by the converter used to deserialize
     * the data.
     */
    private static class JobBatch implements JobConsumer {
        private final ItemSourceImpl itemSource;
        private final World world;

        // Re-use NBTs to allow GC to collect duplicates while adding large batches.
        private final TObjectIntMap<NBTTagCompound> nbtToId = new TObjectIntHashMap<>();
        private final TIntObjectMap<NBTTagCompound> idToNbt = new TIntObjectHashMap<>();
        private int nextId = 1;

        // Lists of added jobs, categorized by their sort index.
        private final TIntObjectMap<List<BatchedJob>> jobs = new TIntObjectHashMap<>();

        JobBatch(final EntityPlayer player) {
            this.world = player.getEntityWorld();
            this.itemSource = new ItemSourceImpl(player.isCreative(), new InvWrapper(player.inventory));
        }

        @Override
        public void accept(final BlockPos pos, final Rotation rotation, final NBTTagCompound nbt) {
            if (!BlueprintAPI.preDeserialize(itemSource, world, pos, rotation, nbt)) {
                return;
            }

            final int sortIndex = BlueprintAPI.getSortIndex(nbt);
            if (!jobs.containsKey(sortIndex)) {
                jobs.put(sortIndex, new ArrayList<>());
            }

            final int id;
            if (nbtToId.containsKey(nbt)) {
                id = nbtToId.get(nbt);
            } else {
                id = nextId++;
                nbtToId.put(nbt, id);
                idToNbt.put(id, nbt);
            }
            jobs.get(sortIndex).add(new BatchedJob(pos, rotation, id));
        }

        void finish(final JobManagerImpl manager) {
            // Shuffle jobs per sort index, to get a less... boring order when
            // actually deserializing the blocks.
            final Random rng = world.rand;
            jobs.forEachValue(list -> {
                Collections.shuffle(list, rng);
                return true;
            });

            // Sort by sort index (the key of the job map).
            final int[] keys = jobs.keys();
            Arrays.sort(keys);
            for (final int key : keys) {
                for (final BatchedJob job : jobs.get(key)) {
                    manager.addJob(job.pos, job.rotation, idToNbt.get(job.id));
                }
            }
        }

        private static final class BatchedJob {
            final BlockPos pos;
            final Rotation rotation;
            final int id;

            BatchedJob(final BlockPos pos, final Rotation rotation, final int id) {
                this.pos = pos;
                this.rotation = rotation;
                this.id = id;
            }
        }
    }
}
