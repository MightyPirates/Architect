package li.cil.architect.common.integration.minecraft;

import li.cil.architect.api.converter.MaterialSource;
import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.api.prefab.converter.AbstractConverter;
import li.cil.architect.common.config.Constants;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;

public class ConverterDoor extends AbstractConverter {
    public ConverterDoor() {
        super(Constants.UUID_CONVERTER_MINECRAFT_DOOR);
    }

    @Override
    public int getSortIndex(final NBTBase data) {
        return SortIndex.ATTACHED_BLOCK;
    }

    @Override
    public Iterable<ItemStack> getItemCosts(final NBTBase data) {
        if (isTopPart(data)) {
            return Collections.emptyList();
        }
        return super.getItemCosts(data);
    }

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BlockDoor;
    }

    @Override
    public boolean preDeserialize(final MaterialSource materialSource, final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        if (isTopPart(data)) {
            return false;
        }

        if (!isTopPosValid(world, pos)) {
            return false;
        }

        return super.preDeserialize(materialSource, world, pos, rotation, data);
    }

    @Override
    public void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        if (!isTopPosValid(world, pos)) {
            cancelDeserialization(world, pos, rotation, data);
            return;
        }

        super.deserialize(world, pos, rotation, data);
    }

    @Override
    protected void postDeserialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
        super.postDeserialize(world, pos, state, data);

        final IBlockState topState = state.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER);

        world.setBlockState(pos.up(), topState);
    }

    private boolean isTopPart(final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return true;
        }

        return state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER;
    }

    private boolean isTopPosValid(final World world, final BlockPos pos) {
        final BlockPos topPos = pos.up();
        return world.isBlockLoaded(topPos) && world.getBlockState(topPos).getBlock().isReplaceable(world, topPos);
    }
}
