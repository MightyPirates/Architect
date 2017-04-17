package li.cil.architect.common.config.converter;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;

import java.util.List;
import java.util.Map;

public final class Blacklist extends AbstractFilterList<BlockStateFilter> {
    public boolean add(final Block block, final Map<IProperty<?>, Comparable<?>> properties) {
        final List<BlockStateFilter> filters = getFilters(block);

        final BlockStateFilter filter = new BlockStateFilter(block, properties);

        return addFilter(filter, filters, properties.isEmpty());
    }

    @Override
    protected BlockStateFilter getSelector(final BlockStateFilter filter) {
        return filter;
    }

    @Override
    protected boolean addFilter(final BlockStateFilter filter, final List<BlockStateFilter> filters, final boolean isWildcard) {
        if (isWildcard) {
            if (filters.size() == 1 && filters.contains(filter)) {
                return false;
            }

            filters.clear();
        }

        return super.addFilter(filter, filters, isWildcard);
    }
}
