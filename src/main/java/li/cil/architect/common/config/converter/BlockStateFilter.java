package li.cil.architect.common.config.converter;

import com.google.common.base.Optional;
import li.cil.architect.common.Architect;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class BlockStateFilter {
    // Keep the raw values for serialization, avoids losing data if the
    // block does not exist in the current setup.
    private final ResourceLocation blockRaw;
    private final Map<String, String> propertiesRaw;

    @Nullable
    private final Block block;
    private final Map<IProperty<?>, Comparable<?>> properties;

    // --------------------------------------------------------------------- //

    public BlockStateFilter(final ResourceLocation location, final Map<String, String> properties) {
        this.blockRaw = location;
        this.propertiesRaw = new LinkedHashMap<>(properties);

        block = ForgeRegistries.BLOCKS.getValue(location);
        this.properties = new HashMap<>();

        if (block != null) {
            final IBlockState state = block.getDefaultState();
            final Collection<IProperty<?>> blockProperties = state.getPropertyKeys();
            outer:
            for (final Map.Entry<String, String> entry : properties.entrySet()) {
                for (final IProperty<?> property : blockProperties) {
                    if (Objects.equals(property.getName(), entry.getKey())) {
                        final Optional<? extends Comparable> value = property.parseValue(entry.getValue());
                        if (value.isPresent()) {
                            this.properties.put(property, value.get());
                        } else {
                            Architect.getLog().warn("Cannot parse property value '{}' for property '{}' of block {}.", entry.getValue(), entry.getKey(), location);
                        }
                        continue outer;
                    }
                }
                Architect.getLog().warn("Block {} has no property '{}'.", location, entry.getKey());
            }
        }
    }

    @SuppressWarnings("unchecked")
    BlockStateFilter(@Nonnull final Block block, final Map<IProperty<?>, Comparable<?>> properties) {
        this.block = block;
        this.properties = new HashMap<>(properties);

        blockRaw = block.getRegistryName();
        propertiesRaw = new HashMap<>();

        for (final Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
            final IProperty key = entry.getKey();
            final Comparable value = entry.getValue();
            propertiesRaw.put(key.getName(), key.getName(value));
        }
    }

    public ResourceLocation getLocation() {
        return blockRaw;
    }

    public Map<String, String> getProperties() {
        return propertiesRaw;
    }

    @Nullable
    public Block getBlock() {
        return block;
    }

    public Map<IProperty<?>, Comparable<?>> getActualProperties() {
        return properties;
    }

    boolean matches(final IBlockState state) {
        if (state.getBlock() != block) {
            return false;
        }

        if (properties.isEmpty()) {
            return true;
        }

        for (final Map.Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet()) {
            if (!entry.getValue().equals(state.getValue(entry.getKey()))) {
                return false;
            }
        }

        return true;
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

        final BlockStateFilter that = (BlockStateFilter) o;
        return blockRaw.equals(that.blockRaw) && propertiesRaw.equals(that.propertiesRaw);
    }

    @Override
    public int hashCode() {
        int result = blockRaw.hashCode();
        result = 31 * result + propertiesRaw.hashCode();
        return result;
    }
}
