package li.cil.architect.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.BitSet;

public final class MessageJobDataResponse implements IMessage {
    private ChunkPos chunkPos;
    private byte[] data;

    public MessageJobDataResponse(final ChunkPos chunkPos, final byte[] data) {
        this.chunkPos = chunkPos;
        this.data = data;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageJobDataResponse() {
    }

    // --------------------------------------------------------------------- //

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public BitSet getData() {
        return BitSet.valueOf(data);
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final int x = buf.readInt();
        final int z = buf.readInt();
        chunkPos = new ChunkPos(x, z);
        final int dataSize = buf.readInt();
        data = new byte[dataSize];
        buf.readBytes(data);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(chunkPos.chunkXPos);
        buf.writeInt(chunkPos.chunkZPos);
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }
}
