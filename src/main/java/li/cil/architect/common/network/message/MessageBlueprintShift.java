package li.cil.architect.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class MessageBlueprintShift implements IMessage {
    private EnumFacing facing;

    public MessageBlueprintShift(final EnumFacing shift) {
        this.facing = shift;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageBlueprintShift() {
    }

    // --------------------------------------------------------------------- //

    public EnumFacing getFacing() {
        return facing;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        facing = EnumFacing.VALUES[buf.readByte()];
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeByte(facing.getIndex());
    }
}
