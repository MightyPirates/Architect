package li.cil.architect.common.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import li.cil.architect.api.API;
import li.cil.architect.common.Architect;
import li.cil.architect.common.json.BlacklistAdapter;
import li.cil.architect.common.json.BlockStateFilterAdapter;
import li.cil.architect.common.json.ConverterFilterAdapter;
import li.cil.architect.common.json.ResourceLocationAdapter;
import li.cil.architect.common.json.Types;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Settings stored in JSON files.
 */
public final class Jasons {
    /**
     * The list of blocks to ignore in built-in converters, user configurable.
     */
    private static final Blacklist blacklist = new Blacklist();

    /**
     * Same as {@link #blacklist}, but never saved, filled via IMCs.
     */
    private static final Blacklist blacklistIMC = new Blacklist();

    /**
     * Same as {@link #blacklist}, but never saved, built-in defaults.
     */
    private static final Blacklist blacklistDefaults = new Blacklist();

    /**
     * The list of blocks with tile entities allowed to be converted by
     * built-in converters, user configurable.
     */
    private static final Map<ResourceLocation, ConverterFilter> whitelist = new LinkedHashMap<>();

    /**
     * Same as {@link #whitelist}, but never saved, filled via IMCs.
     */
    private static final Map<ResourceLocation, ConverterFilter> whitelistIMC = new LinkedHashMap<>();

    /**
     * Same as {@link #whitelist}, but never saved, built-in defaults.
     */
    private static final Map<ResourceLocation, ConverterFilter> whitelistDefaults = new LinkedHashMap<>();

