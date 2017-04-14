package li.cil.architect.common.jobs;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import li.cil.architect.util.ChunkUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.BitSet;
import java.util.LinkedList;

/**
 * A list of jobs for a single chunk.
 * <p>
 * Jobs are organized per chunk to allow storing them in the chunk's data
 * section in case the chunk gets unloaded before all jobs in it have been
 * processed.
 */
final class JobChunkStorage {
    // --------------------------------------------------------------------- //
    // Persisted data.

    // NBT tag names.
    private static final String TAG_SORT_INDEX = "sortOrder";
    private static final String TAG_LIST = "list";

    private final BitSet positions = new BitSet(16 * 16 * 256);
    private final TIntObjectMap<LinkedList<Job>> jobsBySortIndex = new TIntObjectHashMap<>();
    private int lowestSortIndex = Integer.MAX_VALUE;

    // --------------------------------------------------------------------- //

    boolean isEmpty() {
        return jobsBySortIndex.isEmpty();
    }

    boolean contains(final BlockPos pos) {
        return positions.get(ChunkUtils.posToShort(pos) & 0xFFFF);
    }

    int getSortIndex() {
        return lowestSortIndex;
    }

    void pushJob(final int sortIndex, final Job job) {
        if (contains(job)) {
            throw new IllegalArgumentException();
        }
        add(job);

        if (sortIndex < lowestSortIndex || isEmpty()) {
            lowestSortIndex = sortIndex;
        }

        final LinkedList<Job> jobs;
        if (jobsBySortIndex.containsKey(sortIndex)) {
            jobs = jobsBySortIndex.get(sortIndex);
        } else {
            jobs = new LinkedList<>();
            jobsBySortIndex.put(sortIndex, jobs);
        }

        jobs.addLast(job);
    }

    Job popJob() {
        final LinkedList<Job> jobs = jobsBySortIndex.get(lowestSortIndex);
        final Job job = jobs.removeFirst();
        remove(job);
        if (jobs.isEmpty()) {
            jobsBySortIndex.remove(lowestSortIndex);
            lowestSortIndex = findLowestSortIndex();
        }
        return job;
    }

    // --------------------------------------------------------------------- //

    NBTTagList serializeNBT() {
        final NBTTagList nbt = new NBTTagList();
        jobsBySortIndex.forEachEntry((sortIndex, jobs) -> {
            final NBTTagCompound jobsNbt = new NBTTagCompound();
            jobsNbt.setInteger(TAG_SORT_INDEX, sortIndex);
            final NBTTagList jobListNbt = new NBTTagList();
            for (final Job job : jobs) {
                jobListNbt.appendTag(job.serializeNBT());
            }
            jobsNbt.setTag(TAG_LIST, jobListNbt);
            nbt.appendTag(jobsNbt);
            return true;
        });
        return nbt;
    }

    void deserializeNBT(final NBTTagList nbt) {
        positions.clear();
        jobsBySortIndex.clear();
        lowestSortIndex = Integer.MAX_VALUE;
        for (int tagIndex = 0; tagIndex < nbt.tagCount(); tagIndex++) {
            final NBTTagCompound jobsNbt = nbt.getCompoundTagAt(tagIndex);
            final int sortIndex = jobsNbt.getInteger(TAG_SORT_INDEX);
            if (sortIndex < lowestSortIndex) {
                lowestSortIndex = sortIndex;
            }
            final LinkedList<Job> jobList = new LinkedList<>();
            final NBTTagList jobListNbt = jobsNbt.getTagList(TAG_LIST, NBT.TAG_COMPOUND);
            for (int jobTagIndex = 0; jobTagIndex < jobListNbt.tagCount(); jobTagIndex++) {
                final Job job = new Job();
                job.deserializeNBT(jobListNbt.getCompoundTagAt(jobTagIndex));

                if (contains(job)) {
                    // Should never have happened. Someone must have meddled
                    // with our NBT, so just dump duplicates.
                    continue;
                }
                add(job);

                jobList.addLast(job);
            }
            jobsBySortIndex.put(sortIndex, jobList);
        }
    }

    // --------------------------------------------------------------------- //

    private void add(final Job job) {
        positions.set(job.compressedPos & 0xFFFF);
    }

    private void remove(final Job job) {
        positions.clear(job.compressedPos & 0xFFFF);
    }

    private boolean contains(final Job job) {
        return positions.get(job.compressedPos & 0xFFFF);
    }

    private int findLowestSortIndex() {
        if (isEmpty()) {
            return Integer.MAX_VALUE;
        }
        final int[] sortIndices = jobsBySortIndex.keys();
        int minSortIndex = sortIndices[0];
        for (int i = 1; i < sortIndices.length; i++) {
            final int sortIndex = sortIndices[i];
            if (sortIndex < minSortIndex) {
                minSortIndex = sortIndex;
            }
        }
        return minSortIndex;
    }
}
