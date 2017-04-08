package li.cil.architect.common.blueprint;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageJobDataRequest;
import li.cil.architect.util.ChunkUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.BitSet;
import java.util.function.Consumer;

public enum JobManagerClient {
    INSTANCE;

    private final TLongObjectMap<ChunkJobData> chunks = new TLongObjectHashMap<>();

    public void setJobData(final ChunkPos chunkPos, final BitSet data) {
        final ChunkJobData chunkData = chunks.get(ChunkUtils.chunkPosToLong(chunkPos));
        if (chunkData != null) {
            chunkData.blocks.clear();
            chunkData.blocks.or(data);
        }
    }

    public void setJobsDirty() {
        chunks.forEachValue(data -> {
            data.setDirty();
            return true;
        });
    }

    public void forEachJob(final Consumer<BlockPos> consumer) {
        synchronized (chunks) {
            for (final TLongObjectIterator<ChunkJobData> iterator = chunks.iterator(); iterator.hasNext(); ) {
                iterator.advance();
                final ChunkPos chunkPos = ChunkUtils.longToChunkPos(iterator.key());
                final ChunkJobData data = iterator.value();
                for (int index = data.blocks.nextSetBit(0); index >= 0; index = data.blocks.nextSetBit(index + 1)) {
                    final BlockPos pos = ChunkUtils.shortToPos(chunkPos, (short) index);
                    consumer.accept(pos);

                    assert index != Integer.MAX_VALUE;
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        final EntityPlayer player = Minecraft.getMinecraft().player;
        final int playerChunkX = player.chunkCoordX;
        final int playerChunkZ = player.chunkCoordZ;

        synchronized (chunks) {
            // Remove dead entries (too far away from the player).
            chunks.retainEntries((key, data) -> {
                final ChunkPos chunkPos = ChunkUtils.longToChunkPos(key);
                return areChunksAdjacent(playerChunkX, playerChunkZ, chunkPos.chunkXPos, chunkPos.chunkZPos);
            });

            for (int x = playerChunkX - 1; x <= playerChunkX + 1; x++) {
                for (int z = playerChunkZ - 1; z <= playerChunkZ + 1; z++) {
                    final long key = ChunkUtils.chunkPosToLong(new ChunkPos(x, z));
                    if (!chunks.containsKey(key)) {
                        chunks.put(key, new ChunkJobData());
                    }
                }
            }
        }

        final TLongObjectIterator<ChunkJobData> iterator = chunks.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            if (iterator.value().isDirty()) {
                Network.INSTANCE.getWrapper().sendToServer(new MessageJobDataRequest(ChunkUtils.longToChunkPos(iterator.key())));
            }
        }
    }

    private boolean areChunksAdjacent(final int chunkX0, final int chunkZ0, final int chunkX1, final int chunkZ1) {
        return (chunkX1 == chunkX0 - 1 || chunkX1 == chunkX0 || chunkX1 == chunkX0 + 1) &&
               (chunkZ1 == chunkZ0 - 1 || chunkZ1 == chunkZ0 || chunkZ1 == chunkZ0 + 1);
    }

    private static final class ChunkJobData {
        private static final int REFRESH_INTERVAL = 1000;

        private final BitSet blocks = new BitSet(16 * 16 * 256);
        private long lastUpdate;

        boolean isDirty() {
            return (System.currentTimeMillis() - lastUpdate) > REFRESH_INTERVAL;
        }

        void setDirty() {
            lastUpdate = 0;
        }
    }
}
