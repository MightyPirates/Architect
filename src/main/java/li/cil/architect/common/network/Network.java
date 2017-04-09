package li.cil.architect.common.network;

import li.cil.architect.api.API;
import li.cil.architect.common.network.handler.MessageHandlerBlueprintRotate;
import li.cil.architect.common.network.handler.MessageHandlerBlueprintShift;
import li.cil.architect.common.network.message.MessageBlueprintRotate;
import li.cil.architect.common.network.message.MessageBlueprintShift;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public enum Network {
    INSTANCE;

    private SimpleNetworkWrapper wrapper;

    private enum Messages {
        BlueprintShift,
        BlueprintRotate
    }

    // --------------------------------------------------------------------- //

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(API.MOD_ID);

        wrapper.registerMessage(MessageHandlerBlueprintShift.class, MessageBlueprintShift.class, Messages.BlueprintShift.ordinal(), Side.SERVER);
        wrapper.registerMessage(MessageHandlerBlueprintRotate.class, MessageBlueprintRotate.class, Messages.BlueprintRotate.ordinal(), Side.SERVER);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }
}
