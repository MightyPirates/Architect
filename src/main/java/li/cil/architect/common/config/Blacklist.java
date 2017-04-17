package li.cil.architect.common.config;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Blacklist {
    private final Map<ResourceLocation, List<BlockStateFilter>> filtersByBlockRaw = new LinkedHashMap<>();
    private final Map<Block, List<BlockStateFilter>> filtersByBlock = new LinkedHashMap<>();

    // --------------------------------------------------------------------- //

    public Map<ResourceLocation, List<BlockStateFilter>> getEntries() {
        return filtersByBlockRaw;
    }

    public void add(final BlockStateFilter filter) {
        final ResourceLocation location = filter.getLocation();
        List<BlockStateFilter> filters = filtersByBlockRaw.get(location);
        if (filters == null) {
            filters = new ArrayList<>();
            filtersByBlockRaw.put(location, filters);
            final Block block = filter.getBlock();
            if (block != null) {
                filtersByBlock.put(block, filters);
            }
        }

        if (filter.getProperties().isEmpty()) {
            filters.clear();
        }

        if (filters.contains(filter)) {
            return;
        }

        filters.add(filter);
    }

    boolean contains(final IBlockState state) {
        final List<BlockStateFilter> filters = filtersByBlock.get(state.getBlock());
        if (filters == null) {
            return false;
        }
        for (final BlockStateFilter filter : filters) {
            if (filter.matches(state)) {
                return true;
            }
        }
        return false;
    }

    boolean add(final Block block, final Map<IProperty<?>, Comparable<?>> properties) {
        List<BlockStateFilter> filters = filtersByBlock.get(block);
        if (filters == null) {
            final ResourceLocation location = block.getRegistryName();
            filters = new ArrayList<>();
            filtersByBlockRaw.put(location, filters);
            filtersByBlock.put(block, filters);
        }

        final BlockStateFilter filter = new BlockStateFilter(block, properties);

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

    void addAll(final Blacklist from) {
        filtersByBlockRaw.putAll(from.filtersByBlockRaw);
        filtersByBlock.putAll(from.filtersByBlock);
    }
}
