package li.cil.architect.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class MessageClipboard implements IMessage {
    private String value;

    public MessageClipboard(final String value) {
        this.value = value;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageClipboard() {
    }

    // --------------------------------------------------------------------- //

    public String getValue() {
        return value;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer packet = new PacketBuffer(buf);
        value = packet.readString(Short.MAX_VALUE);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer packet = new PacketBuffer(buf);
        packet.writeString(value);
    }
}
