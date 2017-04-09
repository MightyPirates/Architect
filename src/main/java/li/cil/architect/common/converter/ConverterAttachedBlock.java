package li.cil.architect.common.converter;

import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ConverterAttachedBlock extends AbstractConverterBase {
    public ConverterAttachedBlock() {
        super(Constants.UUID_CONVERTER_ATTACHED_BLOCKS, SortIndex.ATTACHED_BLOCK);
    }

    @Override
    protected boolean canSerialize(final IBlockState state) {
        final Block block = state.getBlock();
        return Settings.isAttachedBlock(block) && super.canSerialize(state);
    }

    @Override
    public void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        super.deserialize(world, pos, rotation, data);

        world.neighborChanged(pos, world.getBlockState(pos).getBlock(), pos);
    }
}
