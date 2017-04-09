package li.cil.architect.common.jobs;

import net.minecraft.nbt.NBTTagList;

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

    private final LinkedList<Job> jobs = new LinkedList<>();

    // --------------------------------------------------------------------- //

    boolean isEmpty() {
        return jobs.isEmpty();
    }

    void pushJob(final Job job) {
        jobs.addLast(job);
    }

    Job popJob() {
        return jobs.removeFirst();
    }

    // --------------------------------------------------------------------- //

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
