package li.cil.architect.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class WorldUtils {
    public static boolean isReplaceable(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        return state.getBlock().isReplaceable(world, pos);
    }

    private WorldUtils() {
    }
}
