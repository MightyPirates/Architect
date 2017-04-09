package li.cil.architect.common.api;

import li.cil.architect.api.converter.Converter;
import li.cil.architect.api.converter.MaterialSource;
import li.cil.architect.api.detail.ConverterAPI;
import li.cil.architect.common.Architect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ConverterAPIImpl implements ConverterAPI {
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
    public Iterable<ItemStack> getItemCosts(final NBTTagCompound data) {
        final Converter converter = findConverter(data);
        if (converter == null) {
            return Collections.emptyList();
        }

        return converter.getItemCosts(data.getTag(TAG_DATA));
    }

    @Override
    public Iterable<FluidStack> getFluidCosts(final NBTTagCompound data) {
        final Converter converter = findConverter(data);
        if (converter == null) {
            return Collections.emptyList();
        }

        return converter.getFluidCosts(data.getTag(TAG_DATA));
    }

    @Override
    public boolean preDeserialize(final MaterialSource materialSource, final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound data) {
        if (!isValidPosition(world, pos)) {
            return false;
        }

        final Converter converter = findConverter(data);
        if (converter == null) {
            Architect.getLog().warn("Trying to deserialize block that was serialized with a converter that is not present in the current installation. Ignoring.");
            return false;
        }

        return converter.preDeserialize(materialSource, world, pos, rotation, data.getTag(TAG_DATA));
    }

    @Override
    public void deserialize(final World world, final BlockPos pos, final Rotation rotation, final NBTTagCompound data) {
        final Converter converter = findConverter(data);
        if (converter == null) {
            Architect.getLog().warn("Trying to deserialize block that was serialized with a converter that is not present in the current installation. Ignoring.");
            return;
        }

        if (!isValidPosition(world, pos)) {
            converter.cancelDeserialization(world, pos, rotation, data.getTag(TAG_DATA));
        } else {
            converter.deserialize(world, pos, rotation, data.getTag(TAG_DATA));
        }
    }

    private static boolean isValidPosition(final World world, final BlockPos pos) {
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
        for (int i = converters.size() - 1; i >= 0; i--) {
            final Converter converter = converters.get(i);
            if (converter.canSerialize(world, pos)) {
                return converter;
            }
        }
        return null;
    }
}
