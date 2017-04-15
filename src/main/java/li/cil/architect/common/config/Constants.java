package li.cil.architect.common.config;

import java.util.UUID;

/**
 * Collection of constants used throughout the mod.
 */
public final class Constants {
    // --------------------------------------------------------------------- //
    // Mod data

    public static final String MOD_NAME = "Architect";
    public static final String PROXY_CLIENT = "li.cil.architect.client.ProxyClient";
    public static final String PROXY_COMMON = "li.cil.architect.common.ProxyCommon";

    // --------------------------------------------------------------------- //
    // Block, item, entity and container names

    public static final String NAME_ITEM_SKETCH = "sketch";
    public static final String NAME_ITEM_BLUEPRINT = "blueprint";
    public static final String NAME_ITEM_PROVIDER_ITEM = "provider_item";

    // --------------------------------------------------------------------- //
    // Converter UUIDs

    public static final UUID UUID_CONVERTER_SIMPLE_BLOCKS = UUID.fromString("20a9cafc-d21c-49cd-9a40-f8ea34c91984");
    public static final UUID UUID_CONVERTER_FALLING_BLOCKS = UUID.fromString("74365f58-01c6-4b85-b069-e73656e57064");
    public static final UUID UUID_CONVERTER_TILE_ENTITY = UUID.fromString("daf02728-b4b7-4530-84ba-2fb50dcdc8b1");

    // --------------------------------------------------------------------- //
    // Tooltips

    public static final int MAX_TOOLTIP_WIDTH = 200;

    public static final String TOOLTIP_SKETCH_BOUNDS = "tooltip.architect.sketch.bounds";
    public static final String TOOLTIP_SKETCH_CONVERT = "tooltip.architect.sketch.convert";
    public static final String TOOLTIP_SKETCH_EMPTY = "tooltip.architect.sketch.empty";
    public static final String TOOLTIP_BLUEPRINT = "tooltip.architect.blueprint";
    public static final String TOOLTIP_BLUEPRINT_COSTS_HINT = "tooltip.architect.blueprint.costs.hint";
    public static final String TOOLTIP_BLUEPRINT_COSTS_TITLE = "tooltip.architect.blueprint.costs.title";
    public static final String TOOLTIP_BLUEPRINT_COSTS_TITLE_PAGED = "tooltip.architect.blueprint.costs.title.paged";
    public static final String TOOLTIP_BLUEPRINT_COSTS_LINE = "tooltip.architect.blueprint.costs.line";
    public static final String TOOLTIP_BLUEPRINT_COSTS_UNKNOWN = "tooltip.architect.blueprint.costs.unknown";
    public static final String TOOLTIP_PROVIDER_ITEM = "tooltip.architect.provider.item";
    public static final String TOOLTIP_PROVIDER_TARGET = "tooltip.architect.provider.target";

    // --------------------------------------------------------------------- //
    // Config

    public static final String CONFIG_MAX_BLUEPRINT_SIZE = "config.architect.maxBlueprintSize";
    public static final String CONFIG_MAX_PROVIDER_RADIUS = "config.architect.maxProviderRadius";
    public static final String CONFIG_MAX_CHUNK_OPS_PER_TICK = "config.architect.maxChunkOperationsPerTick";
    public static final String CONFIG_MAX_WORLD_OPS_PER_TICK = "config.architect.maxWorldOperationsPerTick";
    public static final String CONFIG_USE_ENERGY = "config.architect.use_energy";
    public static final String CONFIG_BLACKLIST = "config.architect.blacklist";

    // --------------------------------------------------------------------- //
    // Config files

    public static final String BLACKLIST_FILENAME = "blacklist.json";
    public static final String WHITELIST_FILENAME = "whitelist.json";
    public static final String BLOCK_MAPPING_FILENAME = "block_mapping.json";
    public static final String ITEM_MAPPING_FILENAME = "item_mapping.json";

    // --------------------------------------------------------------------- //
    // Commands

    public static final String COMMAND_USAGE = "commands.architect.usage";
    public static final String COMMAND_SUB_USAGE = "commands.architect.%s.usage";
    public static final String COMMAND_LIST_ADDED = "commands.architect.%s.added";
    public static final String COMMAND_LIST_REMOVED = "commands.architect.%s.removed";
    public static final String COMMAND_MAPPING_CURRENT = "commands.architect.%s.mapping";
    public static final String COMMAND_MAPPING_NO_MAPPING = "commands.architect.%s.nomapping";
    public static final String COMMAND_MAPPING_ADDED = "commands.architect.%s.added";
    public static final String COMMAND_MAPPING_REMOVED = "commands.architect.%s.removed";
    public static final String COMMAND_COPY_SUCCESS = "commands.architect.copy.success";
    public static final String COMMAND_PASTE_INVALID = "commands.architect.paste.invalid";
    public static final String COMMAND_PASTE_SUCCESS = "commands.architect.paste.success";
    public static final String COMMAND_NBT_INVALID_BLOCK = "commands.architect.nbt.invalid_block";
    public static final String COMMAND_NBT_NO_TILE_ENTITY = "commands.architect.nbt.no_tile_entity";
    public static final String COMMAND_NBT_ERROR = "commands.architect.nbt.error";
    public static final String COMMAND_NBT_SUCCESS = "commands.architect.nbt.success";
    public static final String COMMAND_RELOAD_SUCCESS = "commands.architect.reload.success";

    // --------------------------------------------------------------------- //
    // Messages

    public static final String MESSAGE_PLACEMENT_CANCELED = "message.architect.placement.canceled";
    public static final String MESSAGE_PLACEMENT_NOT_ENOUGH_ENERGY = "message.architect.placement.not_enough_energy";

    // --------------------------------------------------------------------- //
    // Key bindings

    public static final String KEY_BINDINGS_CATEGORY_NAME = MOD_NAME;
    public static final String KEY_BINDINGS_BLUEPRINT_ROTATE = "key.architect.rotateBlueprint";

    // --------------------------------------------------------------------- //
    // Gameplay

    public static final int ENERGY_PER_BLOCK = 10;
    public static final float EXHAUSTION_PER_BLOCK = 0.005f;

    // --------------------------------------------------------------------- //

    private Constants() {
    }
}
