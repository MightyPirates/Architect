package li.cil.architect.common.command;

import li.cil.architect.common.config.Constants;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractSubCommand extends CommandBase {
    @Override
    public String getUsage(final ICommandSender sender) {
        return String.format(Constants.SUBCOMMAND_USAGE, getName());
    }

    @Nullable
    static ResourceLocation getLookedAtResourceLocation(final ICommandSender sender) {
        final IBlockState state = getLookedAtBlockState(sender);
        if (state == null) {
            return null;
        }

        return state.getBlock().getRegistryName();
    }

    @Nullable
    private static IBlockState getLookedAtBlockState(final ICommandSender sender) {
        final BlockPos pos = getLookedAtBlockPos(sender);
        if (pos == null) {
            return null;
        }

        return sender.getEntityWorld().getBlockState(pos);
    }

    @Nullable
    private static BlockPos getLookedAtBlockPos(final ICommandSender sender) {
        final Entity entity = sender.getCommandSenderEntity();
        if (!(entity instanceof EntityPlayer)) {
            return null;
        }

        final EntityPlayer player = (EntityPlayer) entity;
        final World world = player.getEntityWorld();
        final Vec3d origin = player.getPositionEyes(1);
        final Vec3d lookVec = player.getLookVec();
        final float blockReachDistance = player.isCreative() ? 5 : 4.5f;
        final Vec3d lookAt = origin.add(lookVec.scale(blockReachDistance));
        final RayTraceResult hit = world.rayTraceBlocks(origin, lookAt);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return null;
        }

        final BlockPos hitPos = hit.getBlockPos();
        if (!world.isBlockLoaded(hitPos)) {
            return null;
        }

        return hitPos;
    }
}
