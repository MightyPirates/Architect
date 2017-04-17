package li.cil.architect.common.converter;

import li.cil.architect.api.prefab.converter.AbstractConverter;
import li.cil.architect.common.config.Constants;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ConverterSimpleBlock extends AbstractConverter {
    public ConverterSimpleBlock() {
        super(Constants.UUID_CONVERTER_SIMPLE_BLOCKS);
    }

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        return !state.getBlock().hasTileEntity(state);
    }
}
