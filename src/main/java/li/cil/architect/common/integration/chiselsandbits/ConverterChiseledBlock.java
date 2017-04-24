package li.cil.architect.common.integration.chiselsandbits;

import li.cil.architect.api.converter.MaterialSource;
import li.cil.architect.api.prefab.converter.AbstractConverter;
import li.cil.architect.util.ItemStackUtils;
import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConverterChiseledBlock extends AbstractConverter {
    private static final String TAG_NBT = "nbt";

    // --------------------------------------------------------------------- //

    ConverterChiseledBlock() {
        super(ProxyChiselsAndBits.UUID_CONVERTER_CHISELED_BLOCK);
    }

    // --------------------------------------------------------------------- //
    // Converter

    @Override
    @SideOnly(Side.CLIENT)
    public Iterable<ItemStack> getItemCosts(final NBTBase data) {
        final NBTBlobConverter converter = new NBTBlobConverter();
        converter.readChisleData(((NBTTagCompound) data).getCompoundTag(TAG_NBT));
        final VoxelBlob pattern = converter.getBlob();

        final List<ItemStack> result = new ArrayList<>();
        final Map<Integer, Integer> bits = pattern.getBlockSums();
        for (final Map.Entry<Integer, Integer> entry : bits.entrySet()) {
            final int bitType = entry.getKey();
            final int bitCount = entry.getValue();

            if (bitType == 0) {
                continue;
            }

            final IBlockState state = ModUtil.getStateById(bitType);
            result.add(new ItemStack(state.getBlock(), bitCount));
        }
        return result;
    }

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        return world.getTileEntity(pos) instanceof TileEntityBlockChiseled;
    }

    // --------------------------------------------------------------------- //
    // AbstractConverter

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        return false;
    }

    @Override
    protected void postSerialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
        super.postSerialize(world, pos, state, data);

        final TileEntity tileEntity = world.getTileEntity(pos);
        assert tileEntity instanceof TileEntityBlockChiseled : "canSerialize lied or was ignored";
        final TileEntityBlockChiseled chiseledBlock = (TileEntityBlockChiseled) tileEntity;

        final NBTTagCompound nbt = new NBTTagCompound();
        new NBTBlobConverter(false, chiseledBlock).writeChisleData(nbt, true);

        // Strip stuff written by TileEntity base class.
        nbt.removeTag("x");
        nbt.removeTag("y");
        nbt.removeTag("z");
        nbt.removeTag("id");

        data.setTag(TAG_NBT, nbt);
    }

    @Override
    public boolean preDeserialize(final MaterialSource materialSource, final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final NBTBlobConverter converter = new NBTBlobConverter();
        converter.readChisleData(((NBTTagCompound) data).getCompoundTag(TAG_NBT));
        final VoxelBlob pattern = converter.getBlob();

        final Map<Integer, Integer> bits = pattern.getBlockSums();
        if (!extractBits(materialSource, bits, true)) {
            return false;
        }

        extractBits(materialSource, bits, false);
        return true;
    }

    @Override
    protected void postDeserialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
        super.postDeserialize(world, pos, state, data);
        if (!data.hasKey(TAG_NBT, Constants.NBT.TAG_COMPOUND)) {
            return;
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return;
        }

        final NBTTagCompound nbt = data.getCompoundTag(TAG_NBT);
        final NBTTagCompound currentNbt = new NBTTagCompound();
        tileEntity.writeToNBT(currentNbt);
        currentNbt.merge(nbt);
        tileEntity.readFromNBT(currentNbt);
    }

    // --------------------------------------------------------------------- //

    private boolean extractBits(final MaterialSource materialSource, final Map<Integer, Integer> bits, final boolean simulate) {
        final IItemHandler itemHandler = getItemHandler(materialSource, simulate);

        final List<BagInventory> bags = getBags(itemHandler);
        for (final Map.Entry<Integer, Integer> entry : bits.entrySet()) {
            final int bitType = entry.getKey();
            final int bitCount = entry.getValue();

            if (bitType == 0) {
                continue;
            }

            int remaining = bitCount - ModUtil.consumeBagBit(bags, bitType, bitCount);
            if (remaining > 0) {
                int slot = findSlotWithBitType(itemHandler, bitType, 0);
                while (slot >= 0) {
                    final ItemStack stack = itemHandler.extractItem(slot, remaining, simulate);
                    if (stack.stackSize >= remaining) {
                        remaining = 0;
                        break;
                    } else {
                        remaining -= stack.stackSize;
                        slot = findSlotWithBitType(itemHandler, bitType, slot + 1);
                    }
                }
            }

            if (remaining > 0) {
                return false;
            }
        }

        return true;
    }

    private IItemHandler getItemHandler(final MaterialSource materialSource, final boolean simulate) {
        final IItemHandler itemHandler;
        if (simulate) {
            final IItemHandler actualItemHandler = materialSource.getItemHandler();
            itemHandler = new ItemStackHandler(actualItemHandler.getSlots());
            for (int slot = 0; slot < actualItemHandler.getSlots(); slot++) {
                final ItemStack stack = actualItemHandler.getStackInSlot(slot);
                itemHandler.insertItem(slot, stack != null ? stack.copy() : null, false);
            }
        } else {
            itemHandler = materialSource.getItemHandler();
        }
        return itemHandler;
    }

    private static List<BagInventory> getBags(final IItemHandler itemHandler) {
        final List<BagInventory> result = new ArrayList<>();
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            final ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemBitBag) {
                result.add(new BagInventory(stack));
            }
        }
        return result;
    }

    private int findSlotWithBitType(final IItemHandler itemHandler, final int bitType, final int startAt) {
        for (int slot = startAt; slot < itemHandler.getSlots(); slot++) {
            final ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!ItemStackUtils.isEmpty(stack) && stack.getItem() instanceof ItemChiseledBit && ItemChiseledBit.sameBit(stack, bitType)) {
                return slot;
            }
        }
        return -1;
    }
}
