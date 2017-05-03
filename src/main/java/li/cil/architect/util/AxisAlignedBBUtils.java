package li.cil.architect.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class AxisAlignedBBUtils {
    public static BlockPos getCenter(final AxisAlignedBB bounds) {
        return new BlockPos(bounds.minX + (bounds.maxX - bounds.minX) * 0.5D, bounds.minY + (bounds.maxY - bounds.minY) * 0.5D, bounds.minZ + (bounds.maxZ - bounds.minZ) * 0.5D);
    }

    public static Vec3d getSize(final AxisAlignedBB bounds) {
        return new Vec3d(bounds.maxX - bounds.minX, bounds.maxY - bounds.minY, bounds.maxZ - bounds.minZ);
    }

    public static Vec3i getBlockSize(final AxisAlignedBB bounds) {
        return new Vec3i(bounds.maxX - bounds.minX, bounds.maxY - bounds.minY, bounds.maxZ - bounds.minZ);
    }

    public static Stream<BlockPos> getTouchedBlocks(final AxisAlignedBB bounds) {
        return StreamSupport.stream(new BlockPosSpliterator(bounds), false);
    }

    private static final class BlockPosSpliterator extends Spliterators.AbstractSpliterator<BlockPos> {
        private final AxisAlignedBB bounds;
        private BlockPos current;

        BlockPosSpliterator(final AxisAlignedBB bounds) {
            super(0, SIZED);
            this.bounds = bounds;
            current = new BlockPos(bounds.minX, bounds.minY, bounds.minZ);
        }

        @Override
        public boolean tryAdvance(final Consumer<? super BlockPos> action) {
            if (current != null) {
                final BlockPos result = current;
                if (current.getX() + 1 < bounds.maxX) {
                    current = new BlockPos(current.getX() + 1, current.getY(), current.getZ());
                } else if (current.getY() + 1 < bounds.maxY) {
                    current = new BlockPos(bounds.minX, current.getY() + 1, current.getZ());
                } else if (current.getZ() + 1 < bounds.maxZ) {
                    current = new BlockPos(bounds.minX, bounds.minY, current.getZ() + 1);
                } else {
                    current = null;
                }
                action.accept(result);
                return true;
            }
            return false;
        }
    }

    private AxisAlignedBBUtils() {
    }
}
