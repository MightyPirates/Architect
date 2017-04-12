package li.cil.architect.common.converter;

import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Jasons;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ConverterTileEntity extends AbstractConverterBase {
    public ConverterTileEntity() {
        super(Constants.UUID_CONVERTER_TILE_ENTITY);
    }

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        final Block block = getBlock(state);
        return getItem(block) != Items.AIR && Jasons.isWhitelisted(block);
    }
}
