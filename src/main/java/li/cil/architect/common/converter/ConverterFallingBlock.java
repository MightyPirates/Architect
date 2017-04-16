package li.cil.architect.common.converter;

import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.api.prefab.converter.AbstractConverter;
import li.cil.architect.common.config.Constants;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ConverterFallingBlock extends AbstractConverter {
    public ConverterFallingBlock() {
        super(Constants.UUID_CONVERTER_FALLING_BLOCKS);
    }

    @Override
    public int getSortIndex(final NBTBase data) {
        return SortIndex.FALLING_BLOCK;
    }

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        return state.getBlock() instanceof BlockFalling && !state.getBlock().hasTileEntity(state);
    }
}
