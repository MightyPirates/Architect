package li.cil.architect.common.integration.minecraft;

import li.cil.architect.api.prefab.converter.AbstractMultiBlockConverter;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConverterBed extends AbstractMultiBlockConverter {
    ConverterBed() {
        super(ProxyMinecraft.UUID_CONVERTER_MINECRAFT_BED);
    }

    // --------------------------------------------------------------------- //
    // AbstractConverter

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        return state.getBlock() instanceof BlockBed;
    }

    // --------------------------------------------------------------------- //
    // AbstractMultiBlockConverter

    @Override
    protected boolean isSecondaryState(final IBlockState state) {
        return state.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD;
    }

    @Override
    protected IBlockState getSecondaryState(final IBlockState state) {
        return state.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);
    }

    @Override
    protected BlockPos getSecondaryPos(final BlockPos pos, final IBlockState state) {
        final EnumFacing facing = state.getValue(BlockBed.FACING);
        return pos.offset(facing);
    }
}
