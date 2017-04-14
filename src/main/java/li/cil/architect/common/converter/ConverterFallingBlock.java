package li.cil.architect.common.converter;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.common.config.Constants;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ConverterFallingBlock extends AbstractConverterBase {
    public ConverterFallingBlock() {
        super(Constants.UUID_CONVERTER_FALLING_BLOCKS, SortIndex.FALLING_BLOCK);
    }

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        return ConverterAPI.mapToBlock(state) instanceof BlockFalling && super.canSerialize(world, pos, state);
    }

    @Override
    public void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        super.deserialize(world, pos, rotation, data);

        world.neighborChanged(pos, world.getBlockState(pos).getBlock(), pos);
    }
}
