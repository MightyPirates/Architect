package li.cil.architect.common.integration.minecraft;

import li.cil.architect.api.converter.MaterialSource;
import li.cil.architect.api.prefab.converter.AbstractConverter;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;

public class ConverterBed extends AbstractConverter {
    ConverterBed() {
        super(ProxyMinecraft.UUID_CONVERTER_MINECRAFT_BED);
    }

    @Override
    public Iterable<ItemStack> getItemCosts(final NBTBase data) {
        if (isHeadPart(data)) {
            return Collections.emptyList();
        }
        return super.getItemCosts(data);
    }

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BlockBed;
    }

    @Override
    public boolean preDeserialize(final MaterialSource materialSource, final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        if (isHeadPart(data)) {
            return false;
        }

        if (!isHeadPosValid(world, pos, rotation, data)) {
            return false;
        }

        return super.preDeserialize(materialSource, world, pos, rotation, data);
    }

    @Override
    public void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        if (!isHeadPosValid(world, pos, rotation, data)) {
            cancelDeserialization(world, pos, rotation, data);
            return;
        }

        super.deserialize(world, pos, rotation, data);
    }

    @Override
    protected void postDeserialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
        super.postDeserialize(world, pos, state, data);

        final IBlockState topState = state.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);

        world.setBlockState(pos.offset(state.getValue(BlockBed.FACING)), topState);
    }

    private boolean isHeadPart(final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return true;
        }

        return state.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD;
    }

    private boolean isHeadPosValid(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return false;
        }
        final EnumFacing facing = state.withRotation(rotation).getValue(BlockBed.FACING);
        final BlockPos headPos = pos.offset(facing);
        return world.isBlockLoaded(headPos) && world.getBlockState(headPos).getBlock().isReplaceable(world, headPos);
    }
}
