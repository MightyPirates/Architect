package li.cil.architect.common.blueprint;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import li.cil.architect.api.API;
import li.cil.architect.api.BlueprintAPI;
import li.cil.architect.common.Architect;
import li.cil.architect.common.Settings;
import li.cil.architect.common.inventory.CompoundItemHandler;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageJobDataResponse;
import li.cil.architect.util.BlockPosUtils;
import li.cil.architect.util.ChunkUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public enum JobManager {
    INSTANCE;

    public void addJob(final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound nbt) {
        final JobManagerImpl manager = getInstance(world);
        final ChunkPos chunkPos = new ChunkPos(pos);
        manager.addJob(chunkPos, pos, rotation, nbt);
    }

    public void sendJobDataToClient(final ChunkPos chunkPos, final EntityPlayerMP player) {
        final World world = player.getEntityWorld();
        final JobManagerImpl manager = getInstance(world);
        manager.sendJobDataToClient(chunkPos, player);
    }

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent event) {
        final JobManagerImpl manager = getInstance(event.world);
        manager.updateJobs(event.world);
        if (event.world.getTotalWorldTime() % 1000 == 0) {
            manager.updateProviders(event.world);
        }
    }


    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        final int dimension = event.getWorld().provider.getDimension();
        MANAGERS.remove(dimension);
    }

    @SubscribeEvent
    public void onChunkLoad(final ChunkDataEvent.Load event) {
        final JobManagerImpl manager = getInstance(event.getWorld());
        manager.loadChunk(event.getChunk().getPos(), event.getData());
    }

    @SubscribeEvent
    public void onChunkSave(final ChunkDataEvent.Save event) {
        final JobManagerImpl manager = getInstance(event.getWorld());
        manager.saveChunk(event.getChunk().getPos(), event.getData());
    }

    @SubscribeEvent
    public void onChunkUnload(final ChunkEvent.Unload event) {
        final JobManagerImpl manager = getInstance(event.getWorld());
        manager.unloadChunk(event.getChunk().getPos());
    }

    // --------------------------------------------------------------------- //

    private final TIntObjectMap<JobManagerImpl> MANAGERS = new TIntObjectHashMap<>();

    private JobManagerImpl getInstance(final World world) {
        final int dimension = world.provider.getDimension();
        JobManagerImpl manager = MANAGERS.get(dimension);
        if (manager == null) {
            manager = (JobManagerImpl) world.loadData(JobManagerImpl.class, JobManagerImpl.ID);
            if (manager == null) {
                manager = new JobManagerImpl();
                world.setData(JobManagerImpl.ID, manager);
            }
            MANAGERS.put(dimension, manager);
        }
        return manager;
    }

    private static final class JobManagerImpl extends WorldSavedData {
        // --------------------------------------------------------------------- //
        // Computed data.

        private static final String ID = API.MOD_ID + ":blocks";

        private static final String TAG_NEXT_ID = "nextId";
        private static final String TAG_BLOCK_DATA = "blockData";
        private static final String TAG_JOBS = API.MOD_ID + ":jobs";

        // List of jobs that (in the last provider update) were in range of at
        // least one provider.
        private ArrayList<JobWithProviders> jobsWithProviders = new ArrayList<>();

        private int nextJobIndex;

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

        void addJob(final ChunkPos chunkPos, final BlockPos pos, final Rotation rotation, final NBTTagCompound nbt) {
            final Job job = new Job();
            job.dataReference = addReference(nbt);
            job.setPos(pos);
            job.setRotation(rotation);

            final JobChunkStorage storage = getChunkStorage(chunkPos);
            storage.addJob(job);
        }

        private void removeJob(final ChunkPos chunkPos, final Job job) {
            removeReference(job.dataReference);

            final JobChunkStorage storage = getChunkStorage(chunkPos);
            storage.removeJob(job);
        }

        void sendJobDataToClient(final ChunkPos chunkPos, final EntityPlayerMP player) {
            final JobChunkStorage storage = getChunkStorage(chunkPos);
            final byte[] data = storage.getDataForClient(player);
            if (data != null) {
                Network.INSTANCE.getWrapper().sendTo(new MessageJobDataResponse(chunkPos, data), player);
            }
        }

        void updateJobs(final World world) {
            for (int i = 0, count = Math.min(jobsWithProviders.size(), Settings.maxWorldOperationsPerTick); i < count; i++) {
                if (nextJobIndex >= jobsWithProviders.size()) {
                    nextJobIndex = 0;
                }
                final JobWithProviders job = jobsWithProviders.get(nextJobIndex++);

                final IItemHandler materials = job.getMaterials();
                final BlockPos pos = job.getPos();
                final Rotation rotation = job.getRotation();
                final NBTTagCompound blockData = job.getData(this);
                if (BlueprintAPI.deserialize(materials, world, pos, rotation, blockData)) {
                    removeJob(job.chunkPos, job.job);
                }
            }
        }

        void updateProviders(final World world) {
            jobsWithProviders.removeIf(job -> {
                final BlockPos pos = job.getPos();
                return !ProviderManager.INSTANCE.hasProviders(world, BlockPosUtils.toVec3d(pos));
            });


        }

        void loadChunk(final ChunkPos chunk, final NBTTagCompound data) {
            JobChunkStorage storage = getChunkStorage(chunk);
            storage.deserializeNBT(data.getTagList(TAG_JOBS, Constants.NBT.TAG_COMPOUND));
        }

        void saveChunk(final ChunkPos chunk, final NBTTagCompound data) {
            JobChunkStorage storage = getChunkStorage(chunk);
            data.setTag(TAG_JOBS, storage.serializeNBT());
        }

        void unloadChunk(final ChunkPos chunkPos) {
            final long id = ChunkUtils.chunkPosToLong(chunkPos);
            chunkData.remove(id);
        }

        // --------------------------------------------------------------------- //

        private JobChunkStorage getChunkStorage(final ChunkPos chunkPos) {
            final long chunkId = ChunkUtils.chunkPosToLong(chunkPos);
            JobChunkStorage chunkStorage = chunkData.get(chunkId);
            if (chunkStorage == null) {
                chunkStorage = new JobChunkStorage();
                chunkData.put(chunkId, chunkStorage);
            }
            return chunkStorage;
        }

        private long addReference(final NBTTagCompound nbt) {
            JobBlockData entry = nbtToData.get(nbt);
            if (entry == null) {
                entry = new JobBlockData(nextId++, nbt);
                idToData.put(entry.id, entry);
                nbtToData.put(nbt, entry);
            }
            entry.referenceCount++;
            markDirty();
            return entry.id;
        }

        private void removeReference(final long id) {
            JobBlockData entry = idToData.get(id);
            if (entry == null) {
                Architect.getLog().error("Failed retrieving block data for id, meaning the data was disposed even though there were still references to it. This should be impossible in normal operation. If you're 110% sure your server didn't corrupt some of its save data, e.g. due to a crash, please report this!");
                return;
            }
            entry.referenceCount--;
            markDirty();
            if (entry.referenceCount <= 0) {
                idToData.remove(entry.id);
                nbtToData.remove(entry.data);
            }
        }

        private NBTTagCompound getBlockData(final long id) {
            JobBlockData entry = idToData.get(id);
            if (entry == null) {
                Architect.getLog().error("Failed retrieving block data for id, meaning the data was disposed even though there were still references to it. This should be impossible in normal operation. If you're 110% sure your server didn't corrupt some of its save data, e.g. due to a crash, please report this!");
                return new NBTTagCompound();
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

    private static final class JobBlockData {
        private static final String TAG_ID = "id";
        private static final String TAG_DATA = "data";
        private static final String TAG_REFERENCES = "refs";

        long id;
        NBTTagCompound data;
        int referenceCount;

        JobBlockData(final long id, final NBTTagCompound data) {
            this.id = id;
            this.data = data.copy();
        }

        JobBlockData() {
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

    private static final class JobChunkStorage {
        private final BitSet jobsForClients = new BitSet(16 * 16 * 256);
        private byte[] serializedJobsForClients;
        private final HashSet<NetHandlerPlayServer> sentToClients = new HashSet<>();

        private final List<Job> jobs = new ArrayList<>();

        void addJob(final Job job) {
            jobs.add(job);
            jobsForClients.set(job.compressedPos);
            sentToClients.clear();
            serializedJobsForClients = null;
        }

        void removeJob(final Job job) {
            jobs.remove(job);
            jobsForClients.clear(job.compressedPos);
            sentToClients.clear();
            serializedJobsForClients = null;
        }

        @Nullable
        byte[] getDataForClient(final EntityPlayerMP player) {
            if (sentToClients.contains(player.connection)) {
                return null;
            }
            sentToClients.add(player.connection);

            if (serializedJobsForClients == null) {
                serializedJobsForClients = jobsForClients.toByteArray();
            }
            return serializedJobsForClients;
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
                jobs.add(job);
            }
        }
    }

    private static final class Job {
        private static final String TAG_REFERENCE = "ref";
        private static final String TAG_POSITION = "pos";
        private static final String TAG_ROTATION = "rot";

        long dataReference;
        private short compressedPos; // 0xZYYX
        private byte rotation;

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

    private static final class JobWithProviders {
        final ChunkPos chunkPos;
        final Job job;
        final List<Provider> providers;
        private IItemHandler cachedCompoundHandler;

        private JobWithProviders(final ChunkPos chunkPos, final Job job, final List<Provider> providers) {
            this.chunkPos = chunkPos;
            this.job = job;
            this.providers = providers;
        }

        BlockPos getPos() {
            return job.getPos(chunkPos);
        }

        Rotation getRotation() {
            return job.getRotation();
        }

        NBTTagCompound getData(final JobManagerImpl manager) {
            return manager.getBlockData(job.dataReference);
        }

        IItemHandler getMaterials() {
            if (cachedCompoundHandler == null) {
                final int providerCount = providers.size();
                final IItemHandler[] providedHandlers = new IItemHandler[providerCount];
                for (int j = 0; j < providerCount; j++) {
                    providedHandlers[j] = providers.get(j).getMaterials();
                }
                cachedCompoundHandler = new CompoundItemHandler(providedHandlers);
            }
            return cachedCompoundHandler;
        }
    }
}
