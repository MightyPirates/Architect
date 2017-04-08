package li.cil.architect.common.api;

import li.cil.architect.api.blueprint.Converter;
import li.cil.architect.api.detail.BlueprintAPI;
import li.cil.architect.common.Architect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
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
    public int getSortIndex(final NBTTagCompound data) {
        final Converter converter = findConverter(data);
        if (converter == null) {
            return 0;
        }
        return converter.getSortIndex(data);
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
    public Iterable<ItemStack> getMissingMaterials(final IItemHandler materials, final World world, final BlockPos pos, final NBTTagCompound data) {
        if (!isValidPosition(world, pos)) {
            return Collections.emptyList();
        }

        final Converter converter = findConverter(data);
        if (converter == null) {
            return Collections.emptyList();
        }

        return converter.getMissingMaterials(materials, data.getTag(TAG_DATA));
    }

    @Override
    public boolean preDeserialize(final IItemHandler materials, final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound data) {
        if (!isValidPosition(world, pos)) {
            return false;
        }

        final Converter converter = findConverter(data);
        if (converter == null) {
            Architect.getLog().warn("Trying to deserialize block that was serialized with a converter that is not present in the current installation. Ignoring.");
            return false;
        }

        return converter.preDeserialize(materials, world, pos, rotation, data.getTag(TAG_DATA));
    }

    @Override
    public void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound data) {
        if (!isValidPosition(world, pos)) {
            return;
        }

        final Converter converter = findConverter(data);
        if (converter == null) {
            Architect.getLog().warn("Trying to deserialize block that was serialized with a converter that is not present in the current installation. Ignoring.");
            return;
        }

        converter.deserialize(world, pos, rotation, data.getTag(TAG_DATA));
    }

    private boolean isValidPosition(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        return state.getBlock().isReplaceable(world, pos);
    }

    @Nullable
    private Converter findConverter(final NBTTagCompound data) {
        final UUID uuid = new UUID(data.getLong(TAG_CONVERTER_MSB), data.getLong(TAG_CONVERTER_LSB));
        return uuidToConverter.get(uuid);
    }

    @Nullable
    private Converter findConverter(final World world, final BlockPos pos) {
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
