package li.cil.architect.common.jobs;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.converter.MaterialSourceImpl;
import li.cil.architect.common.inventory.CompoundItemHandler;
import li.cil.architect.common.item.ItemProviderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Utility class for adding jobs in batches, which are then internally
 * sorted by the sort index provided by the converter used to deserialize
 * the data.
 */
class JobBatch implements JobManager.JobConsumer {
    // --------------------------------------------------------------------- //
    // Computed data.

    private final MaterialSourceImpl itemSource;
    private final EntityPlayer player;
    private final World world;

    // Re-use NBTs to allow GC to collect duplicates while adding large batches.
    private final TObjectIntMap<NBTTagCompound> nbtToId = new TObjectIntHashMap<>();
    private final TIntObjectMap<NBTTagCompound> idToNbt = new TIntObjectHashMap<>();
    private int nextId = 1;
    private boolean anyCanceled;

    // Lists of added jobs, categorized by their sort index.
    private final TIntObjectMap<List<BatchedJob>> jobs = new TIntObjectHashMap<>();

    // --------------------------------------------------------------------- //

    JobBatch(final EntityPlayer player) {
        this.player = player;
        this.world = player.getEntityWorld();
        final IItemHandler inventory = new InvWrapper(player.inventory);
        final List<IItemHandler> providers = ItemProviderItem.findProviders(player.getPositionVector(), inventory);
        providers.add(inventory);
        final IItemHandler compoundProvider = new CompoundItemHandler(providers.toArray(new IItemHandler[providers.size()]));
        this.itemSource = new MaterialSourceImpl(player.isCreative(), compoundProvider);
    }

    void finish(final JobManagerImpl manager) {
        if (anyCanceled) {
            player.sendMessage(new TextComponentTranslation(Constants.MESSAGE_PLACEMENT_CANCELED));
        }

        // Shuffle jobs per sort index, to get a less... boring order when
        // actually deserializing the blocks.
        final Random rng = world.rand;
        jobs.forEachValue(list -> {
            Collections.shuffle(list, rng);
            return true;
        });

        // Sort by sort index (the key of the job map).
        final int[] sortIndices = jobs.keys();
        Arrays.sort(sortIndices);
        for (final int sortIndex : sortIndices) {
            for (final BatchedJob job : jobs.get(sortIndex)) {
                manager.addJob(sortIndex, job.pos, job.rotation, idToNbt.get(job.id));
            }
        }
    }

    // --------------------------------------------------------------------- //
    // JobConsumer

    @Override
    public void accept(final BlockPos pos, final Rotation rotation, final NBTTagCompound nbt) {
        // Respect spawn protection and world border.
        if (!world.provider.canMineBlock(player, pos)) {
            anyCanceled = true;
            return;
        }

        // Give permission mods and such a chance to forbid the placement.
        // Note: looking at ItemLilyPad, the *correct* use would be to actually
        // have the block placed after taking the snapshot, *then* firing the
        // event, and reverting if the event is canceled. Since we want to know
        // this in advance (because we don't want to store the responsible
        // player for each pending job!) this... doesn't.
        // Let's hope it'll be fine.
        final BlockEvent.PlaceEvent event = ForgeEventFactory.onPlayerBlockPlace(player, BlockSnapshot.getBlockSnapshot(world, pos), EnumFacing.UP, EnumHand.MAIN_HAND);
        if (event.isCanceled()) {
            anyCanceled = true;
            return;
        }

        if (!ConverterAPI.preDeserialize(itemSource, world, pos, rotation, nbt)) {
            return;
        }

        final int sortIndex = ConverterAPI.getSortIndex(nbt);
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

    // --------------------------------------------------------------------- //

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
