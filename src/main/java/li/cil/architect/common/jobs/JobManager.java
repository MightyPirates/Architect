package li.cil.architect.common.jobs;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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

    /**
     * Utility interface for {@link #getJobTester(World)}.
     */
    public interface JobTester {
        boolean hasJob(final BlockPos pos);
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
     * @param player       the player creating the jobs.
     * @param allowPartial whether to allow partial placement.
     * @param provider     the stream to query for jobs.
     */
    public void addJobBatch(final EntityPlayer player, final boolean allowPartial, final Stream<JobSupplier> provider) {
        final JobBatch batch = new JobBatch(player, allowPartial);
        provider.forEach(jobProvider -> jobProvider.get(batch));

        final JobManagerImpl manager = getInstance(player.getEntityWorld());
        batch.finish(manager);
    }

    /**
     * Get an object for the specified world that can be used to test for the
     * presence of jobs at some position in that world.
     * <p>
     * This is used as a performance shortcut when batch-adding jobs, as it
     * saves us the manager lookup for each potentially added job.
     *
     * @param world the world to get the tester for.
     * @return the tester for presence of jobs.
     */
    public JobTester getJobTester(final World world) {
        final JobManagerImpl manager = getInstance(world);
        return manager::hasJob;
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
        manager.loadChunk(event.getChunk().getChunkCoordIntPair(), event.getData());
    }

    @SubscribeEvent
    public void onChunkSave(final ChunkDataEvent.Save event) {
        assert !event.getWorld().isRemote;
        final JobManagerImpl manager = getInstance(event.getWorld());
        manager.saveChunk(event.getChunk().getChunkCoordIntPair(), event.getData());
    }

    @SubscribeEvent
    public void onChunkUnload(final ChunkEvent.Unload event) {
        if (event.getWorld().isRemote) {
            return;
        }
        final JobManagerImpl manager = getInstance(event.getWorld());
        manager.unloadChunk(event.getChunk().getChunkCoordIntPair());
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
}
