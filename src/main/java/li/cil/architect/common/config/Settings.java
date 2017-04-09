package li.cil.architect.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import li.cil.architect.api.API;
import li.cil.architect.common.Architect;
import li.cil.architect.common.json.ResourceLocationAdapter;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.Arrays;
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
     * The parsed list of blocks to ignore in built-in converters.
     */
    private static Set<ResourceLocation> blockBlacklist = new HashSet<>();

    /**
     * The parsed list of blocks with tile entities allowed to be converted by
     * built-in converters.
     */
    private static Set<ResourceLocation> tileEntityWhitelist = new HashSet<>(Arrays.asList(new ResourceLocation[]{
            new ResourceLocation("minecraft:beacon"),
            new ResourceLocation("minecraft:brewing_stand"),
            new ResourceLocation("minecraft:chest"),
            new ResourceLocation("minecraft:daylight_detector"),
            new ResourceLocation("minecraft:daylight_detector_inverted"),
            new ResourceLocation("minecraft:dispenser"),
            new ResourceLocation("minecraft:enchanting_table"),
            new ResourceLocation("minecraft:flower_pot"),
            new ResourceLocation("minecraft:furnace"),
            new ResourceLocation("minecraft:lit_furnace"),
            new ResourceLocation("minecraft:hopper"),
            new ResourceLocation("minecraft:jukebox"),
            new ResourceLocation("minecraft:noteblock"),
            new ResourceLocation("minecraft:piston"),
            new ResourceLocation("minecraft:sticky_piston"),
            new ResourceLocation("minecraft:unpowered_comparator"),
            new ResourceLocation("minecraft:powered_comparator"),
            new ResourceLocation("minecraft:white_shulker_box"),
            new ResourceLocation("minecraft:orange_shulker_box"),
            new ResourceLocation("minecraft:magenta_shulker_box"),
            new ResourceLocation("minecraft:light_blue_shulker_box"),
            new ResourceLocation("minecraft:yellow_shulker_box"),
            new ResourceLocation("minecraft:lime_shulker_box"),
            new ResourceLocation("minecraft:pink_shulker_box"),
            new ResourceLocation("minecraft:gray_shulker_box"),
            new ResourceLocation("minecraft:silver_shulker_box"),
            new ResourceLocation("minecraft:cyan_shulker_box"),
            new ResourceLocation("minecraft:purple_shulker_box"),
            new ResourceLocation("minecraft:blue_shulker_box"),
            new ResourceLocation("minecraft:brown_shulker_box"),
            new ResourceLocation("minecraft:green_shulker_box"),
            new ResourceLocation("minecraft:red_shulker_box"),
            new ResourceLocation("minecraft:black_shulker_box")
    }));

    /**
     * The parsed list of blocks to ignore in built-in converters.
     */
    private static Set<ResourceLocation> attachedBlocks = new HashSet<>(Arrays.asList(new ResourceLocation[]{
            new ResourceLocation("minecraft:grass"),
            new ResourceLocation("minecraft:sapling"),
            new ResourceLocation("minecraft:leaves"),
            new ResourceLocation("minecraft:leaves2"),
            new ResourceLocation("minecraft:tallgrass"),
            new ResourceLocation("minecraft:deadbush"),
            new ResourceLocation("minecraft:yellow_flower"),
            new ResourceLocation("minecraft:red_flower"),
            new ResourceLocation("minecraft:brown_mushroom"),
            new ResourceLocation("minecraft:red_mushroom"),
            new ResourceLocation("minecraft:torch"),
            new ResourceLocation("minecraft:wheat"),
            new ResourceLocation("minecraft:ladder"),
            new ResourceLocation("minecraft:rail"),
            new ResourceLocation("minecraft:lever"),
            new ResourceLocation("minecraft:stone_pressure_plate"),
            new ResourceLocation("minecraft:wooden_pressure_plate"),
            new ResourceLocation("minecraft:redstone_torch"),
            new ResourceLocation("minecraft:stone_button"),
            new ResourceLocation("minecraft:snow_layer"),
            new ResourceLocation("minecraft:cactus"),
            new ResourceLocation("minecraft:reeds"),
            new ResourceLocation("minecraft:cake"),
            new ResourceLocation("minecraft:powered_repeater"),
            new ResourceLocation("minecraft:unpowered_repeater"),
            new ResourceLocation("minecraft:pumpkin_stem"),
            new ResourceLocation("minecraft:melon_stem"),
            new ResourceLocation("minecraft:vine"),
            new ResourceLocation("minecraft:nether_wart"),
            new ResourceLocation("minecraft:cocoa"),
            new ResourceLocation("minecraft:tripwire_hook"),
            new ResourceLocation("minecraft:tripwire"),
            new ResourceLocation("minecraft:carrots"),
            new ResourceLocation("minecraft:potatoes"),
            new ResourceLocation("minecraft:wooden_button"),
            new ResourceLocation("minecraft:light_weighted_pressure_plate"),
            new ResourceLocation("minecraft:heavy_weighted_pressure_plate"),
            new ResourceLocation("minecraft:powered_comparator"),
            new ResourceLocation("minecraft:unpowered_comparator"),
            new ResourceLocation("minecraft:activator_rail"),
            new ResourceLocation("minecraft:end_rod"),
            new ResourceLocation("minecraft:chorus_plant"),
            new ResourceLocation("minecraft:chorus_flower"),
            new ResourceLocation("minecraft:beetroots")
    }));

    private static Map<ResourceLocation, ResourceLocation> blockToItemMapping = new HashMap<>();

    private static Map<ResourceLocation, ResourceLocation> blockToBlockMapping = new HashMap<>();

    static {
        addBlockMapping("minecraft:lit_furnace", "minecraft:furnace");
        addBlockMapping("minecraft:lit_redstone_lamp", "minecraft:redstone_lamp");
        addBlockMapping("minecraft:lit_redstone_ore", "minecraft:redstone_ore");
        addBlockMapping("minecraft:powered_comparator", "minecraft:unpowered_comparator");
        addBlockMapping("minecraft:powered_repeater", "minecraft:unpowered_repeater");
        addBlockMapping("minecraft:unlit_redstone_torch", "minecraft:redstone_torch");

        addItemMapping("minecraft:brewing_stand", "minecraft:brewing_stand");
        addItemMapping("minecraft:daylight_detector_inverted", "minecraft:daylight_detector");
        addItemMapping("minecraft:flower_pot", "minecraft:flower_pot");
        addItemMapping("minecraft:unpowered_comparator", "minecraft:comparator");
        addItemMapping("minecraft:unpowered_repeater", "minecraft:repeater");
        addItemMapping("minecraft:redstone_wire", "minecraft:redstone");
    }

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

        final Gson gson = new GsonBuilder().
                setPrettyPrinting().
                registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter()).create();

        final Type resourceLocationSetType = new TypeToken<HashSet<ResourceLocation>>(){}.getType();
        final Type resourceLocationMapType = new TypeToken<HashMap<ResourceLocation, ResourceLocation>>(){}.getType();

        blockBlacklist = deserialize("blacklist.json", blockBlacklist, configFile, gson, resourceLocationSetType);
        tileEntityWhitelist = deserialize("whitelist.json", tileEntityWhitelist, configFile, gson, resourceLocationSetType);
        attachedBlocks = deserialize("attached.json", attachedBlocks, configFile, gson, resourceLocationSetType);
        blockToItemMapping = deserialize("item_mapping.json", blockToItemMapping, configFile, gson, resourceLocationMapType);
        blockToBlockMapping = deserialize("block_mapping.json", blockToBlockMapping, configFile, gson, resourceLocationMapType);
    }

    // --------------------------------------------------------------------- //

    private static void addBlockMapping(final String key, final String value) {
        blockToBlockMapping.put(new ResourceLocation(key), new ResourceLocation(value));
    }

    private static void addItemMapping(final String key, final String value) {
        blockToItemMapping.put(new ResourceLocation(key), new ResourceLocation(value));
    }

    private static <T> T deserialize(final String fileName, T value, final File basePath, final Gson gson, final Type type) {
        final File path = Paths.get(basePath.getParent(), API.MOD_ID, fileName).toFile();
        if (path.exists()) {
            try {
                value = gson.fromJson(FileUtils.readFileToString(path), type);
            } catch (IOException e) {
                Architect.getLog().warn("Failed reading " + path.toString() + ".", e);
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
