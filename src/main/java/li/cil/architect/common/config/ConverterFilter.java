package li.cil.architect.common.config;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public final class ConverterFilter {
    private final Map<String, Object> nbtFilter;
    private int sortIndex;

    // --------------------------------------------------------------------- //

    public ConverterFilter(final Map<String, Object> nbtFilter, final int sortIndex) {
        this.sortIndex = sortIndex;
        this.nbtFilter = nbtFilter;
    }

    public ConverterFilter(final int sortIndex) {
        this(Collections.emptyMap(), sortIndex);
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(final int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public Map<String, Object> getNbtFilter() {
        return nbtFilter;
    }

    public void filter(final NBTTagCompound nbt) {
        filter(nbt, this.nbtFilter);
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("unchecked")
    private static void filter(final NBTTagCompound nbt, final Map<String, Object> filter) {
        for (final Iterator<String> iterator = nbt.getKeySet().iterator(); iterator.hasNext(); ) {
            final String key = iterator.next();
            if (filter.containsKey(key)) {
                final NBTBase value = nbt.getTag(key);
                final Object filterValue = filter.get(key);
                if (filterValue instanceof Map && value instanceof NBTTagCompound) {
                    filter((NBTTagCompound) value, (Map<String, Object>) filterValue);
                }
            } else {
                iterator.remove();
            }
        }
    }
}
