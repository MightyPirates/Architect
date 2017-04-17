package li.cil.architect.common.config;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Whitelist {
    private final Map<ResourceLocation, List<TileEntityFilter>> filtersByBlockRaw = new LinkedHashMap<>();
    private final Map<Block, List<TileEntityFilter>> filtersByBlock = new LinkedHashMap<>();
    // --------------------------------------------------------------------- //

    public Map<ResourceLocation, List<TileEntityFilter>> getEntries() {
        return filtersByBlockRaw;
    }

    boolean contains(final IBlockState state) {
        return getFilter(state) != null;
    }

    @Nullable
    TileEntityFilter getFilter(final IBlockState state) {
        final List<TileEntityFilter> filters = filtersByBlock.get(state.getBlock());
        if (filters == null) {
            return null;
        }
        for (final TileEntityFilter filter : filters) {
            if (filter.getSelector().matches(state)) {
                return filter;
            }
        }
        return null;
    }

    public void add(final TileEntityFilter filter) {
        final ResourceLocation location = filter.getSelector().getLocation();
        List<TileEntityFilter> filters = filtersByBlockRaw.get(location);
        if (filters == null) {
            filters = new ArrayList<>();
            filtersByBlockRaw.put(location, filters);
            final Block block = filter.getSelector().getBlock();
            if (block != null) {
                filtersByBlock.put(block, filters);
            }
        }

        if (filter.getSelector().getProperties().isEmpty()) {
            filters.clear();
        }

        if (filters.contains(filter)) {
            return;
        }

        filters.add(filter);
    }

    boolean add(final Block block, final Map<IProperty<?>, Comparable<?>> properties, final int sortIndex, final Map<String, Object> nbtFilter, final Map<String, Object> nbtStripper) {
        List<TileEntityFilter> filters = filtersByBlock.get(block);
        if (filters == null) {
            final ResourceLocation location = block.getRegistryName();
            filters = new ArrayList<>();
            filtersByBlockRaw.put(location, filters);
            filtersByBlock.put(block, filters);
        }

        final TileEntityFilter filter = new TileEntityFilter(new BlockStateFilter(block, properties), sortIndex, nbtFilter, nbtStripper);

        // When adding a constraint that is empty that means "any".
        if (properties.isEmpty()) {
            if (filters.size() == 1 && filters.contains(filter)) {
                return false;
            }

            filters.clear();
        }

        if (filters.contains(filter)) {
            return false;
        }

        filters.add(filter);
        return true;
    }

    boolean remove(final ResourceLocation location) {
        final Block block = ForgeRegistries.BLOCKS.getValue(location);
        return filtersByBlockRaw.remove(location) != null | filtersByBlock.remove(block) != null;
    }

    void clear() {
        filtersByBlockRaw.clear();
        filtersByBlock.clear();
    }

    void addAll(final Whitelist from) {
        filtersByBlockRaw.putAll(from.filtersByBlockRaw);
        filtersByBlock.putAll(from.filtersByBlock);
    }
}
