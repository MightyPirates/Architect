package li.cil.architect.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public final class BlockPosUtils {
    public static BlockPos clamp(final Vec3i pos, final AxisAlignedBB bounds) {
        return new BlockPos(MathHelper.clamp(pos.getX(), bounds.minX, bounds.maxX - 1),
                            MathHelper.clamp(pos.getY(), bounds.minY, bounds.maxY - 1),
                            MathHelper.clamp(pos.getZ(), bounds.minZ, bounds.maxZ - 1));
    }

    public static Vec3d toVec3d(final BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    private BlockPosUtils() {
    }
}
