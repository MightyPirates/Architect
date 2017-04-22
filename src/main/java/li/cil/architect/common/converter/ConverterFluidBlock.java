package li.cil.architect.common.converter;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.api.prefab.converter.AbstractConverter;
import li.cil.architect.common.config.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nullable;
import java.util.Collections;

public final class ConverterFluidBlock extends AbstractConverter {
    public ConverterFluidBlock() {
        super(Constants.UUID_CONVERTER_FLUID_BLOCKS);
    }

    @Override
    public int getSortIndex(final NBTBase data) {
        return SortIndex.FLUID_BLOCK;
    }

    @Override
    public Iterable<ItemStack> getItemCosts(final NBTBase data) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<FluidStack> getFluidCosts(final NBTBase data) {
        final FluidStack costs = getFluidStack(data);
        if (costs != null) {
            return Collections.singleton(costs);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos).getActualState(world, pos);
        final IBlockState mapped = ConverterAPI.mapToBlock(state);
        return mapped != null && canSerialize(world, pos, mapped);
    }

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        final Block block = state.getBlock();
        if (block.hasTileEntity(state)) {
            return false;
        }
        if (block == Blocks.WATER || block == Blocks.LAVA) {
            return state.getValue(BlockLiquid.LEVEL) == 0;
        }
        if (block instanceof IFluidBlock) {
            final IFluidBlock fluidBlock = (IFluidBlock) block;
            final FluidStack drained = fluidBlock.drain(world, pos, false);
            return drained != null && drained.amount == Fluid.BUCKET_VOLUME;
        }

        return false;
    }

    @Nullable
    @Override
    protected FluidStack getFluidStack(final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return null;
        }

        final Block block = state.getBlock();
        if (block == Blocks.WATER) {
            return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
        }
        if (block == Blocks.LAVA) {
            return new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME);
        }
        if (block instanceof IFluidBlock) {
            final IFluidBlock fluidBlock = (IFluidBlock) block;
            return new FluidStack(fluidBlock.getFluid(), Fluid.BUCKET_VOLUME);
        }

        return null;
    }
}
