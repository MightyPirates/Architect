package li.cil.architect.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class MessageBlueprintData implements IMessage {
    private String name;
    private EnumDyeColor color;

    public MessageBlueprintData(final String name, final EnumDyeColor color) {
        this.name = name;
        this.color = color;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageBlueprintData() {
    }

    // --------------------------------------------------------------------- //

    public String getName() {
        return name;
    }

    public EnumDyeColor getColor() {
        return color;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer packet = new PacketBuffer(buf);
        name = packet.readString(24);
        color = packet.readEnumValue(EnumDyeColor.class);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer packet = new PacketBuffer(buf);
        packet.writeString(name);
        packet.writeEnumValue(color);
    }
}
