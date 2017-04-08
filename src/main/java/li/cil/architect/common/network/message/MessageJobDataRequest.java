package li.cil.architect.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public final class MessageJobDataRequest implements IMessage {
    private ChunkPos chunkPos;

    public MessageJobDataRequest(final ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageJobDataRequest() {
    }

    // --------------------------------------------------------------------- //

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final int x = buf.readInt();
        final int z = buf.readInt();
        chunkPos = new ChunkPos(x, z);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(chunkPos.chunkXPos);
        buf.writeInt(chunkPos.chunkZPos);
    }
}
