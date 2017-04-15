package li.cil.architect.common.integration.minecraft;

import li.cil.architect.api.prefab.converter.AbstractMultiBlockConverter;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConverterDoor extends AbstractMultiBlockConverter {
    ConverterDoor() {
        super(ProxyMinecraft.UUID_CONVERTER_MINECRAFT_DOOR);
    }

    // --------------------------------------------------------------------- //
    // AbstractConverter

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        return state.getBlock() instanceof BlockDoor;
    }

    // --------------------------------------------------------------------- //
    // AbstractMultiBlockConverter

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
