package li.cil.architect.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.Rotation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class MessageBlueprintRotate implements IMessage {
    private Rotation rotation;

    public MessageBlueprintRotate(final Rotation rotation) {
        this.rotation = rotation;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageBlueprintRotate() {
    }

    // --------------------------------------------------------------------- //

    public Rotation getRotation() {
        return rotation;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        rotation = Rotation.values()[buf.readByte()];
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeByte(rotation.ordinal());
    }
}
