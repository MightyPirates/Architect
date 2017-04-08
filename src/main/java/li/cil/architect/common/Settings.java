package li.cil.architect.common;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * User configurable stuff via config file.
 */
public final class Settings {
    /**
     * The maximum size of a blueprint in any dimension.
     */
    public static int maxBlueprintSize = 16;

    /**
     * The radius around providers in which players must be for them to count.
     */
    public static int maxProviderRadius = 32;

    /**
     * The maximum number of blocks to deserialize (place) per tick per chunk.
     */
    public static int maxChunkOperationsPerTick = 1;

    /**
     * The maximum number of blocks to deserialize (place) per tick per world.
     */
    public static int maxWorldOperationsPerTick = 50;

    /**
     * The distance at which the free-aim pointer is positioned (e.g. in sketch
     * when finishing a ranged selection in mid-air). This can be changed
     * during runtime.
     */
    public static float freeAimDistance = 4;

    // --------------------------------------------------------------------- //

    private static final String CONFIG_VERSION = "1";

    private static final String CATEGORY_BLUEPRINT = "blueprint";
    private static final String CATEGORY_PROVIDER = "provider";
    private static final String CATEGORY_SYSTEM = "system";

    private static final String NAME_MAX_BLUEPRINT_SIZE = "maxSize";
    private static final String NAME_MAX_PROVIDER_RADIUS = "maxProviderRadius";
    private static final String NAME_MAX_CHUNK_OPERATIONS_PER_TICK = "maxChunkOperationsPerTick";
    private static final String NAME_MAX_WORLD_OPERATIONS_PER_TICK = "maxWorldOperationsPerTick";

    private static final String COMMENT_MAX_BLUEPRINT_SIZE = "The maximum size of a blueprint in any dimension.";
    private static final String COMMENT_MAX_PROVIDER_ITEM_RADIUS = "The radius around providers in which players must be for them to count.";
    private static final String COMMENT_MAX_CHUNK_OPERATIONS_PER_TICK = "The maximum number of blocks to deserialize (place) per tick per chunk.";
    private static final String COMMENT_MAX_WORLD_OPERATIONS_PER_TICK = "The maximum number of blocks to deserialize (place) per tick per world.";

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

        if (config.hasChanged()) {
            config.save();
        }
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
