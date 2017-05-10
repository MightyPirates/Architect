package li.cil.architect.common.jobs;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.api.ConverterAPIImpl;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import li.cil.architect.common.converter.MaterialSourceImpl;
import li.cil.architect.common.inventory.CompoundItemHandler;
import li.cil.architect.common.item.ItemProviderFluid;
import li.cil.architect.common.item.ItemProviderItem;
import li.cil.architect.util.FluidHandlerUtils;
import li.cil.architect.util.ItemHandlerUtils;
import li.cil.architect.util.ItemStackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.FoodStats;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.*;

/**
 * Utility class for adding jobs in batches, which are then internally
 * sorted by the sort index provided by the converter used to deserialize
 * the data.
 */
class JobBatch implements JobManager.JobConsumer {
    // --------------------------------------------------------------------- //
    // Computed data.

    private final boolean allowPartial;
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

    JobBatch(final EntityPlayer player, final boolean allowPartial) {
        this.player = player;
        this.world = player.getEntityWorld();
        this.jobTester = JobManager.INSTANCE.getJobTester(world);
        this.allowPartial = allowPartial;
        final IItemHandlerModifiable inventory = new InvWrapper(player.inventory);
        final List<IItemHandler> itemHandlers = ItemProviderItem.findProviders(player.getPositionVector(), inventory);
        final List<IFluidHandler> fluidHandlers = ItemProviderFluid.findProviders(player.getPositionVector(), inventory);
        addItemHandlers(inventory, itemHandlers);
        addFluidHandlers(inventory, fluidHandlers);
        final IItemHandler compoundItemHandler = new CompoundItemHandler(itemHandlers.toArray(new IItemHandler[itemHandlers.size()]));
        final IFluidHandler compoundFluidHandler = new FluidHandlerConcatenate(fluidHandlers.toArray(new IFluidHandler[fluidHandlers.size()]));
        this.materialSource = new MaterialSourceImpl(player.isCreative(), compoundItemHandler, compoundFluidHandler);
    }

    void finish(final JobManagerImpl manager) {
        if (!simulateConsumeMaterials()) {
            player.sendMessage(new TextComponentTranslation(Constants.MESSAGE_PLACEMENT_MISSING_MATERIALS));
            return;
        }

        if (!consumeEnergy()) {
            player.sendMessage(new TextComponentTranslation(Constants.MESSAGE_PLACEMENT_NOT_ENOUGH_ENERGY));
            return;
        }

        if (anyCanceled) {
            player.sendMessage(new TextComponentTranslation(Constants.MESSAGE_PLACEMENT_CANCELED));
        }

        consumeMaterials();

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

        if (!ConverterAPIImpl.isValidPosition(world, pos)) {
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

    private void addItemHandlers(final IItemHandler inventory, final List<IItemHandler> itemHandlers) {
        itemHandlers.add(inventory);

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (ItemStackUtils.isEmpty(stack)) {
                continue;
            }

            final IItemHandler capability = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (capability == null) {
                continue;
            }

            itemHandlers.add(capability);
        }
    }

    private void addFluidHandlers(final IItemHandlerModifiable inventory, final List<IFluidHandler> fluidHandlers) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            final ItemStack stack = inventory.getStackInSlot(slot);
            if (ItemStackUtils.isEmpty(stack)) {
                continue;
            }

            final IFluidHandler capability = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            if (capability == null || !FluidHandlerUtils.canDrain(capability)) {
                continue;
            }

            fluidHandlers.add(capability);
        }
    }

    private boolean simulateConsumeMaterials() {
        // Simulate using a full copy of our materials in case the user only
        // wants to place the blueprint if all materials are available. We
        // need to do it this way because otherwise (if we just simulated
        // extraction) converters could consume the same resource multiple
        // times.
        final MaterialSourceImpl simulationSource = new MaterialSourceImpl(false, ItemHandlerUtils.copy(materialSource.getItemHandler()), FluidHandlerUtils.copy(materialSource.getFluidHandler()));
        return jobs.forEachValue(list -> {
            for (final Iterator<BatchedJob> iterator = list.iterator(); iterator.hasNext(); ) {
                final BatchedJob job = iterator.next();
                if (!ConverterAPI.preDeserialize(simulationSource, world, job.pos, job.rotation, idToNbt.get(job.id))) {
                    if (!allowPartial) {
                        return false;
                    }
                    iterator.remove();
                }
            }
            return true;
        });
    }

    private void consumeMaterials() {
        jobs.forEachValue(list -> {
            for (final BatchedJob job : list) {
                final boolean success = ConverterAPI.preDeserialize(materialSource, world, job.pos, job.rotation, idToNbt.get(job.id));
                assert success : "Inconsistent result from preDeserialize in simulation and actual consumption: " + idToNbt.get(job.id);
            }
            return true;
        });

        player.inventory.markDirty();
    }

    private boolean consumeEnergy() {
        if (player.isCreative()) {
            return true;
        }

        int blockCount = 0;
        for (final List<BatchedJob> list : jobs.valueCollection()) {
            blockCount += list.size();
        }

        if (Settings.useEnergy) {
            return consumeEnergy(blockCount);
        } else if (!player.capabilities.disableDamage && !world.isRemote) {
            // Exhaustion is normally capped at 40, this allows us to bypass that cap by removing hunger directly
            // Exhaustion has a 4:1 ratio with hunger.
            final float exhaustion = (float) (Settings.exhaustionPerBlock * blockCount);
            final EnumDifficulty enumdifficulty = world.getDifficulty();
            final FoodStats foodStats = player.getFoodStats();
            final float foodSaturationLevel = foodStats.getSaturationLevel();
            float hungerCost = exhaustion * 0.25f;
            if (foodSaturationLevel >= hungerCost) {
                foodStats.setFoodSaturationLevel(Math.max(foodSaturationLevel - hungerCost, 0f));
            } else if (foodSaturationLevel < hungerCost) {
                hungerCost -= foodSaturationLevel;
                foodStats.setFoodSaturationLevel(0f);
                if (enumdifficulty != EnumDifficulty.PEACEFUL) {
                    foodStats.setFoodLevel(Math.max(foodStats.getFoodLevel() - (int) hungerCost, 0));
                }
            }
        }

        return true;
    }

    private boolean consumeEnergy(final int blockCount) {
        final int energyRequired = Constants.ENERGY_PER_BLOCK * blockCount;
        int energyReceived = 0;
        final List<IEnergyStorage> energyStorages = new ArrayList<>();

        // Find energy storages in the player's inventory until we found
        // enough of them to cover the energy costs of the operation.
        final int slotCount = player.inventory.getSizeInventory();
        for (int slot = 0; slot < slotCount; slot++) {
            final ItemStack stack = player.inventory.getStackInSlot(slot);
            if (ItemStackUtils.isEmpty(stack)) {
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
