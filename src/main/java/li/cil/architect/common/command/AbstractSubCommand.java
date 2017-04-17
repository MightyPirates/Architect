package li.cil.architect.common.command;

import li.cil.architect.common.Architect;
import li.cil.architect.common.config.Constants;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractSubCommand extends AbstractCommand {
    @Override
    public String getUsage(final ICommandSender sender) {
        return String.format(Constants.COMMAND_SUB_USAGE, getName());
    }

    @Nullable
    static ResourceLocation getLookedAtResourceLocation(final ICommandSender sender) throws CommandException {
        final IBlockState state = getLookedAtBlockState(sender);
        if (state == null) {
            return null;
        }

        return state.getBlock().getRegistryName();
    }

    @Nullable
    static NBTTagCompound getLookedAtTileEntityNBT(final ICommandSender sender) throws CommandException {
        final TileEntity tileEntity = getLookedAtTileEntity(sender);
        if (tileEntity == null) {
            return null;
        }

        final NBTTagCompound nbt = new NBTTagCompound();
        try {
            tileEntity.writeToNBT(nbt);
        } catch (final Throwable e) {
            Architect.getLog().warn("Failed getting tile entity NBT.", e);
            return null;
        }

        // Strip stuff written by TileEntity base class.
        nbt.removeTag("x");
        nbt.removeTag("y");
        nbt.removeTag("z");
        nbt.removeTag("id");

        return nbt;
    }

    @Nullable
    static TileEntity getLookedAtTileEntity(final ICommandSender sender) throws CommandException {
        final BlockPos pos = getLookedAtBlockPos(sender);
        if (pos == null) {
            return null;
        }

        return sender.getEntityWorld().getTileEntity(pos);
    }

    @Nullable
    static IBlockState getLookedAtBlockState(final ICommandSender sender) throws CommandException {
        final World world = sender.getEntityWorld();
        final BlockPos pos = getLookedAtBlockPos(sender);
        if (pos == null) {
            return null;
        }

        return world.getBlockState(pos).getActualState(world, pos);
    }

    @Nullable
    static BlockPos getLookedAtBlockPos(final ICommandSender sender) throws CommandException {
        final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
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
