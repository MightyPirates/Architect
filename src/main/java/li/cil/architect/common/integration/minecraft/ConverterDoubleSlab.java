package li.cil.architect.common.integration.minecraft;

import li.cil.architect.api.prefab.converter.AbstractConverter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoubleStoneSlab;
import net.minecraft.block.BlockDoubleStoneSlabNew;
import net.minecraft.block.BlockDoubleWoodSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConverterDoubleSlab extends AbstractConverter {
    ConverterDoubleSlab() {
        super(ProxyMinecraft.UUID_CONVERTER_MINECRAFT_DOUBLE_SLAB);
    }

    // --------------------------------------------------------------------- //
    // AbstractConverter

    @Override
    protected ItemStack getItemStack(final Item item, final IBlockState state, final NBTBase data) {
        final ItemStack stack = super.getItemStack(item, state, data);
        stack.setCount(2);
        return stack;
    }

    // --------------------------------------------------------------------- //
    // AbstractConverter

    @Override
    protected boolean canSerialize(final World world, final BlockPos pos, final IBlockState state) {
        final Block block = state.getBlock();
        return block instanceof BlockDoubleStoneSlab ||
               block instanceof BlockDoubleStoneSlabNew ||
               block instanceof BlockDoubleWoodSlab;
    }
}
