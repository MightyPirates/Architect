package li.cil.architect.common.config;

import li.cil.architect.common.Architect;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
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
     * A list of registry names of blocks to ignore in built-in converters.
     */
    public static String[] rawBlockBlacklist = {};

    /**
     * The parsed list of blocks to ignore in built-in converters.
     */
    public static final Set<ResourceLocation> blockBlacklist = new HashSet<>();

    /**
     * A list of registry names of blocks that depend on other blocks. This is
     * merely intended for simple cases, such as torches and levers.
     */
    public static String[] rawAttachedBlocks = {
            "minecraft:grass",
            "minecraft:sapling",
            "minecraft:leaves",
            "minecraft:leaves2",
            "minecraft:tallgrass",
            "minecraft:deadbush",
            "minecraft:yellow_flower",
            "minecraft:red_flower",
            "minecraft:brown_mushroom",
            "minecraft:red_mushroom",
            "minecraft:torch",
            "minecraft:wheat",
            "minecraft:ladder",
            "minecraft:rail",
            "minecraft:lever",
            "minecraft:stone_pressure_plate",
            "minecraft:wooden_pressure_plate",
            "minecraft:redstone_torch",
            "minecraft:stone_button",
            "minecraft:snow_layer",
            "minecraft:cactus",
            "minecraft:reeds",
            "minecraft:cake",
            "minecraft:unpowered_repeater",
            "minecraft:pumpkin_stem",
            "minecraft:melon_stem",
            "minecraft:vine",
            "minecraft:nether_wart",
            "minecraft:cocoa",
            "minecraft:tripwire_hook",
            "minecraft:tripwire",
            "minecraft:carrots",
            "minecraft:potatoes",
            "minecraft:wooden_button",
            "minecraft:light_weighted_pressure_plate",
            "minecraft:heavy_weighted_pressure_plate",
            "minecraft:unpowered_comparator",
            "minecraft:activator_rail",
            "minecraft:end_rod",
            "minecraft:chorus_plant",
            "minecraft:chorus_flower",
            "minecraft:beetroots"
    };

    /**
     * The parsed list of blocks to ignore in built-in converters.
     */
    public static final Set<ResourceLocation> attachedBlocks = new HashSet<>();

    // --------------------------------------------------------------------- //

    private static final String CONFIG_VERSION = "1";

    private static final String CATEGORY_BLUEPRINT = "blueprint";
    private static final String CATEGORY_CONVERTER = "converter";
    private static final String CATEGORY_PROVIDER = "provider";
    private static final String CATEGORY_SYSTEM = "system";

    private static final String NAME_CONVERTER_BLACKLIST = "blacklist";
    private static final String NAME_CONVERTER_ATTACHED_BLOCKS = "attachedBlocks";
    private static final String NAME_MAX_BLUEPRINT_SIZE = "maxSize";
    private static final String NAME_MAX_PROVIDER_RADIUS = "maxProviderRadius";
    private static final String NAME_MAX_CHUNK_OPERATIONS_PER_TICK = "maxChunkOperationsPerTick";
    private static final String NAME_MAX_WORLD_OPERATIONS_PER_TICK = "maxWorldOperationsPerTick";

    private static final String COMMENT_CONVERTER_BLACKLIST =
            "Registry names of blocks to ignore in the built-in converters.\n" +
            "Values must be formatted as resource locations, e.g.:\n" +
            "  minecraft:iron_block";
    private static final String COMMENT_CONVERTER_ATTACHED_BLOCKS =
            "Registry names of blocks that depend on other blocks for placement.\n" +
            "This is merely intended for simple cases, such as torches and levers.\n" +
            "Values must be formatted as resource locations, e.g.:\n" +
            "  minecraft:iron_block";
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

    public static boolean isAttachedBlock(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location != null && attachedBlocks.contains(location);
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

        rawBlockBlacklist = config.getStringList(
                NAME_CONVERTER_BLACKLIST, CATEGORY_CONVERTER,
                rawBlockBlacklist, COMMENT_CONVERTER_BLACKLIST);
        rawAttachedBlocks = config.getStringList(
                NAME_CONVERTER_ATTACHED_BLOCKS, CATEGORY_CONVERTER,
                rawAttachedBlocks, COMMENT_CONVERTER_ATTACHED_BLOCKS);

        if (config.hasChanged()) {
            config.save();
        }

        parseResourceLocations(rawBlockBlacklist, blockBlacklist);
        parseResourceLocations(rawAttachedBlocks, attachedBlocks);
    }

    // --------------------------------------------------------------------- //

    private static void parseResourceLocations(final String[] names, final Collection<ResourceLocation> locations) {
        for (final String blacklistItem : names) {
            try {
                final ResourceLocation location = new ResourceLocation(blacklistItem);
                locations.add(location);
            } catch (final Throwable t) {
                Architect.getLog().warn("Failed parsing block resource location '" + blacklistItem + "'.", t);
            }
        }
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
