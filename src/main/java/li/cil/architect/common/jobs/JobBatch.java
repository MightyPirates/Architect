package li.cil.architect.common.jobs;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import li.cil.architect.common.converter.MaterialSourceImpl;
import li.cil.architect.common.inventory.CompoundItemHandler;
import li.cil.architect.common.item.ItemProviderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
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

    private final MaterialSourceImpl materialSource;
    private final EntityPlayer player;
    private final World world;
    private final JobManager.JobTester jobTester;

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
        this.jobTester = JobManager.INSTANCE.getJobTester(world);
        final IItemHandler inventory = new InvWrapper(player.inventory);
        final List<IItemHandler> providers = ItemProviderItem.findProviders(player.getPositionVector(), inventory);
        providers.add(inventory);
        final IItemHandler compoundProvider = new CompoundItemHandler(providers.toArray(new IItemHandler[providers.size()]));
        this.materialSource = new MaterialSourceImpl(player.isCreative(), compoundProvider);
    }

    void finish(final JobManagerImpl manager) {
        if (!consumeEnergy()) {
            return;
        }

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
        // Don't venture into unloaded territory... this should never really
        // happen anyway, because the player would have to be nearby.
        if (!world.isBlockLoaded(pos)) {
            return;
        }

        // Don't allow multiple jobs at one position, to avoid item rains.
        if (jobTester.hasJob(pos)) {
            return;
        }

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

        if (!ConverterAPI.preDeserialize(materialSource, world, pos, rotation, nbt)) {
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

    private boolean consumeEnergy() {
        if (player.isCreative()) {
            return true;
        }

        int blockCount = 0;
        for (final List<BatchedJob> list : jobs.valueCollection()) {
            blockCount += list.size();
        }

        if (Settings.useEnergy) {
            final int energyRequired = Constants.ENERGY_PER_BLOCK * blockCount;
            int energyReceived = 0;
            final List<IEnergyStorage> energyStorages = new ArrayList<>();

            // Find energy storages in the player's inventory until we found
            // enough of them to cover the energy costs of the operation.
            final int slotCount = player.inventory.getSizeInventory();
            for (int slot = 0; slot < slotCount; slot++) {
                final ItemStack stack = player.inventory.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }

                final IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
                if (energyStorage == null || !energyStorage.canExtract()) {
                    continue;
                }

                energyReceived += energyStorage.extractEnergy(energyRequired - energyReceived, true);
                energyStorages.add(energyStorage);
                if (energyReceived >= energyRequired) {
                    break;
                }
            }

            // If we can't satisfy the energy usage, let the player know.
            if (energyReceived < energyRequired) {
                player.sendMessage(new TextComponentTranslation(Constants.MESSAGE_PLACEMENT_NOT_ENOUGH_ENERGY));
                return false;
            }

            // Actually extract the energy.
            energyReceived = 0;
            for (final IEnergyStorage energyStorage : energyStorages) {
                energyReceived += energyStorage.extractEnergy(energyRequired - energyReceived, false);
                if (Math.abs(energyRequired - energyReceived) < 1) {
                    break;
                }
            }
        } else {
            player.addExhaustion(Constants.EXHAUSTION_PER_BLOCK * blockCount);
        }

        return true;
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
