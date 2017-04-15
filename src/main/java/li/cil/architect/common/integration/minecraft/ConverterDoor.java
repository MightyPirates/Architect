package li.cil.architect.common.integration.minecraft;

import li.cil.architect.api.converter.SortIndex;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class ConverterDoor extends AbstractMultiBlockConverter {
    ConverterDoor() {
        super(ProxyMinecraft.UUID_CONVERTER_MINECRAFT_DOOR, SortIndex.ATTACHED_BLOCK);
    }

    // --------------------------------------------------------------------- //
    // AbstractMultiBlockConverter

    @Override
    protected boolean canSerialize(final IBlockState state) {
        return state.getBlock() instanceof BlockDoor;
    }

    @Override
    protected boolean isSecondaryState(final IBlockState state) {
        return state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER;
    }

    @Override
    protected IBlockState getSecondaryState(final IBlockState state) {
        return state.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER);
    }

    @Override
    protected BlockPos getSecondaryPos(final BlockPos pos, final IBlockState state) {
        return pos.up();
    }
}
