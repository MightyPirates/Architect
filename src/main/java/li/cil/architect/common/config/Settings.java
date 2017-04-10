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
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User configurable stuff via config file.
 */
public final class Settings {
    /**
     * The distance at which the free-aim pointer is positioned (e.g. in sketch
     * when finishing a ranged selection in mid-air). This can be changed
     * during runtime.
     */
    public static float freeAimDistance = 4;

    /**
     * The maximum size of a blueprint in any dimension.
     */
    public static int maxBlueprintSize = 16;

    /**
     * The radius around providers in which players must be for them to count.
     */
    public static int maxProviderRadius = 64;

    /**
     * The maximum number of blocks to deserialize (place) per tick per chunk.
     */
    public static int maxChunkOperationsPerTick = 1;

    /**
     * The maximum number of blocks to deserialize (place) per tick per world.
     */
    public static int maxWorldOperationsPerTick = 16;

    /**
     * The list of blocks to ignore in built-in converters.
     */
    private static final Set<ResourceLocation> blockBlacklist = new HashSet<>();

    /**
     * The list of blocks with tile entities allowed to be converted by
     * built-in converters.
     */
    private static final Set<ResourceLocation> tileEntityWhitelist = new HashSet<>();

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

    private static final String CONFIG_VERSION = "1";

    private static final String CATEGORY_BLUEPRINT = "blueprint";
    private static final String CATEGORY_PROVIDER = "provider";
    private static final String CATEGORY_SYSTEM = "system";

    private static final String NAME_MAX_BLUEPRINT_SIZE = "maxSize";
    private static final String NAME_MAX_PROVIDER_RADIUS = "maxProviderRadius";
    private static final String NAME_MAX_CHUNK_OPERATIONS_PER_TICK = "maxChunkOperationsPerTick";
    private static final String NAME_MAX_WORLD_OPERATIONS_PER_TICK = "maxWorldOperationsPerTick";

    private static final String COMMENT_MAX_BLUEPRINT_SIZE =
            "The maximum size of a blueprint in any dimension.";
    private static final String COMMENT_MAX_PROVIDER_ITEM_RADIUS =
            "The radius around providers in which players must be for them to count.";
    private static final String COMMENT_MAX_CHUNK_OPERATIONS_PER_TICK =
            "The maximum number of blocks to deserialize (place) per tick per chunk.";
    private static final String COMMENT_MAX_WORLD_OPERATIONS_PER_TICK =
            "The maximum number of blocks to deserialize (place) per tick per world.";

    // --------------------------------------------------------------------- //

    public static boolean isBlacklisted(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location == null || blockBlacklist.contains(location);
    }

    public static boolean isWhitelisted(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location != null && tileEntityWhitelist.contains(location);
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

    public static void load(final File configFile) {
        final Configuration config = new Configuration(configFile, CONFIG_VERSION);

        config.load();

        maxBlueprintSize = config.getInt(
                NAME_MAX_BLUEPRINT_SIZE, CATEGORY_BLUEPRINT,
                maxBlueprintSize, 1, 255, COMMENT_MAX_BLUEPRINT_SIZE);

        maxProviderRadius = config.getInt(
                NAME_MAX_PROVIDER_RADIUS, CATEGORY_PROVIDER,
                maxProviderRadius, 1, 1000, COMMENT_MAX_PROVIDER_ITEM_RADIUS);

        maxChunkOperationsPerTick = config.getInt(
                NAME_MAX_CHUNK_OPERATIONS_PER_TICK, CATEGORY_SYSTEM,
                maxChunkOperationsPerTick, 1, 64, COMMENT_MAX_CHUNK_OPERATIONS_PER_TICK);
        maxWorldOperationsPerTick = config.getInt(
                NAME_MAX_WORLD_OPERATIONS_PER_TICK, CATEGORY_SYSTEM,
                maxWorldOperationsPerTick, 1, 1000, COMMENT_MAX_WORLD_OPERATIONS_PER_TICK);

        if (config.hasChanged()) {
            config.save();
        }

        loadJason(configFile.getParent());
    }

    // --------------------------------------------------------------------- //

    private static void loadJason(final String configDirectory) {
        final Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).create();

        loadJason(blockBlacklist, "blacklist.json", configDirectory, gson);
        loadJason(tileEntityWhitelist, "whitelist.json", configDirectory, gson);
        loadJason(attachedBlocks, "attached.json", configDirectory, gson);
        loadJason(blockToBlockMapping, "block_mapping.json", configDirectory, gson);
        loadJason(blockToItemMapping, "item_mapping.json", configDirectory, gson);
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
            try {
                value = gson.fromJson(FileUtils.readFileToString(path), type);
            } catch (IOException e) {
                Architect.getLog().warn("Failed reading " + path.toString() + ".", e);
            }
        } else {
            Architect.getLog().info("Writing defaults for " + fileName + ".");
            try (final InputStream stream = Settings.class.getResourceAsStream("/assets/" + API.MOD_ID + "/config/" + fileName)) {
                value = gson.fromJson(new InputStreamReader(stream), type);
            } catch (IOException e) {
                Architect.getLog().warn("Failed loading defaults for " + fileName + ".", e);
                e.printStackTrace();
            }
        }
        try {
            FileUtils.writeStringToFile(path, gson.toJson(value));
        } catch (final IOException e) {
            Architect.getLog().warn("Failed writing " + path.toString() + ".", e);
        }
        return value;
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
