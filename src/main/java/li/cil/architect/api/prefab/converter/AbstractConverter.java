package li.cil.architect.api.prefab.converter;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.api.converter.Converter;
import li.cil.architect.api.converter.MaterialSource;
import li.cil.architect.api.converter.SortIndex;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

/**
 * Base implementation of a converter.
 */
public abstract class AbstractConverter implements Converter {
    // --------------------------------------------------------------------- //
    // Computed data

    private final UUID uuid;
    private final int sortIndex;

    // NBT tag names.
    private static final String TAG_NAME = "name";
    private static final String TAG_METADATA = "meta";

    // --------------------------------------------------------------------- //

    protected AbstractConverter(final UUID uuid, final int sortIndex) {
        this.uuid = uuid;
        this.sortIndex = sortIndex;
    }

    protected AbstractConverter(final UUID uuid) {
        this(uuid, SortIndex.SOLID_BLOCK);
    }

    // --------------------------------------------------------------------- //

    protected abstract boolean canSerialize(final World world, final BlockPos pos, final IBlockState state);

    // --------------------------------------------------------------------- //
    // Converter

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Iterable<ItemStack> getItemCosts(final NBTBase data) {
        final ItemStack wantStack = getItemStack(data);
        if (wantStack.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singleton(wantStack);
    }

    @Override
    public Iterable<FluidStack> getFluidCosts(final NBTBase data) {
        return Collections.emptyList();
    }

    @Override
    public int getSortIndex(final NBTBase data) {
        return sortIndex;
    }

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        return ConverterAPI.mapToItem(state.getBlock()) != Items.AIR && canSerialize(world, pos, state);
    }

    @Override
    public NBTBase serialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        final Block block = ConverterAPI.mapToBlock(state);

        final ResourceLocation name = block.getRegistryName();
        assert name != null : "canSerialize implementation allowed invalid block";

        // When mapping blocks, only keep metadata if class stays the same,
        // otherwise we can't rely on the meta conversion to work correctly.
        final int metadata = block.getClass() == state.getBlock().getClass() ? block.getMetaFromState(state) : 0;

        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(TAG_NAME, name.toString());
        nbt.setByte(TAG_METADATA, (byte) metadata);

        postSerialize(world, pos, state, nbt);

        return nbt;
    }

    @Override
    public boolean preDeserialize(final MaterialSource materialSource, final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final ItemStack wantStack = getItemStack(data);
        if (wantStack.isEmpty()) {
            return true;
        }

        final ItemStack haveStack = materialSource.extractItem(wantStack);
        return !haveStack.isEmpty();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final NBTTagCompound nbt = (NBTTagCompound) data;
        final ResourceLocation name = new ResourceLocation(nbt.getString(TAG_NAME));
        final int metadata = nbt.getByte(TAG_METADATA) & 0xFF;

        final Block block = ForgeRegistries.BLOCKS.getValue(name);
        if (block == null) {
            // Block type does not exist in this Minecraft instance even though
            // an item exists? Weird. Drop what we consumed then.
            cancelDeserialization(world, pos, rotation, data);
            return;
        }

        final IBlockState state = block.getStateFromMeta(metadata).withRotation(rotation);

        world.setBlockState(pos, state);

        postDeserialize(world, pos, state, nbt);

        if (block.hasTileEntity(state)) {
            final TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity != null) {
                tileEntity.rotate(rotation);
            }
        }

        // Force a block update after deserializing non-static blocks to avoid
        // them hanging around in invalid states.
        switch (getSortIndex(data)) {
            case SortIndex.ATTACHED_BLOCK:
            case SortIndex.FALLING_BLOCK:
            case SortIndex.FLUID_BLOCK:
                world.neighborChanged(pos, world.getBlockState(pos).getBlock(), pos);
                break;
        }
    }

    @Override
    public void cancelDeserialization(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final ItemStack wantStack = getItemStack(data);
        if (!wantStack.isEmpty()) {
            InventoryHelper.spawnItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, wantStack);
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Allows hooking into the serialization process, adding additional NBT
     * data to the tag compound returned from the basic serialization.
     * <p>
     * Note that the tags <code>name</code> and <code>meta</code> will already
     * be set in the passed {@link NBTTagCompound}, and should usually not be
     * overwritten.
     *
     * @param world the world the block to serialize lives in.
     * @param pos   the position of the block to serialize.
     * @param state the block state of the block to serialize.
     * @param data  the tag generated from the serialization process so far.
     */
    protected void postSerialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
    }

    /**
     * Allows hooking into the deserialization process, applying further changes
     * to the deserialized block. This is called after the block has already
     * been created in the world.
     *
     * @param world the world the block is being deserialized into.
     * @param pos   the position the block is being deserialized at.
     * @param state the block state of the deserialized block.
     * @param data  the serialized representation of the block to
     */
    protected void postDeserialize(final World world, final BlockPos pos, final IBlockState state, final NBTTagCompound data) {
    }

    /**
     * Get the block state serialized in the specified data.
     *
     * @param data the serialized representation of the block.
     * @return the block state stored in the specified data.
     */
    @SuppressWarnings("deprecation")
    @Nullable
    protected IBlockState getBlockState(final NBTBase data) {
        final NBTTagCompound nbt = (NBTTagCompound) data;
        final ResourceLocation name = new ResourceLocation(nbt.getString(TAG_NAME));
        final int metadata = nbt.getByte(TAG_METADATA) & 0xFF;

        // Note: in practice this will never return null, but let's honor the
        // annotations in case someone decides this has to change somewhen...
        final Block block = ForgeRegistries.BLOCKS.getValue(name);
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        return block.getStateFromMeta(metadata);
    }

    /**
     * Get the item stack required as materials to deserialize the block
     * stored in the specified data.
     *
     * @param data the serialized representation of the block.
     * @return the material cost for the block.
     */
    protected ItemStack getItemStack(final NBTBase data) {
        final IBlockState state = getBlockState(data);
        if (state == null) {
            return ItemStack.EMPTY;
        }

        final Item item = ConverterAPI.mapToItem(state.getBlock());
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return getItemStack(item, state, data);
    }

    /**
     * Resolve an item to an actual item stack. Override this in case this is
     * not correct in your converter (e.g. dropped item is NBT dependent).
     *
     * @param item  the item to create an item stack for.
     * @param state the block state based on which to create the item.
     * @param data  the serialized representation of the block.
     * @return the item stack representing the costs of the block.
     */
    protected ItemStack getItemStack(final Item item, final IBlockState state, final NBTBase data) {
        return new ItemStack(item, 1, state.getBlock().damageDropped(state));
    }
}
