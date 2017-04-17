package li.cil.architect.common.config.converter;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;

import java.util.List;
import java.util.Map;

public final class Blacklist extends AbstractFilterList<BlockStateFilter> {
    @Override
    protected BlockStateFilter getSelector(final BlockStateFilter filter) {
        return filter;
    }

    public boolean add(final Block block, final Map<IProperty<?>, Comparable<?>> properties) {
        final List<BlockStateFilter> filters = getFilters(block);

        final BlockStateFilter filter = new BlockStateFilter(block, properties);

        return addFilter(filter, filters, properties);
    }
}
