package li.cil.architect.common.integration.minecraft;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.api.converter.MaterialSource;
import li.cil.architect.api.prefab.converter.AbstractConverter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.UUID;

public abstract class AbstractMultiBlockConverter extends AbstractConverter {
    public AbstractMultiBlockConverter(final UUID uuid, final int sortIndex) {
        super(uuid, sortIndex);
    }

    public AbstractMultiBlockConverter(final UUID uuid) {
        super(uuid);
    }

    protected abstract boolean canSerialize(final IBlockState state);

    protected abstract boolean isSecondaryState(final IBlockState state);

    protected abstract IBlockState getSecondaryState(final IBlockState state);

    protected abstract BlockPos getSecondaryPos(final BlockPos pos, final IBlockState state);

    @Override
    public Iterable<ItemStack> getItemCosts(final NBTBase data) {
        if (isSecondaryPart(data)) {
            return Collections.emptyList();
        }
        return super.getItemCosts(data);
    }

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        return canSerialize(state) && ConverterAPI.mapToItem(state.getBlock()) != Items.AIR;
    }

    @Override
    public boolean preDeserialize(final MaterialSource materialSource, final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        if (isSecondaryPart(data)) {
            return false;
        }

        if (!isSecondaryPosValid(world, pos, rotation, data)) {
            return false;
        }

        return super.preDeserialize(materialSource, world, pos, rotation, data);
    }

    @Override
    public void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        if (!isSecondaryPosValid(world, pos, rotation, data)) {
            cancelDeserialization(world, pos, rotation, data);
            return;
        }

        super.deserialize(world, pos, rotation, data);
    }

    @Override
    protected void postDeserialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
        super.postDeserialize(world, pos, state, data);

        final IBlockState secondaryState = getSecondaryState(state);

        world.setBlockState(getSecondaryPos(pos, state), secondaryState);
    }

    private boolean isSecondaryPart(final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return true;
        }

        return isSecondaryState(state);
    }

    private boolean isSecondaryPosValid(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return false;
        }
        final BlockPos secondaryPos = getSecondaryPos(pos, state.withRotation(rotation));
        return world.isBlockLoaded(secondaryPos) && world.getBlockState(secondaryPos).getBlock().isReplaceable(world, secondaryPos);
    }
}
