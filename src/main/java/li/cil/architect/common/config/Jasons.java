package li.cil.architect.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import li.cil.architect.api.API;
import li.cil.architect.common.Architect;
import li.cil.architect.common.json.ConverterFilterAdapter;
import li.cil.architect.common.json.ResourceLocationAdapter;
import li.cil.architect.common.json.Types;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Settings stored in JSON files.
 */
public final class Jasons {
    /**
     * The list of blocks to ignore in built-in converters, user configurable.
     */
    private static final Set<ResourceLocation> blacklist = new LinkedHashSet<>();

    /**
     * Same as {@link #blacklist}, but never saved, filled via IMCs.
     */
    private static final Set<ResourceLocation> blacklistIMC = new HashSet<>();

    /**
     * Same as {@link #blacklist}, but never saved, built-in defaults.
     */
    private static final Set<ResourceLocation> blacklistDefaults = new HashSet<>();

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

    public static boolean isBlacklisted(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        if (location == null) {
            return true;
        }
        if (blacklist.contains(location)) {
            return true;
        }
        if (whitelist.containsKey(location)) {
            return false;
        }
        if (blacklistIMC.contains(location)) {
            return true;
        }
        if (whitelistIMC.containsKey(location)) {
            return false;
        }
        return blacklistDefaults.contains(location);
    }

    public static boolean isWhitelisted(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location != null && (whitelist.containsKey(location) ||
                                    whitelistIMC.containsKey(location) ||
                                    whitelistDefaults.containsKey(location));
    }

    public static boolean hasNbtFilter(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        ConverterFilter filter = whitelist.get(location);
        if (filter == null) {
            filter = whitelistIMC.get(location);
        }
        if (filter == null) {
            filter = whitelistDefaults.get(location);
        }
        return filter != null && !filter.getNbtFilter().isEmpty();
    }

    public static void filterNbt(final Block block, final NBTTagCompound nbt) {
        final ResourceLocation location = block.getRegistryName();
        ConverterFilter filter = whitelist.get(location);
        if (filter == null) {
            filter = whitelistIMC.get(location);
        }
        if (filter == null) {
            whitelistDefaults.get(location);
        }
        if (filter != null) {
            filter.filter(nbt);
        }
    }

    public static void stripNbt(final Block block, final NBTTagCompound nbt) {
        final ResourceLocation location = block.getRegistryName();
        ConverterFilter filter = whitelist.get(location);
        if (filter == null) {
            filter = whitelistIMC.get(location);
        }
        if (filter == null) {
            filter = whitelistDefaults.get(location);
        }
        if (filter != null) {
            filter.strip(nbt);
        }
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

    public static Block mapBlockToBlock(final Block block) {
        final ResourceLocation blockLocation = block.getRegistryName();
        if (blockLocation == null) {
            return block;
        }
        ResourceLocation mappedLocation = blockToBlockMapping.get(blockLocation);
        if (mappedLocation == null) {
            mappedLocation = blockToBlockMappingIMC.get(blockLocation);
        }
        if (mappedLocation == null) {
            mappedLocation = blockToBlockMappingDefaults.get(blockLocation);
        }
        if (mappedLocation == null) {
            return block;
        }
        final Block mappedBlock = ForgeRegistries.BLOCKS.getValue(mappedLocation);
        return (mappedBlock == null || mappedBlock == Blocks.AIR) ? block : mappedBlock;
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

    public static void addToIMCBlacklist(final ResourceLocation location) {
        blacklistIMC.add(location);
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

    public static String[] getBlacklist() {
        return toStringArray(blacklist);
    }

    public static void setBlacklist(final String[] values) {
        blacklist.clear();
        blacklist.addAll(toResourceLocationSet(values));
    }

    public static boolean addToBlacklist(final ResourceLocation location) {
        if (blacklist.add(location)) {
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
                mapping = ForgeRegistries.ITEMS.getKey(Item.getItemFromBlock(block));
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

    public static void loadJSON(final boolean initDefaults) {
        final String configDirectory = Loader.instance().getConfigDir().getPath();
        final Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).
                registerTypeAdapter(ConverterFilter.class, new ConverterFilterAdapter()).
                create();

        if (initDefaults) {
            loadDefaultBlacklist(gson);
            loadDefaultWhitelist(gson);
            loadDefaultBlockMapping(gson);
            loadDefaultItemMapping(gson);
        }

        loadBlacklist(configDirectory, gson);
        loadWhitelist(configDirectory, gson);
        loadMapping(blockToBlockMapping, Constants.BLOCK_MAPPING_FILENAME, configDirectory, gson);
        loadMapping(blockToItemMapping, Constants.ITEM_MAPPING_FILENAME, configDirectory, gson);
    }

    public static void saveJSON() {
        final String configDirectory = Loader.instance().getConfigDir().getPath();
        final Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).
                registerTypeAdapter(ConverterFilter.class, new ConverterFilterAdapter()).
                create();

        save(blacklist, Constants.BLACKLIST_FILENAME, configDirectory, gson);
        save(whitelist, Constants.WHITELIST_FILENAME, configDirectory, gson);
        save(blockToBlockMapping, Constants.BLOCK_MAPPING_FILENAME, configDirectory, gson);
        save(blockToItemMapping, Constants.ITEM_MAPPING_FILENAME, configDirectory, gson);
    }

    // --------------------------------------------------------------------- //

    private static String[] toStringArray(final Collection<ResourceLocation> locations) {
        return locations.stream().map(ResourceLocation::toString).sorted().toArray(String[]::new);
    }

    private static Set<ResourceLocation> toResourceLocationSet(final String[] values) {
        return Arrays.stream(values).map(ResourceLocation::new).collect(Collectors.toSet());
    }

    private static void loadDefaultBlacklist(final Gson gson) {
        try {
            final Collection<ResourceLocation> result = loadDefault(Constants.BLACKLIST_FILENAME, Types.SET_RESOURCE_LOCATION, gson);
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

    private static void loadBlacklist(final String basePath, final Gson gson) {
        final Set<ResourceLocation> result = load(blacklist, Constants.BLACKLIST_FILENAME, Types.SET_RESOURCE_LOCATION, basePath, gson);
        if (result != blacklist) {
            blacklist.clear();
            blacklist.addAll(result);
        }
    }

    private static void loadWhitelist(final String basePath, final Gson gson) {
        final Map<ResourceLocation, ConverterFilter> result = load(whitelist, Constants.WHITELIST_FILENAME, Types.MAP_CONVERTER_FILTER, basePath, gson);
        if (result != whitelist) {
            whitelist.clear();
            whitelist.putAll(result);
        }
    }

    private static void loadMapping(final Map<ResourceLocation, ResourceLocation> map, final String fileName, final String basePath, final Gson gson) {
        final Map<ResourceLocation, ResourceLocation> result = load(map, fileName, Types.MAP_RESOURCE_LOCATION, basePath, gson);
        if (result != map) {
            map.clear();
            map.putAll(result);
        }
    }

    private static <T> T load(T value, final String fileName, final Type type, final String basePath, final Gson gson) {
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
