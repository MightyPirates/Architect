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
     * The radius around the player in which to deserialize (place) blocks.
     */
    public static int maxProviderEntityRadius = 32;

    /**
     * The radius around the player in which to deserialize (place) blocks.
     */
    public static int maxProviderEntityOperationsPerTick = 5;

    /**
     * The maximum number of blocks to deserialize (place) per tick per world.
     */
    public static int maxWorldOperationsPerTick = 50;

    /**
     * The distance at which the free-aim pointer is positioned (e.g. in sketch
     * when finishing a ranged selection in mid-air).
     */
    public static float freeAimDistance = 4;

    // --------------------------------------------------------------------- //

    private static final String CONFIG_VERSION = "1";

    private static final String CATEGORY_BLUEPRINT = "blueprint";
    private static final String CATEGORY_PROVIDER = "provider";
    private static final String CATEGORY_SYSTEM = "system";

    private static final String NAME_MAX_BLUEPRINT_SIZE = "maxSize";
    private static final String NAME_MAX_PROVIDER_ITEM_RADIUS = "maxProviderItemRadius";
    private static final String NAME_MAX_OPERATIONS_PER_TICK = "maxOperationsPerTick";

    private static final String COMMENT_MAX_BLUEPRINT_SIZE = "The maximum size of a blueprint in any dimension.";
    private static final String COMMENT_MAX_PROVIDER_ITEM_RADIUS = "The radius around players with a provider in their inventory in which to deserialize (place) blocks.";
    private static final String COMMENT_MAX_OPERATIONS_PER_TICK = "The maximum number of blocks to deserialize (place) per tick per world.";

    // --------------------------------------------------------------------- //

    static void load(final File configFile) {
        final Configuration config = new Configuration(configFile, CONFIG_VERSION);

        config.load();

        maxBlueprintSize = config.getInt(
                NAME_MAX_BLUEPRINT_SIZE, CATEGORY_BLUEPRINT,
                maxBlueprintSize, 1, 255, COMMENT_MAX_BLUEPRINT_SIZE);

        maxProviderEntityRadius = config.getInt(
                NAME_MAX_PROVIDER_ITEM_RADIUS, CATEGORY_PROVIDER,
                maxProviderEntityRadius, 1, 1000, COMMENT_MAX_PROVIDER_ITEM_RADIUS);
        maxProviderEntityOperationsPerTick = config.getInt(
                NAME_MAX_OPERATIONS_PER_TICK, CATEGORY_PROVIDER,
                maxProviderEntityOperationsPerTick, 1, 64, COMMENT_MAX_OPERATIONS_PER_TICK);

        maxWorldOperationsPerTick = config.getInt(
                NAME_MAX_OPERATIONS_PER_TICK, CATEGORY_SYSTEM,
                maxWorldOperationsPerTick, 1, 1000, COMMENT_MAX_OPERATIONS_PER_TICK);

        if (config.hasChanged()) {
            config.save();
        }
    }

    // --------------------------------------------------------------------- //

    private Settings() {
    }
}
