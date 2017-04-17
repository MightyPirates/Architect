package li.cil.architect.common.config.converter;

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

public abstract class AbstractFilterList<F> {
    private final Map<ResourceLocation, List<F>> filtersByBlockRaw = new LinkedHashMap<>();
    private final Map<Block, List<F>> filtersByBlock = new LinkedHashMap<>();
    // --------------------------------------------------------------------- //

    public Map<ResourceLocation, List<F>> getEntries() {
        return filtersByBlockRaw;
    }

    public void add(final F filter) {
        final BlockStateFilter selector = getSelector(filter);
        final ResourceLocation location = selector.getLocation();
        List<F> filters = filtersByBlockRaw.get(location);
        if (filters == null) {
            filters = new ArrayList<>();
            filtersByBlockRaw.put(location, filters);
            final Block block = selector.getBlock();
            if (block != null) {
                filtersByBlock.put(block, filters);
            }
        }

        if (selector.getProperties().isEmpty()) {
            filters.clear();
        }

        if (filters.contains(filter)) {
            return;
        }

        filters.add(filter);
    }

    public void addAll(final AbstractFilterList<F> from) {
        filtersByBlockRaw.putAll(from.filtersByBlockRaw);
        filtersByBlock.putAll(from.filtersByBlock);
    }

    public boolean remove(final ResourceLocation location) {
        final Block block = ForgeRegistries.BLOCKS.getValue(location);
        return filtersByBlockRaw.remove(location) != null | filtersByBlock.remove(block) != null;
    }

    public void clear() {
        filtersByBlockRaw.clear();
        filtersByBlock.clear();
    }

    public boolean contains(final IBlockState state) {
        return getFilter(state) != null;
    }

    @Nullable
    public F getFilter(final IBlockState state) {
        final List<F> filters = filtersByBlock.get(state.getBlock());
        if (filters == null) {
            return null;
        }
        for (final F filter : filters) {
            if (getSelector(filter).matches(state)) {
                return filter;
            }
        }
        return null;
    }

    protected List<F> getFilters(final Block block) {
        List<F> filters = filtersByBlock.get(block);
        if (filters == null) {
            final ResourceLocation location = block.getRegistryName();
            filters = new ArrayList<>();
            filtersByBlockRaw.put(location, filters);
            filtersByBlock.put(block, filters);
        }
        return filters;
    }

    protected boolean addFilter(final F filter, final List<F> filters, final Map<IProperty<?>, Comparable<?>> properties) {
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

    protected abstract BlockStateFilter getSelector(final F filter);
}
