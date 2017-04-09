package li.cil.architect.common.blueprint;

import li.cil.architect.api.blueprint.ItemSource;
import li.cil.architect.api.prefab.blueprint.AbstractConverter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collections;
import java.util.UUID;

public final class ConverterSimpleBlock extends AbstractConverter {
    private static final String TAG_NAME = "name";
    private static final String TAG_METADATA = "meta";

    public ConverterSimpleBlock(final UUID uuid) {
        super(uuid);
    }

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        return Item.getItemFromBlock(block) != Items.AIR && block.getRegistryName() != null && !block.hasTileEntity(state);
    }

    @Override
    public NBTBase serialize(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();

        final ResourceLocation name = block.getRegistryName();
        final int metadata = block.getMetaFromState(state);

        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(TAG_NAME, name.toString());
        nbt.setByte(TAG_METADATA, (byte) metadata);
        return nbt;
    }

    @Override
    public Iterable<ItemStack> getMaterialCosts(final NBTBase data) {
        final ItemStack wantStack = getItem(data);
        if (wantStack.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singleton(wantStack);
    }

    @Override
    public boolean preDeserialize(final ItemSource itemSource, final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final ItemStack wantStack = getItem(data);
        if (wantStack.isEmpty()) {
            return true;
        }

        final ItemStack haveStack = itemSource.extract(wantStack);
        return !haveStack.isEmpty();
    }

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
    }

    @Override
    public void cancelDeserialization(final World world, final BlockPos pos, final Rotation rotation, final NBTBase data) {
        final ItemStack wantStack = getItem(data);
        if (!wantStack.isEmpty()) {
            InventoryHelper.spawnItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, wantStack);
        }
    }

    private static ItemStack getItem(final NBTBase data) {
        final NBTTagCompound nbt = (NBTTagCompound) data;
        final ResourceLocation name = new ResourceLocation(nbt.getString(TAG_NAME));

        final Block block = ForgeRegistries.BLOCKS.getValue(name);
        if (block == null) {
            return ItemStack.EMPTY;
        }

        final Item item = Item.getItemFromBlock(block);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item);
    }
}
