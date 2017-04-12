package li.cil.architect.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import li.cil.architect.api.API;
import li.cil.architect.common.Architect;
import li.cil.architect.common.json.ResourceLocationAdapter;
import li.cil.architect.common.json.Types;
import net.minecraft.block.Block;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final Set<ResourceLocation> blacklist = new HashSet<>();

    /**
     * The list of blocks with tile entities allowed to be converted by
     * built-in converters.
     */
    private static final Set<ResourceLocation> whitelist = new HashSet<>();

    /**
     * The list of blocks to convert using the attached block sorting index.
     */
    private static final Set<ResourceLocation> attachedBlocks = new HashSet<>();

    /**
     * The mappings of blocks to other blocks for replacements in blueprints.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToBlockMapping = new HashMap<>();

    /**
     * The mappings of blocks to items for lookup of items for blocks.
     */
    private static final Map<ResourceLocation, ResourceLocation> blockToItemMapping = new HashMap<>();

    // --------------------------------------------------------------------- //
    // Converter accessors

    public static boolean isBlacklisted(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location == null || blacklist.contains(location);
    }

    public static boolean isWhitelisted(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location != null && whitelist.contains(location);
    }

    public static boolean isAttachedBlock(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location != null && attachedBlocks.contains(location);
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

    public static boolean addToBlacklist(@Nullable final ResourceLocation location) {
        if (blacklist.add(location)) {
            saveJSON();
            return true;
        }
        return false;
    }

    public static boolean removeFromBlacklist(@Nullable final ResourceLocation location) {
        if (blacklist.remove(location)) {
            saveJSON();
            return true;
        }
        return false;
    }

    public static String[] getWhitelist() {
        return toStringArray(whitelist);
    }

    public static void setWhitelist(final String[] values) {
        whitelist.clear();
        whitelist.addAll(toResourceLocationSet(values));
    }

    public static boolean addToWhitelist(@Nullable final ResourceLocation location) {
        if (whitelist.add(location)) {
            saveJSON();
            return true;
        }
        return false;
    }

    public static boolean removeFromWhitelist(@Nullable final ResourceLocation location) {
        if (whitelist.remove(location)) {
            saveJSON();
            return true;
        }
        return false;
    }

    public static String[] getAttachedBlocks() {
        return toStringArray(attachedBlocks);
    }

    public static void setAttachedBlocks(final String[] values) {
        attachedBlocks.clear();
        attachedBlocks.addAll(toResourceLocationSet(values));
    }

    public static boolean addToAttachedBlockList(@Nullable final ResourceLocation location) {
        if (attachedBlocks.add(location)) {
            saveJSON();
            return true;
        }
        return false;
    }

    public static boolean removeFromAttachedBlockList(@Nullable final ResourceLocation location) {
        if (attachedBlocks.remove(location)) {
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
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).create();

        loadJason(blacklist, Constants.BLACKLIST_FILENAME, configDirectory, gson);
        loadJason(whitelist, Constants.WHITELIST_FILENAME, configDirectory, gson);
        loadJason(attachedBlocks, Constants.ATTACHED_BLOCKS_FILENAME, configDirectory, gson);
        loadJason(blockToBlockMapping, Constants.BLOCK_MAPPING_FILENAME, configDirectory, gson);
        loadJason(blockToItemMapping, Constants.ITEM_MAPPING_FILENAME, configDirectory, gson);
    }

    public static void saveJSON() {
        final String configDirectory = Loader.instance().getConfigDir().getPath();
        final Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).create();

        saveJason(blacklist, Constants.BLACKLIST_FILENAME, configDirectory, gson);
        saveJason(whitelist, Constants.WHITELIST_FILENAME, configDirectory, gson);
        saveJason(attachedBlocks, Constants.ATTACHED_BLOCKS_FILENAME, configDirectory, gson);
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

    private static void loadJason(final Set<ResourceLocation> set, final String fileName, final String basePath, final Gson gson) {
        final Set<ResourceLocation> result = loadJason(set, fileName, Types.SET_RESOURCE_LOCATION, basePath, gson);
        if (result != set) {
            set.clear();
            set.addAll(result);
        }
    }

    private static void loadJason(final Map<ResourceLocation, ResourceLocation> map, final String fileName, final String basePath, final Gson gson) {
        final Map<ResourceLocation, ResourceLocation> result = loadJason(map, fileName, Types.MAP_RESOURCE_LOCATION, basePath, gson);
        if (result != map) {
            map.clear();
            map.putAll(result);
        }
    }

    private static <T> T loadJason(T value, final String fileName, final Type type, final String basePath, final Gson gson) {
        final File path = Paths.get(basePath, API.MOD_ID, fileName).toFile();
        if (path.exists()) {
            value = loadJason(value, path, type, gson);
        } else {
            value = loadDefaultJason(value, fileName, type, gson);
        }
        saveJason(value, path, gson);
        return value;
    }

    private static <T> T loadJason(T value, final File path, final Type type, final Gson gson) {
        try (final InputStream stream = new FileInputStream(path)) {
            value = gson.fromJson(new InputStreamReader(stream), type);
        } catch (IOException e) {
            Architect.getLog().warn("Failed reading " + path.toString() + ".", e);
        }
        return value;
    }

    private static <T> T loadDefaultJason(T value, final String fileName, final Type type, final Gson gson) {
        try (final InputStream stream = Settings.class.getResourceAsStream("/assets/" + API.MOD_ID + "/config/" + fileName)) {
            value = gson.fromJson(new InputStreamReader(stream), type);
        } catch (IOException e) {
            Architect.getLog().warn("Failed loading defaults for " + fileName + ".", e);
        }
        return value;
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