    /**
     * The mappings of blocks to other blocks for replacements in blueprints.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToBlockMapping = new LinkedHashMap<>();

    /**
     * Same as {@link #blockToBlockMapping}, but never saved, filled via IMCs.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToBlockMappingIMC = new LinkedHashMap<>();

    /**
     * Same as {@link #blockToBlockMapping}, but never saved, built-in defaults.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToBlockMappingDefaults = new LinkedHashMap<>();

    /**
     * The mappings of blocks to items for lookup of items for blocks.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToItemMapping = new LinkedHashMap<>();

    /**
     * Same as {@link #blockToItemMapping}, but never saved, filled via IMCs.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToItemMappingIMC = new LinkedHashMap<>();

    /**
     * Same as {@link #blockToItemMapping}, but never saved, built-in defaults.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToItemMappingDefaults = new LinkedHashMap<>();

    // --------------------------------------------------------------------- //
    // Converter accessors

    public static boolean isBlacklisted(final IBlockState state) {
        final ResourceLocation location = state.getBlock().getRegistryName();
        if (location == null) {
            return true;
        }

        if (blacklist.contains(state)) {
            return true;
        }
        if (whitelist.containsKey(location)) {
            return false;
        }
        if (blacklistIMC.contains(state)) {
            return true;
        }
        if (whitelistIMC.containsKey(location)) {
            return false;
        }
        return blacklistDefaults.contains(state);
    }

    @Nullable
    public static ConverterFilter getFilter(@Nullable final IBlockState state) {
        if (state == null) {
            return null;
        }

        final ResourceLocation location = state.getBlock().getRegistryName();
        if (location == null) {
            return null;
        }

        ConverterFilter filter = whitelist.get(location);
        if (filter == null) {
            filter = whitelistIMC.get(location);
        }
        if (filter == null) {
            filter = whitelistDefaults.get(location);
        }
        return filter;
    }

    public static int getSortIndex(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        ConverterFilter filter = whitelist.get(location);
        if (filter == null) {
            filter = whitelistIMC.get(location);
        }
        if (filter == null) {
            filter = whitelistDefaults.get(location);
        }
        return filter == null ? 0 : filter.getSortIndex();
    }

    @SuppressWarnings("deprecation")
    public static IBlockState mapBlockToBlock(final IBlockState state) {
        final ResourceLocation location = state.getBlock().getRegistryName();
        if (location == null) {
            return state;
        }
        ResourceLocation mappedLocation = blockToBlockMapping.get(location);
        if (mappedLocation == null) {
            mappedLocation = blockToBlockMappingIMC.get(location);
        }
        if (mappedLocation == null) {
            mappedLocation = blockToBlockMappingDefaults.get(location);
        }
        if (mappedLocation == null) {
            return state;
        }
        final Block mappedBlock = ForgeRegistries.BLOCKS.getValue(mappedLocation);
        if (mappedBlock == null || mappedBlock == Blocks.AIR) {
            return state;
        }

        // When mapping blocks, only keep metadata if class stays the same,
        // otherwise we can't rely on the meta conversion to work correctly.
        return mappedBlock.getClass() == state.getBlock().getClass() ? mappedBlock.getStateFromMeta(state.getBlock().getMetaFromState(state)) : mappedBlock.getDefaultState();
    }

    public static Item mapBlockToItem(final Block block) {
        final ResourceLocation blockLocation = block.getRegistryName();
        if (blockLocation == null) {
            return Item.getItemFromBlock(block);
        }
        ResourceLocation itemLocation = blockToItemMapping.get(blockLocation);
        if (itemLocation == null) {
            itemLocation = blockToItemMappingIMC.get(blockLocation);
        }
        if (itemLocation == null) {
            itemLocation = blockToItemMappingDefaults.get(blockLocation);
        }
        if (itemLocation == null) {
            return Item.getItemFromBlock(block);
        }
        final Item item = ForgeRegistries.ITEMS.getValue(itemLocation);
        return (item == null || item == Items.AIR) ? Item.getItemFromBlock(block) : item;
    }

    // --------------------------------------------------------------------- //
    // IMC accessors

    public static void addToIMCBlacklist(final Block block, final ImmutableMap<IProperty<?>, Comparable<?>> properties) {
        blacklistIMC.add(block, properties);
    }

    public static void addToIMCWhitelist(final ResourceLocation location, final ConverterFilter filter) {
        whitelistIMC.put(location, filter);
    }

    public static void addIMCBlockMapping(final ResourceLocation location, final ResourceLocation mapping) {
        blockToBlockMappingIMC.put(location, mapping);
    }

    public static void addIMCItemMapping(final ResourceLocation location, final ResourceLocation mapping) {
        blockToItemMappingIMC.put(location, mapping);
    }

    // --------------------------------------------------------------------- //
    // Config GUI and command accessors

    public static boolean addToBlacklist(final Block block, final ImmutableMap<IProperty<?>, Comparable<?>> properties) {
        if (blacklist.add(block, properties)) {
            saveJSON();
            return true;
        }
        return false;
    }

    public static boolean removeFromBlacklist(final ResourceLocation location) {
        if (blacklist.remove(location)) {
            saveJSON();
            return true;
        }
        return false;
    }

    public static boolean addToWhitelist(final ResourceLocation location, final int sortIndex) {
        if (!whitelist.containsKey(location)) {
            whitelist.put(location, new ConverterFilter(sortIndex));
            saveJSON();
            return true;
        } else {
            final ConverterFilter filter = whitelist.get(location);
            if (filter.getSortIndex() != sortIndex) {
                filter.setSortIndex(sortIndex);
                saveJSON();
                return true;
            }
        }
        return false;
    }

    public static boolean removeFromWhitelist(final ResourceLocation location) {
        if (whitelist.containsKey(location)) {
            whitelist.remove(location);
            saveJSON();
            return true;
        }
        return false;
    }

    @Nullable
    public static ResourceLocation getBlockMapping(final ResourceLocation location) {
        ResourceLocation mapping = blockToBlockMapping.get(location);
        if (mapping == null) {
            mapping = blockToItemMappingIMC.get(location);
        }
        return mapping;
    }

    public static boolean addBlockMapping(final ResourceLocation location, final ResourceLocation mapping) {
        if (!Objects.equals(blockToBlockMapping.get(location), mapping)) {
            blockToBlockMapping.put(location, mapping);
            saveJSON();
            return true;
        }
        return false;
    }

    public static boolean removeBlockMapping(final ResourceLocation location) {
        if (blockToBlockMapping.remove(location) != null) {
            saveJSON();
            return true;
        }
        return false;
    }

    @Nullable
    public static ResourceLocation getItemMapping(final ResourceLocation location) {
        ResourceLocation mapping = blockToItemMapping.get(location);
        if (mapping == null) {
            mapping = blockToItemMappingIMC.get(location);
        }
        if (mapping == null) {
            final Block block = ForgeRegistries.BLOCKS.getValue(location);
            if (block != null) {
                mapping = Item.getItemFromBlock(block).getRegistryName();
            }
        }
        return mapping;
    }

    public static boolean addItemMapping(final ResourceLocation location, final ResourceLocation mapping) {
        if (!Objects.equals(blockToItemMapping.get(location), mapping)) {
            blockToItemMapping.put(location, mapping);
            saveJSON();
            return true;
        }
        return false;
    }

    public static boolean removeItemMapping(final ResourceLocation location) {
        if (blockToItemMapping.remove(location) != null) {
            saveJSON();
            return true;
        }
        return false;
    }

    // --------------------------------------------------------------------- //

    public static Map<String, Throwable> loadJSON(final boolean initDefaults) {
        final String configDirectory = Loader.instance().getConfigDir().getPath();
        final Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).
                registerTypeAdapter(BlockStateFilter.class, new BlockStateFilterAdapter()).
                registerTypeAdapter(Blacklist.class, new BlacklistAdapter()).
                registerTypeAdapter(ConverterFilter.class, new ConverterFilterAdapter()).
                create();

        if (initDefaults) {
            loadDefaultBlacklist(gson);
            loadDefaultWhitelist(gson);
            loadDefaultBlockMapping(gson);
            loadDefaultItemMapping(gson);
        }

        final Map<String, Throwable> errors = new LinkedHashMap<>();
        loadBlacklist(configDirectory, gson, errors);
        loadWhitelist(configDirectory, gson, errors);
        loadMapping(blockToBlockMapping, Constants.BLOCK_MAPPING_FILENAME, configDirectory, gson, errors);
        loadMapping(blockToItemMapping, Constants.ITEM_MAPPING_FILENAME, configDirectory, gson, errors);
        return errors;
    }

    public static void saveJSON() {
        final String configDirectory = Loader.instance().getConfigDir().getPath();
        final Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).
                registerTypeAdapter(BlockStateFilter.class, new BlockStateFilterAdapter()).
                registerTypeAdapter(Blacklist.class, new BlacklistAdapter()).
                registerTypeAdapter(ConverterFilter.class, new ConverterFilterAdapter()).
                create();

        save(blacklist, Constants.BLACKLIST_FILENAME, configDirectory, gson);
        save(whitelist, Constants.WHITELIST_FILENAME, configDirectory, gson);
        save(blockToBlockMapping, Constants.BLOCK_MAPPING_FILENAME, configDirectory, gson);
        save(blockToItemMapping, Constants.ITEM_MAPPING_FILENAME, configDirectory, gson);
    }

    // --------------------------------------------------------------------- //

    private static void loadDefaultBlacklist(final Gson gson) {
        try {
            final Blacklist result = loadDefault(Constants.BLACKLIST_FILENAME, Blacklist.class, gson);
            blacklistDefaults.clear();
            blacklistDefaults.addAll(result);
        } catch (final IOException | JsonSyntaxException e) {
            Architect.getLog().warn("Failed reading " + Constants.BLACKLIST_FILENAME + ".", e);
        }
    }

    private static void loadDefaultWhitelist(final Gson gson) {
        try {
            final Map<ResourceLocation, ConverterFilter> result = loadDefault(Constants.WHITELIST_FILENAME, Types.MAP_CONVERTER_FILTER, gson);
            whitelistDefaults.clear();
            whitelistDefaults.putAll(result);
        } catch (final IOException | JsonSyntaxException e) {
            Architect.getLog().warn("Failed reading " + Constants.WHITELIST_FILENAME + ".", e);
        }
    }

    private static void loadDefaultBlockMapping(final Gson gson) {
        try {
            final Map<ResourceLocation, ResourceLocation> result = loadDefault(Constants.BLOCK_MAPPING_FILENAME, Types.MAP_RESOURCE_LOCATION, gson);
            blockToBlockMappingDefaults.clear();
            blockToBlockMappingDefaults.putAll(result);
        } catch (final IOException | JsonSyntaxException e) {
            Architect.getLog().warn("Failed reading " + Constants.BLOCK_MAPPING_FILENAME + ".", e);
        }
    }

    private static void loadDefaultItemMapping(final Gson gson) {
        try {
            final Map<ResourceLocation, ResourceLocation> result = loadDefault(Constants.ITEM_MAPPING_FILENAME, Types.MAP_RESOURCE_LOCATION, gson);
            blockToItemMappingDefaults.clear();
            blockToItemMappingDefaults.putAll(result);
        } catch (final IOException | JsonSyntaxException e) {
            Architect.getLog().warn("Failed reading " + Constants.ITEM_MAPPING_FILENAME + ".", e);
        }
    }

    private static void loadBlacklist(final String basePath, final Gson gson, final Map<String, Throwable> errors) {
        final Blacklist result = load(blacklist, Constants.BLACKLIST_FILENAME, Blacklist.class, basePath, gson, errors);
        if (result != blacklist) {
            blacklist.clear();
            blacklist.addAll(result);
        }
    }

    private static void loadWhitelist(final String basePath, final Gson gson, final Map<String, Throwable> errors) {
        final Map<ResourceLocation, ConverterFilter> result = load(whitelist, Constants.WHITELIST_FILENAME, Types.MAP_CONVERTER_FILTER, basePath, gson, errors);
        if (result != whitelist) {
            whitelist.clear();
            whitelist.putAll(result);
        }
    }

    private static void loadMapping(final Map<ResourceLocation, ResourceLocation> map, final String fileName, final String basePath, final Gson gson, final Map<String, Throwable> errors) {
        final Map<ResourceLocation, ResourceLocation> result = load(map, fileName, Types.MAP_RESOURCE_LOCATION, basePath, gson, errors);
        if (result != map) {
            map.clear();
            map.putAll(result);
        }
    }

    private static <T> T load(T value, final String fileName, final Type type, final String basePath, final Gson gson, final Map<String, Throwable> errors) {
        final File path = Paths.get(basePath, API.MOD_ID, fileName).toFile();
        try {
            if (path.exists()) {
                value = load(path, type, gson);
            } else {
                value = loadDefault(fileName, type, gson);
            }
            save(value, path, gson);
        } catch (final IOException | JsonSyntaxException e) {
            Architect.getLog().warn("Failed reading " + fileName + ".", e);
            errors.put(fileName, e);
        }
        return value;
    }

    private static <T> T load(final File path, final Type type, final Gson gson) throws IOException, JsonSyntaxException {
        try (final InputStream stream = new FileInputStream(path)) {
            return gson.fromJson(new InputStreamReader(stream), type);
        }
    }

    private static <T> T loadDefault(final String fileName, final Type type, final Gson gson) throws IOException, JsonSyntaxException {
        try (final InputStream stream = Settings.class.getResourceAsStream("/assets/" + API.MOD_ID + "/config/" + fileName)) {
            return gson.fromJson(new InputStreamReader(stream), type);
        }
    }

    private static void save(final Object value, final String fileName, final String basePath, final Gson gson) {
        final File path = Paths.get(basePath, API.MOD_ID, fileName).toFile();
        save(value, path, gson);
    }

    private static void save(final Object value, final File path, final Gson gson) {
        try {
            FileUtils.writeStringToFile(path, gson.toJson(value));
        } catch (final IOException e) {
            Architect.getLog().warn("Failed writing " + path.toString() + ".", e);
        }
    }
}
