package li.cil.architect.common.network.handler;

import li.cil.architect.common.blueprint.JobManager;
import li.cil.architect.common.network.message.MessageJobDataRequest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerJobDataRequest extends AbstractMessageHandler<MessageJobDataRequest> {
    @Override
    protected void onMessageSynchronized(final MessageJobDataRequest message, final MessageContext context) {
        final EntityPlayerMP player = context.getServerHandler().player;
        JobManager.INSTANCE.sendJobDataToClient(message.getChunkPos(), player);
    }
}
