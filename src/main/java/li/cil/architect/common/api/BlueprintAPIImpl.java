package li.cil.architect.common.api;

import li.cil.architect.api.blueprint.Converter;
import li.cil.architect.api.detail.BlueprintAPI;
import li.cil.architect.common.Architect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BlueprintAPIImpl implements BlueprintAPI {
    private static final String TAG_CONVERTER_LSB = "converterLSB";
    private static final String TAG_CONVERTER_MSB = "converterMSB";
    private static final String TAG_DATA = "data";

    private final List<Converter> converters = new ArrayList<>();
    private final Map<UUID, Converter> uuidToConverter = new HashMap<>();

    @Override
    public void addConverter(final Converter converter) {
        final UUID uuid = converter.getUUID();
        if (!uuidToConverter.containsKey(uuid)) {
            uuidToConverter.put(uuid, converter);
            converters.add(converter);
        }
    }

    @Override
    public boolean canSerialize(final World world, final BlockPos pos) {
        return findConverter(world, pos) != null;
    }

    @Nullable
    @Override
    public NBTTagCompound serialize(final World world, final BlockPos pos) {
        final Converter converter = findConverter(world, pos);
        if (converter == null) {
            return null; // No converter for this block, ignore it.
        }
        final UUID uuid = converter.getUUID();
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong(TAG_CONVERTER_LSB, uuid.getLeastSignificantBits());
        nbt.setLong(TAG_CONVERTER_MSB, uuid.getMostSignificantBits());
        nbt.setTag(TAG_DATA, converter.serialize(world, pos));
        return nbt;
    }

    @Override
    public boolean canDeserialize(final World world, final BlockPos pos, final NBTTagCompound nbt) {
        final IBlockState state = world.getBlockState(pos);
        if (!state.getBlock().isReplaceable(world, pos)) {
            return false;
        }

        final UUID uuid = new UUID(nbt.getLong(TAG_CONVERTER_MSB), nbt.getLong(TAG_CONVERTER_LSB));
        final Converter converter = uuidToConverter.get(uuid);
        if (converter == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deserialize(final IItemHandler materials, final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound nbt) {
        final IBlockState state = world.getBlockState(pos);
        if (!state.getBlock().isReplaceable(world, pos)) {
            return false;
        }

        final UUID uuid = new UUID(nbt.getLong(TAG_CONVERTER_MSB), nbt.getLong(TAG_CONVERTER_LSB));
        final Converter converter = uuidToConverter.get(uuid);
        if (converter == null) {
            Architect.getLog().warn("Trying to deserialize block that was serialized with a converter that is not present in the current installation. Ignoring.");
            return true; // Can never succeed, don't try again.
        }
        return converter.deserialize(materials, world, pos, rotation, nbt.getTag(TAG_DATA));
    }

    @Nullable
    private synchronized Converter findConverter(final World world, final BlockPos pos) {
        for (int i = 0; i < converters.size(); i++) {
            final Converter converter = converters.get(i);
            if (converter.canSerialize(world, pos)) {
                // Bring to front, assuming it'll be used again sooner than
                // others, potentially speeding up future look-ups.
                Collections.swap(converters, i, 0);
                return converter;
            }
        }
        return null;
    }
}
