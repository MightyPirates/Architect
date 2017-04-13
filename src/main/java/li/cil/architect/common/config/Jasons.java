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
     * The list of blocks to ignore in built-in converters.
     */
    private static final Set<ResourceLocation> blacklist = new LinkedHashSet<>();

    /**
     * The list of blocks with tile entities allowed to be converted by
     * built-in converters.
     */
    private static final Map<ResourceLocation, ConverterFilter> whitelist = new LinkedHashMap<>();

    /**
     * The mappings of blocks to other blocks for replacements in blueprints.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToBlockMapping = new LinkedHashMap<>();

    /**
     * The mappings of blocks to items for lookup of items for blocks.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToItemMapping = new LinkedHashMap<>();

    // --------------------------------------------------------------------- //
    // Converter accessors

    public static boolean isBlacklisted(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location == null || blacklist.contains(location);
    }

    public static boolean isWhitelisted(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location != null && whitelist.containsKey(location);
    }

    public static boolean isNbtAllowed(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        final ConverterFilter filter = whitelist.get(location);
        return filter != null && !filter.getNbtFilter().isEmpty();
    }

    public static void filterNbt(final Block block, final NBTTagCompound nbt) {
        final ResourceLocation location = block.getRegistryName();
        final ConverterFilter filter = whitelist.get(location);
        if (filter != null) {
            filter.filter(nbt);
        }
    }

    public static int getSortIndex(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        final ConverterFilter filter = whitelist.get(location);
        return filter == null ? 0 : filter.getSortIndex();
    }

    public static Block mapBlockToBlock(final Block block) {
        final ResourceLocation blockLocation = block.getRegistryName();
        if (blockLocation == null) {
            return block;
        }
        final ResourceLocation mappedLocation = blockToBlockMapping.get(blockLocation);
        if (mappedLocation == null) {
            return block;
        }
        final Block mappedBlock = ForgeRegistries.BLOCKS.getValue(mappedLocation);
        return mappedBlock == null ? block : mappedBlock;
    }

    public static Item mapBlockToItem(final Block block) {
        final ResourceLocation blockLocation = block.getRegistryName();
        if (blockLocation == null) {
            return Item.getItemFromBlock(block);
        }
        final ResourceLocation itemLocation = blockToItemMapping.get(blockLocation);
        if (itemLocation == null) {
            return Item.getItemFromBlock(block);
        }
        final Item item = ForgeRegistries.ITEMS.getValue(itemLocation);
        return item == null ? Item.getItemFromBlock(block) : item;
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
        return blockToBlockMapping.get(location);
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
        return blockToItemMapping.get(location);
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

    public static void loadJSON() {
        final String configDirectory = Loader.instance().getConfigDir().getPath();
        final Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).
                registerTypeAdapter(ConverterFilter.class, new ConverterFilterAdapter()).
                create();

        loadJasonBlacklist(blacklist, Constants.BLACKLIST_FILENAME, configDirectory, gson);
        loadJasonWhitelist(whitelist, Constants.WHITELIST_FILENAME, configDirectory, gson);
        loadJasonMapping(blockToBlockMapping, Constants.BLOCK_MAPPING_FILENAME, configDirectory, gson);
        loadJasonMapping(blockToItemMapping, Constants.ITEM_MAPPING_FILENAME, configDirectory, gson);
    }

    public static void saveJSON() {
        final String configDirectory = Loader.instance().getConfigDir().getPath();
        final Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).
                registerTypeAdapter(ConverterFilter.class, new ConverterFilterAdapter()).
                create();

        saveJason(blacklist, Constants.BLACKLIST_FILENAME, configDirectory, gson);
        saveJason(whitelist, Constants.WHITELIST_FILENAME, configDirectory, gson);
        saveJason(blockToBlockMapping, Constants.BLOCK_MAPPING_FILENAME, configDirectory, gson);
        saveJason(blockToItemMapping, Constants.ITEM_MAPPING_FILENAME, configDirectory, gson);
    }

    // --------------------------------------------------------------------- //

    private static String[] toStringArray(final Collection<ResourceLocation> locations) {
        return locations.stream().map(ResourceLocation::toString).sorted().toArray(String[]::new);
    }

    private static Set<ResourceLocation> toResourceLocationSet(final String[] values) {
        return Arrays.stream(values).map(ResourceLocation::new).collect(Collectors.toSet());
    }

    private static void loadJasonBlacklist(final Set<ResourceLocation> set, final String fileName, final String basePath, final Gson gson) {
        final Set<ResourceLocation> result = loadJason(set, fileName, Types.SET_RESOURCE_LOCATION, basePath, gson);
        if (result != set) {
            set.clear();
            set.addAll(result);
        }
    }

    private static void loadJasonWhitelist(final Map<ResourceLocation, ConverterFilter> map, final String fileName, final String basePath, final Gson gson) {
        final Map<ResourceLocation, ConverterFilter> result = loadJason(map, fileName, Types.MAP_CONVERTER_FILTER, basePath, gson);
        if (result != map) {
            map.clear();
            map.putAll(result);
        }
    }

    private static void loadJasonMapping(final Map<ResourceLocation, ResourceLocation> map, final String fileName, final String basePath, final Gson gson) {
        final Map<ResourceLocation, ResourceLocation> result = loadJason(map, fileName, Types.MAP_RESOURCE_LOCATION, basePath, gson);
        if (result != map) {
            map.clear();
            map.putAll(result);
        }
    }

    private static <T> T loadJason(T value, final String fileName, final Type type, final String basePath, final Gson gson) {
        final File path = Paths.get(basePath, API.MOD_ID, fileName).toFile();
        try {
            if (path.exists()) {
                value = loadJason(path, type, gson);
            } else {
                value = loadDefaultJason(fileName, type, gson);
            }
            saveJason(value, path, gson);
        } catch (final IOException | JsonSyntaxException e) {
            Architect.getLog().warn("Failed reading " + path.toString() + ".", e);
        }
        return value;
    }

    private static <T> T loadJason(final File path, final Type type, final Gson gson) throws IOException, JsonSyntaxException {
        try (final InputStream stream = new FileInputStream(path)) {
            return gson.fromJson(new InputStreamReader(stream), type);
        }
    }

    private static <T> T loadDefaultJason(final String fileName, final Type type, final Gson gson) throws IOException, JsonSyntaxException {
        try (final InputStream stream = Settings.class.getResourceAsStream("/assets/" + API.MOD_ID + "/config/" + fileName)) {
            return gson.fromJson(new InputStreamReader(stream), type);
        }
    }

    private static void saveJason(final Object value, final String fileName, final String basePath, final Gson gson) {
        final File path = Paths.get(basePath, API.MOD_ID, fileName).toFile();
        saveJason(value, path, gson);
    }

    private static void saveJason(final Object value, final File path, final Gson gson) {
        try {
            FileUtils.writeStringToFile(path, gson.toJson(value));
        } catch (final IOException e) {
            Architect.getLog().warn("Failed writing " + path.toString() + ".", e);
        }
    }
}
