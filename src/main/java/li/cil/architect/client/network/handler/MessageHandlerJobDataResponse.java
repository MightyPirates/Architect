package li.cil.architect.client.network.handler;

import li.cil.architect.common.blueprint.JobManagerClient;
import li.cil.architect.common.network.handler.AbstractMessageHandler;
import li.cil.architect.common.network.message.MessageJobDataResponse;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerJobDataResponse extends AbstractMessageHandler<MessageJobDataResponse> {
    @Override
    protected void onMessageSynchronized(final MessageJobDataResponse message, final MessageContext context) {
        JobManagerClient.INSTANCE.setJobData(message.getChunkPos(), message.getData());
    }
}
