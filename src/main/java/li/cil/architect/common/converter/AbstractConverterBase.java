package li.cil.architect.common.converter;

import li.cil.architect.api.prefab.converter.AbstractConverter;
import li.cil.architect.common.config.Jasons;
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
        final Block block = getBlock(state);
        return getItem(block) != Items.AIR && !block.hasTileEntity(state);
    }

    // --------------------------------------------------------------------- //
    // Converter

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        final Block block = getBlock(state);
        return !Jasons.isBlacklisted(block) && canSerialize(world, pos, state);
    }

    // --------------------------------------------------------------------- //
    // AbstractConverter

    @Override
    protected Block getBlock(final IBlockState state) {
        return Jasons.mapBlockToBlock(super.getBlock(state));
    }

    @Override
    protected Item getItem(final Block block) {
        return Jasons.mapBlockToItem(block);
    }
}
