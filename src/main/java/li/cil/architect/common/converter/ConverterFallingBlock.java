package li.cil.architect.common.converter;

import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.common.config.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;

public final class ConverterFallingBlock extends AbstractConverterBase {
    public ConverterFallingBlock() {
        super(Constants.UUID_CONVERTER_FALLING_BLOCKS, SortIndex.FALLING_BLOCK);
    }

    @Override
    protected boolean canSerialize(final IBlockState state) {
        final Block block = state.getBlock();
        return block instanceof BlockFalling && super.canSerialize(state);
    }
}
