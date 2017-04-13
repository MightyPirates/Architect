package li.cil.architect.api.prefab.converter;

import li.cil.architect.api.converter.Converter;
import li.cil.architect.api.converter.MaterialSource;
import li.cil.architect.api.converter.SortIndex;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
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
    private static long lastPlaceSound;

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
    // Converter

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Iterable<ItemStack> getItemCosts(final NBTBase data) {
        final ItemStack wantStack = getItem(data);
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
    public NBTBase serialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        final Block block = getBlock(state);

        final ResourceLocation name = block.getRegistryName();
        final int metadata = block.getMetaFromState(state);

        assert name != null;

        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(TAG_NAME, name.toString());
        nbt.setByte(TAG_METADATA, (byte) metadata);

        postSerialize(world, pos, state, nbt);

        return nbt;
    }

    @Override
    public boolean preDeserialize(final MaterialSource materialSource, final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final ItemStack wantStack = getItem(data);
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
            return; // Block type does not exist in this Minecraft instance.
        }

        final IBlockState state = block.getStateFromMeta(metadata).withRotation(rotation);

        world.setBlockState(pos, state);

        postDeserialize(world, pos, state, nbt);

        if (world.getTotalWorldTime() > lastPlaceSound + 3) {
            lastPlaceSound = world.getTotalWorldTime();
            final SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, null);
            world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1f) / 2f, soundtype.getPitch() * 0.8f);
        }
    }

    @Override
    public void cancelDeserialization(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final ItemStack wantStack = getItem(data);
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
     * Allows hooking into block lookup logic. This is used in Architect to
     * apply block mappings for example (lit furnace to furnace, read from the
     * <code>block_mappings.json</code> file).
     *
     * @param state the state to get the block from.
     * @return the block, possibly mapped to an alias value.
     */
    protected Block getBlock(final IBlockState state) {
        return state.getBlock();
    }

    /**
     * Allows hooking into item lookup logic. This is used in Architect to
     * apply item mappings for example (redstone wire to redstone, read from the
     * <code>item_mappings.json</code> file).
     *
     * @param block the block to get the item representation for.
     * @return the item representing the block.
     */
    protected Item getItem(final Block block) {
        return Item.getItemFromBlock(block);
    }

    @Nullable
    protected Block getBlock(final NBTBase data) {
        final NBTTagCompound nbt = (NBTTagCompound) data;
        final ResourceLocation name = new ResourceLocation(nbt.getString(TAG_NAME));

        final Block block = ForgeRegistries.BLOCKS.getValue(name);
        return block == Blocks.AIR ? null : block;
    }

    protected ItemStack getItem(final NBTBase data) {
        final Block block = getBlock(data);
        if (block == null) {
            return ItemStack.EMPTY;
        }

        final Item item = getItem(block);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item);
    }
}
