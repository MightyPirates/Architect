package li.cil.architect.common.converter;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.api.prefab.converter.AbstractConverter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public abstract class AbstractConverterBase extends AbstractConverter {
    AbstractConverterBase(final UUID uuid, final int sortIndex) {
        super(uuid, sortIndex);
    }

    AbstractConverterBase(final UUID uuid) {
        super(uuid);
    }

    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        final Block block = ConverterAPI.mapToBlock(state);
        return !block.hasTileEntity(state);
    }

    // --------------------------------------------------------------------- //
    // Converter

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        final Item item = ConverterAPI.mapToItem(state.getBlock());
        return item != Items.AIR && canSerialize(world, pos, state);
    }
}
