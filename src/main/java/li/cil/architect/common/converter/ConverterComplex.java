package li.cil.architect.common.converter;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.api.prefab.converter.AbstractConverter;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Jasons;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public final class ConverterComplex extends AbstractConverter {
    private static final String TAG_NBT = "nbt";

    public ConverterComplex() {
        super(Constants.UUID_CONVERTER_TILE_ENTITY);
    }

    @Override
    public int getSortIndex(final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return SortIndex.SOLID_BLOCK;
        }

        return Jasons.getSortIndex(state.getBlock());
    }

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        final Block block = ConverterAPI.mapToBlock(state);
        return Jasons.isWhitelisted(block);
    }

    @Override
    protected void postSerialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
        super.postSerialize(world, pos, state, data);
        final Block block = ConverterAPI.mapToBlock(state);
        if (!Jasons.hasNbtFilter(block)) {
            return;
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return;
        }

        final NBTTagCompound nbt = new NBTTagCompound();
        tileEntity.writeToNBT(nbt);
        Jasons.filterNbt(block, nbt);
        data.setTag(TAG_NBT, nbt);
    }

    @Override
    protected void postDeserialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
        super.postDeserialize(world, pos, state, data);
        if (!data.hasKey(TAG_NBT, NBT.TAG_COMPOUND)) {
            return;
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return;
        }

        final Block block = ConverterAPI.mapToBlock(state);

        // Yes, also filter when deserializing, in case the filter changed in
        // the meantime, so we don't allow players with an old blueprint to
        // deserialize NBT they shouldn't be allowed to.
        final NBTTagCompound nbt = data.getCompoundTag(TAG_NBT);
        Jasons.filterNbt(block, nbt);

        // Merge the persisted values into the current state of the TE.
        final NBTTagCompound currentNbt = new NBTTagCompound();
        tileEntity.writeToNBT(currentNbt);
        Jasons.stripNbt(block, currentNbt);
        currentNbt.merge(nbt);
        tileEntity.readFromNBT(currentNbt);
    }
}
