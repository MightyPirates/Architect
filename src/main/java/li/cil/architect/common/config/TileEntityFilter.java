package li.cil.architect.common.config;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class TileEntityFilter {
    private final BlockStateFilter selector;
    private int sortIndex;
    private final Map<String, Object> nbtFilter;
    private final Map<String, Object> nbtStripper;

    // --------------------------------------------------------------------- //

    public TileEntityFilter(final BlockStateFilter selector, final int sortIndex, final Map<String, Object> nbtFilter, final Map<String, Object> nbtStripper) {
        this.selector = selector;
        this.sortIndex = sortIndex;
        this.nbtFilter = new HashMap<>(nbtFilter);
        this.nbtStripper = new HashMap<>(nbtStripper);
    }

    public BlockStateFilter getSelector() {
        return selector;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public Map<String, Object> getNbtFilter() {
        return nbtFilter;
    }

    public Map<String, Object> getNbtStripper() {
        return nbtStripper;
    }

    public void filter(final NBTTagCompound nbt) {
        filter(nbt, nbtFilter);
    }

    public void strip(final NBTTagCompound nbt) {
        strip(nbt, nbtStripper);
    }

    // --------------------------------------------------------------------- //
    // Object

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TileEntityFilter filter = (TileEntityFilter) o;
        if (sortIndex != filter.sortIndex) {
            return false;
        }
        if (!selector.equals(filter.selector)) {
            return false;
        }
        if (!nbtFilter.equals(filter.nbtFilter)) {
            return false;
        }
        return nbtStripper.equals(filter.nbtStripper);
    }

    @Override
    public int hashCode() {
        int result = selector.hashCode();
        result = 31 * result + sortIndex;
        result = 31 * result + nbtFilter.hashCode();
        result = 31 * result + nbtStripper.hashCode();
        return result;
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

    @SuppressWarnings("unchecked")
    private static void strip(final NBTTagCompound nbt, final Map<String, Object> filter) {
        for (final Iterator<String> iterator = nbt.getKeySet().iterator(); iterator.hasNext(); ) {
            final String key = iterator.next();
            if (!filter.containsKey(key)) {
                continue;
            }
            final NBTBase value = nbt.getTag(key);
            final Object filterValue = filter.get(key);
            if (filterValue instanceof Map && value instanceof NBTTagCompound) {
                strip((NBTTagCompound) value, (Map<String, Object>) filterValue);
            } else {
                iterator.remove();
            }
        }
    }
}
