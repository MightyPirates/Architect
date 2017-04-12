package li.cil.architect.common.network;

import li.cil.architect.api.API;
import li.cil.architect.client.network.handler.MessageHandlerClipboardClient;
import li.cil.architect.client.network.handler.MessageHandlerRequestClipboard;
import li.cil.architect.common.network.handler.MessageHandlerBlueprintRotate;
import li.cil.architect.common.network.handler.MessageHandlerBlueprintShift;
import li.cil.architect.common.network.handler.MessageHandlerClipboardServer;
import li.cil.architect.common.network.message.MessageBlueprintRotate;
import li.cil.architect.common.network.message.MessageBlueprintShift;
import li.cil.architect.common.network.message.MessageClipboard;
import li.cil.architect.common.network.message.MessageRequestBlueprintData;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public enum Network {
    INSTANCE;

    private SimpleNetworkWrapper wrapper;

    private enum Messages {
        BlueprintShift,
        BlueprintRotate,
        Clipboard,
        RequestBlueprintData
    }

    // --------------------------------------------------------------------- //

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(API.MOD_ID);

        wrapper.registerMessage(MessageHandlerBlueprintShift.class, MessageBlueprintShift.class, Messages.BlueprintShift.ordinal(), Side.SERVER);
        wrapper.registerMessage(MessageHandlerBlueprintRotate.class, MessageBlueprintRotate.class, Messages.BlueprintRotate.ordinal(), Side.SERVER);
        wrapper.registerMessage(MessageHandlerClipboardClient.class, MessageClipboard.class, Messages.Clipboard.ordinal(), Side.CLIENT);
        wrapper.registerMessage(MessageHandlerClipboardServer.class, MessageClipboard.class, Messages.Clipboard.ordinal(), Side.SERVER);
        wrapper.registerMessage(MessageHandlerRequestClipboard.class, MessageRequestBlueprintData.class, Messages.RequestBlueprintData.ordinal(), Side.CLIENT);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }
}
