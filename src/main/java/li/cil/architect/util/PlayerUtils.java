package li.cil.architect.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class PlayerUtils {
    private static final int MIN_AIM_DISTANCE = 4;
    private static final int MAX_AIM_DISTANCE = 10;

    private static float freeAimDistance = MIN_AIM_DISTANCE;

    /**
     * Change the distance at which the free-aim pointer is positioned.
     *
     * @param delta the amount by which to change the distance.
     */
    public static void changeFreeAimDistance(final float delta) {
        freeAimDistance = MathHelper.clamp(freeAimDistance + delta, MIN_AIM_DISTANCE, MAX_AIM_DISTANCE);
    }

    public static BlockPos getLookAtPos(final EntityPlayer player) {
        final Vec3d lookVec = player.getLookVec();
        final Vec3d eyePos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        return new BlockPos(eyePos.add(lookVec.scale(freeAimDistance)));
    }

    public static BlockPos getRaytrace(final EntityPlayer player) {
        final RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            boolean replaceable = player.world.getBlockState(hit.getBlockPos()).getBlock().isReplaceable(player.world, hit.getBlockPos());
            if (replaceable) {
                return hit.getBlockPos();
            }
            return hit.getBlockPos().offset(hit.sideHit);
        }
        return PlayerUtils.getLookAtPos(player);
    }

    @Nullable
    public static EnumFacing getSideHit(World world) {
        final RayTraceResult hit = Minecraft.getMinecraft().objectMouseOver;
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            boolean replaceable = world.getBlockState(hit.getBlockPos()).getBlock().isReplaceable(world, hit.getBlockPos());
            if (replaceable) {
                return EnumFacing.UP;
            }
            return hit.sideHit;
        }
        return null;
    }

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

    private PlayerUtils() {
    }
}
