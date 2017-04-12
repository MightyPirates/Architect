package li.cil.architect.client.network.handler;

import com.google.common.base.Strings;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.handler.AbstractMessageHandler;
import li.cil.architect.common.network.message.MessageClipboard;
import li.cil.architect.common.network.message.MessageRequestBlueprintData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerRequestClipboard extends AbstractMessageHandler<MessageRequestBlueprintData> {
    @Override
    protected void onMessageSynchronized(final MessageRequestBlueprintData message, final MessageContext context) {
        final String value = GuiScreen.getClipboardString();
        if (Strings.isNullOrEmpty(value)) {
            return;
        }

        Network.INSTANCE.getWrapper().sendToServer(new MessageClipboard(value));
    }
}
