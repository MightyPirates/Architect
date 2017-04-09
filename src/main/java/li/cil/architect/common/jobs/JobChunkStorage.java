package li.cil.architect.common.jobs;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

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
    private static final String TAG_SORT_ORDER = "sortOrder";
    private static final String TAG_LIST = "list";

    private final TIntObjectMap<LinkedList<Job>> jobsBySortOrder = new TIntObjectHashMap<>();
    private int lowestSortOrder = Integer.MAX_VALUE;

    // --------------------------------------------------------------------- //

    boolean isEmpty() {
        return jobsBySortOrder.isEmpty();
    }

    int getSortOrder() {
        return lowestSortOrder;
    }

    void pushJob(final int sortOrder, final Job job) {
        if (sortOrder < lowestSortOrder || isEmpty()) {
            lowestSortOrder = sortOrder;
        }

        final LinkedList<Job> jobs;
        if (jobsBySortOrder.containsKey(sortOrder)) {
            jobs = jobsBySortOrder.get(sortOrder);
        } else {
            jobs = new LinkedList<>();
            jobsBySortOrder.put(sortOrder, jobs);
        }

        jobs.addLast(job);
    }

    Job popJob() {
        final LinkedList<Job> jobs = jobsBySortOrder.get(lowestSortOrder);
        final Job result = jobs.removeFirst();
        if (jobs.isEmpty()) {
            jobsBySortOrder.remove(lowestSortOrder);
            lowestSortOrder = findLowestSortOrder();
        }
        return result;
    }

    // --------------------------------------------------------------------- //

    NBTTagList serializeNBT() {
        final NBTTagList nbt = new NBTTagList();
        jobsBySortOrder.forEachEntry((sortOrder, jobs) -> {
            final NBTTagCompound jobsNbt = new NBTTagCompound();
            jobsNbt.setInteger(TAG_SORT_ORDER, sortOrder);
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
        jobsBySortOrder.clear();
        lowestSortOrder = Integer.MAX_VALUE;
        for (int tagIndex = 0; tagIndex < nbt.tagCount(); tagIndex++) {
            final NBTTagCompound jobsNbt = nbt.getCompoundTagAt(tagIndex);
            final int sortOrder = jobsNbt.getInteger(TAG_SORT_ORDER);
            if (sortOrder < lowestSortOrder) {
                lowestSortOrder = sortOrder;
            }
            final LinkedList<Job> jobList = new LinkedList<>();
            final NBTTagList jobListNbt = jobsNbt.getTagList(TAG_LIST, NBT.TAG_COMPOUND);
            for (int jobTagIndex = 0; jobTagIndex < jobListNbt.tagCount(); jobTagIndex++) {
                final Job job = new Job();
                job.deserializeNBT(jobListNbt.getCompoundTagAt(jobTagIndex));
                jobList.addLast(job);
            }
            jobsBySortOrder.put(sortOrder, jobList);
        }
    }

    // --------------------------------------------------------------------- //

    private int findLowestSortOrder() {
        if (isEmpty()) {
            return Integer.MAX_VALUE;
        }
        final int[] sortOrders = jobsBySortOrder.keys();
        int minSortOrder = sortOrders[0];
        for (int i = 1; i < sortOrders.length; i++) {
            final int sortOrder = sortOrders[i];
            if (sortOrder < minSortOrder) {
                minSortOrder = sortOrder;
            }
        }
        return minSortOrder;
    }
}
