package li.cil.architect.util;

import li.cil.architect.common.config.Settings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class PlayerUtils {
    public static EnumFacing getPrimaryFacing(final EntityPlayer player) {
        final Vec3d lookVec = player.getLookVec();
        final double absX = Math.abs(lookVec.xCoord);
        final double absY = Math.abs(lookVec.yCoord);
        final double absZ = Math.abs(lookVec.zCoord);

        if (absX > absY && absX > absZ) {
            if (lookVec.xCoord > 0) {
                return EnumFacing.EAST;
            } else {
                return EnumFacing.WEST;
            }
        } else if (absY > absZ) {
            if (lookVec.yCoord > 0) {
                return EnumFacing.UP;
            } else {
                return EnumFacing.DOWN;
            }
        } else {
            if (lookVec.zCoord > 0) {
                return EnumFacing.SOUTH;
            } else {
                return EnumFacing.NORTH;
            }
        }
    }

    public static BlockPos getLookAtPos(final EntityPlayer player) {
        final Vec3d lookVec = player.getLookVec();
        final Vec3d eyePos = player.getPositionEyes(1);
        return new BlockPos(eyePos.add(lookVec.scale(Settings.freeAimDistance)));
    }

    private PlayerUtils() {
    }
}
