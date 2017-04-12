package li.cil.architect.client.network.handler;

import li.cil.architect.common.network.handler.AbstractMessageHandler;
import li.cil.architect.common.network.message.MessageClipboard;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerClipboardClient extends AbstractMessageHandler<MessageClipboard> {
    @Override
    protected void onMessageSynchronized(final MessageClipboard message, final MessageContext context) {
        final String value = message.getValue();
        GuiScreen.setClipboardString(value);
    }
}
