package li.cil.architect.common.integration.minecraft;

import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.api.prefab.converter.AbstractMultiBlockConverter;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConverterDoublePlant extends AbstractMultiBlockConverter {
    ConverterDoublePlant() {
        super(ProxyMinecraft.UUID_CONVERTER_MINECRAFT_DOUBLE_PLANT, SortIndex.ATTACHED_BLOCK);
    }

    // --------------------------------------------------------------------- //
    // AbstractConverter

    @Override
    protected ItemStack getItemStack(final Item item, final IBlockState state, final NBTBase data) {
        final BlockDoublePlant.EnumPlantType type = state.getValue(BlockDoublePlant.VARIANT);
        if (type == BlockDoublePlant.EnumPlantType.FERN) {
            return new ItemStack(Blocks.TALLGRASS, 2, BlockTallGrass.EnumType.FERN.getMeta());
        }
        if (type == BlockDoublePlant.EnumPlantType.GRASS) {
            return new ItemStack(Blocks.TALLGRASS, 2, BlockTallGrass.EnumType.GRASS.getMeta());
        }
        return super.getItemStack(item, state, data);
    }

    // --------------------------------------------------------------------- //
    // AbstractMultiBlockConverter

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        return state.getBlock() instanceof BlockDoublePlant;
    }

    @Override
    protected boolean isSecondaryState(final IBlockState state) {
        return state.getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.UPPER;
    }

    @Override
    protected IBlockState getSecondaryState(final IBlockState state) {
        return state.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);
    }

    @Override
    protected BlockPos getSecondaryPos(final BlockPos pos, final IBlockState state) {
        return pos.up();
    }
}
