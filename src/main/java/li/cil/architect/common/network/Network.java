package li.cil.architect.common.network;

import li.cil.architect.api.API;
import li.cil.architect.client.network.handler.MessageHandlerJobDataResponse;
import li.cil.architect.common.network.handler.MessageHandlerBlueprintShift;
import li.cil.architect.common.network.handler.MessageHandlerJobDataRequest;
import li.cil.architect.common.network.message.MessageBlueprintShift;
import li.cil.architect.common.network.message.MessageJobDataRequest;
import li.cil.architect.common.network.message.MessageJobDataResponse;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Central networking hub for TIS-3D.
 * <p>
 * Aside from managing the mod's channel this also has facilities for throttling
 * package throughput to avoid overloading the network when a large number of
 * casings are active and nearby players. Throttling is applied to particle
 * effect emission and module packets where possible.
 */
public final class Network {
    public static final Network INSTANCE = new Network();

    public static final int RANGE_HIGH = 48;
    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;

    private static SimpleNetworkWrapper wrapper;

    private enum Messages {
        BlueprintShift,
        JobDataRequest,
        JobDataResponse,
    }

    // --------------------------------------------------------------------- //

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(API.MOD_ID);

        wrapper.registerMessage(MessageHandlerBlueprintShift.class, MessageBlueprintShift.class, Messages.BlueprintShift.ordinal(), Side.SERVER);
        wrapper.registerMessage(MessageHandlerJobDataRequest.class, MessageJobDataRequest.class, Messages.JobDataRequest.ordinal(), Side.SERVER);
        wrapper.registerMessage(MessageHandlerJobDataResponse.class, MessageJobDataResponse.class, Messages.JobDataResponse.ordinal(), Side.CLIENT);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }

    // --------------------------------------------------------------------- //

    public static NetworkRegistry.TargetPoint getTargetPoint(final World world, final double x, final double y, final double z, final int range) {
        return new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, range);
    }

    public static NetworkRegistry.TargetPoint getTargetPoint(final World world, final BlockPos position, final int range) {
        return getTargetPoint(world, position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5, range);
    }

    public static NetworkRegistry.TargetPoint getTargetPoint(final TileEntity tileEntity, final int range) {
        return getTargetPoint(tileEntity.getWorld(), tileEntity.getPos(), range);
    }

    // --------------------------------------------------------------------- //

    /**
     * Check if there are any players nearby the specified target point.
     * <p>
     * Used to determine whether a packet will actually be sent to any
     * clients.
     *
     * @param target the target point to check for.
     * @return <tt>true</tt> if there are nearby players; <tt>false</tt> otherwise.
     */
    private static boolean areAnyPlayersNear(final NetworkRegistry.TargetPoint target) {
        for (final EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            if (player.dimension == target.dimension) {
                final double dx = target.x - player.posX;
                final double dy = target.y - player.posY;
                final double dz = target.z - player.posZ;

                if (dx * dx + dy * dy + dz * dz < target.range * target.range) {
                    final NetworkDispatcher dispatcher = player.connection.netManager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
                    if (dispatcher != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // --------------------------------------------------------------------- //

    private Network() {
    }
}
