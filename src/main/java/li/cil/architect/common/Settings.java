package li.cil.architect.common;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

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

    public static String[] simpleBlockBlacklist = {};

    // --------------------------------------------------------------------- //

    private static final String CONFIG_VERSION = "1";

    private static final String CATEGORY_BLUEPRINT = "blueprint";
    private static final String CATEGORY_CONVERTER_SIMPLE = "converter.simple";
    private static final String CATEGORY_PROVIDER = "provider";
    private static final String CATEGORY_SYSTEM = "system";

    private static final String NAME_MAX_BLUEPRINT_SIZE = "maxSize";
    private static final String NAME_MAX_PROVIDER_RADIUS = "maxProviderRadius";
    private static final String NAME_MAX_CHUNK_OPERATIONS_PER_TICK = "maxChunkOperationsPerTick";
    private static final String NAME_MAX_WORLD_OPERATIONS_PER_TICK = "maxWorldOperationsPerTick";
    private static final String NAME_SIMPLE_BLOCK_BLACKLIST = "simpleBlockBlacklist";

    private static final String COMMENT_MAX_BLUEPRINT_SIZE =
            "The maximum size of a blueprint in any dimension.";
    private static final String COMMENT_MAX_PROVIDER_ITEM_RADIUS =
            "The radius around providers in which players must be for them to count.";
    private static final String COMMENT_MAX_CHUNK_OPERATIONS_PER_TICK =
            "The maximum number of blocks to deserialize (place) per tick per chunk.";
    private static final String COMMENT_MAX_WORLD_OPERATIONS_PER_TICK =
            "The maximum number of blocks to deserialize (place) per tick per world.";
    private static final String COMMENT_SIMPLE_BLOCK_BLACKLIST =
            "Registry names of blocks to ignore in the simple block converter.\n" +
            "The simple block converter will by default work for all blocks that\n" +
            "do not have a TileEntity, and that have a corresponding ItemBlock.\n" +
            "However, it does not take metadata into account when looking for items\n" +
            "when deserializing a block (to 'pay' for it), and as such there may be\n" +
            "cases where it would try to use the wrong item in deserialization. For\n" +
            "those cases, add the block's registry name in this list. Or throw it in\n" +
            "if you just don't want the block to be copyable by blueprints.\n" +
            "Values must be formatted as resource locations, e.g.:\n" +
            "  minecraft:iron_block";

    // --------------------------------------------------------------------- //

    static void load(final File configFile) {
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

        simpleBlockBlacklist = config.getStringList(
                NAME_SIMPLE_BLOCK_BLACKLIST, CATEGORY_CONVERTER_SIMPLE,
                simpleBlockBlacklist, COMMENT_SIMPLE_BLOCK_BLACKLIST);

        if (config.hasChanged()) {
            config.save();
        }
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
