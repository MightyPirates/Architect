package li.cil.architect.common.config;

import li.cil.architect.common.Architect;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
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

    // --------------------------------------------------------------------- //

    private static final String CONFIG_VERSION = "1";

    private static final String CATEGORY_BLUEPRINT = "blueprint";
    private static final String CATEGORY_CONVERTER = "converter";
    private static final String CATEGORY_PROVIDER = "provider";
    private static final String CATEGORY_SYSTEM = "system";

    private static final String NAME_MAX_BLUEPRINT_SIZE = "maxSize";
    private static final String NAME_MAX_PROVIDER_RADIUS = "maxProviderRadius";
    private static final String NAME_MAX_CHUNK_OPERATIONS_PER_TICK = "maxChunkOperationsPerTick";
    private static final String NAME_MAX_WORLD_OPERATIONS_PER_TICK = "maxWorldOperationsPerTick";
    private static final String NAME_CONVERTER_BLACKLIST = "blacklist";

    private static final String COMMENT_MAX_BLUEPRINT_SIZE =
            "The maximum size of a blueprint in any dimension.";
    private static final String COMMENT_MAX_PROVIDER_ITEM_RADIUS =
            "The radius around providers in which players must be for them to count.";
    private static final String COMMENT_MAX_CHUNK_OPERATIONS_PER_TICK =
            "The maximum number of blocks to deserialize (place) per tick per chunk.";
    private static final String COMMENT_MAX_WORLD_OPERATIONS_PER_TICK =
            "The maximum number of blocks to deserialize (place) per tick per world.";
    private static final String COMMENT_CONVERTER_BLACKLIST =
            "Registry names of blocks to ignore in the built-in converters.\n" +
            "Values must be formatted as resource locations, e.g.:\n" +
            "  minecraft:iron_block";

    // --------------------------------------------------------------------- //

    public static boolean isBlacklisted(final Block block) {
        final ResourceLocation location = block.getRegistryName();
        return location == null || blockBlacklist.contains(location);
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

        if (config.hasChanged()) {
            config.save();
        }

        for (final String blacklistItem : rawBlockBlacklist) {
            try {
                final ResourceLocation location = new ResourceLocation(blacklistItem);
                blockBlacklist.add(location);
            } catch (final Throwable t) {
                Architect.getLog().warn("Failed parsing simple block converter blacklist entry '" + blacklistItem + "'.", t);
            }
        }
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
