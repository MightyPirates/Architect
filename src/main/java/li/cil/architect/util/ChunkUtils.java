package li.cil.architect.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class ChunkUtils {
    public static long chunkPosToLong(final ChunkPos chunkPos) {
        final long x = chunkPos.chunkXPos & 0xFFFFFFFFL;
        final long z = chunkPos.chunkXPos & 0xFFFFFFFFL;
        return (x << 32) | z;
    }

    public static ChunkPos longToChunkPos(final long value) {
        return new ChunkPos((int) (value >> 32), (int) value);
    }

    public static short posToShort(final BlockPos pos) {
        final int x = pos.getX() & 0xF;
        final int y = pos.getY() & 0xFF;
        final int z = pos.getZ() & 0xF;
        return (short) ((z << 24) | (y << 8) | x);
    }

    public static BlockPos shortToPos(final ChunkPos chunkPos, final short value) {
        final int x = (value & 0x00F);
        final int y = (value & 0x0FF0) >>> 8;
        final int z = (value & 0xF000) >>> 24;
        return chunkPos.getBlock(x, y, z);
    }

    // --------------------------------------------------------------------- //

    private ChunkUtils() {
    }
}
