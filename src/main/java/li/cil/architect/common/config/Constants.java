package li.cil.architect.common.config;

import li.cil.architect.api.API;

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
    public static final String GUI_FACTORY = "li.cil.architect.client.config.ModGuiFactoryArchitect";

    // --------------------------------------------------------------------- //
    // Block, item, entity and container names

    public static final String NAME_ITEM_SKETCH = "sketch";
    public static final String NAME_ITEM_BLUEPRINT = "blueprint";
    public static final String NAME_ITEM_PROVIDER_ITEM = "provider_item";

    // --------------------------------------------------------------------- //
    // Converter UUIDs

    public static final UUID UUID_CONVERTER_SOLID_BLOCKS = UUID.fromString("20a9cafc-d21c-49cd-9a40-f8ea34c91984");
    public static final UUID UUID_CONVERTER_FALLING_BLOCKS = UUID.fromString("74365f58-01c6-4b85-b069-e73656e57064");
    public static final UUID UUID_CONVERTER_ATTACHED_BLOCKS = UUID.fromString("856e187b-adc2-43be-8d4a-9120ce61f709");
    public static final UUID UUID_CONVERTER_TILE_ENTITY = UUID.fromString("daf02728-b4b7-4530-84ba-2fb50dcdc8b1");

    // --------------------------------------------------------------------- //
    // Tooltips

    public static final int MAX_TOOLTIP_WIDTH = 200;

    public static final String TOOLTIP_SKETCH_BOUNDS = "tooltip." + API.MOD_ID + ".sketch.bounds";
    public static final String TOOLTIP_SKETCH_CONVERT = "tooltip." + API.MOD_ID + ".sketch.convert";
    public static final String TOOLTIP_SKETCH_EMPTY = "tooltip." + API.MOD_ID + ".sketch.empty";
    public static final String TOOLTIP_BLUEPRINT = "tooltip." + API.MOD_ID + ".blueprint";
    public static final String TOOLTIP_BLUEPRINT_COSTS_HINT = "" + API.MOD_ID + ".tooltip.blueprint.costs.hint";
    public static final String TOOLTIP_BLUEPRINT_COSTS_TITLE = "tooltip." + API.MOD_ID + ".blueprint.costs.title";
    public static final String TOOLTIP_BLUEPRINT_COSTS_LINE = "tooltip." + API.MOD_ID + ".blueprint.costs.line";
    public static final String TOOLTIP_BLUEPRINT_COSTS_UNKNOWN = "tooltip." + API.MOD_ID + ".blueprint.costs.unknown";
    public static final String TOOLTIP_PROVIDER_ITEM = "tooltip." + API.MOD_ID + ".provider.item";
    public static final String TOOLTIP_PROVIDER_TARGET = "tooltip." + API.MOD_ID + ".provider.target";

    // --------------------------------------------------------------------- //
    // Config

    public static final String CONFIG_MAX_BLUEPRINT_SIZE = "config." + API.MOD_ID + ".maxBlueprintSize";
    public static final String CONFIG_MAX_PROVIDER_RADIUS = "config." + API.MOD_ID + ".maxProviderRadius";
    public static final String CONFIG_MAX_CHUNK_OPS_PER_TICK = "config." + API.MOD_ID + ".maxChunkOperationsPerTick";
    public static final String CONFIG_MAX_WORLD_OPS_PER_TICK = "config." + API.MOD_ID + ".maxWorldOperationsPerTick";
    public static final String CONFIG_BLACKLIST = "config." + API.MOD_ID + ".blacklist";
    public static final String CONFIG_WHITELIST = "config." + API.MOD_ID + ".whitelist";
    public static final String CONFIG_ATTACHED = "config." + API.MOD_ID + ".attached";

    // --------------------------------------------------------------------- //
    // Config files

    public static final String BLACKLIST_FILENAME = "blacklist.json";
    public static final String WHITELIST_FILENAME = "whitelist.json";
    public static final String ATTACHED_BLOCKS_FILENAME = "attached.json";
    public static final String BLOCK_MAPPING_FILENAME = "block_mapping.json";
    public static final String ITEM_MAPPING_FILENAME = "item_mapping.json";

    // --------------------------------------------------------------------- //
    // Commands

    public static final String COMMAND_USAGE = "commands.architect.usage";
    public static final String SUBCOMMAND_USAGE = "commands.architect.%s.usage";
    public static final String SUBCOMMAND_LIST_ADDED = "commands.architect.%s.added";
    public static final String SUBCOMMAND_LIST_REMOVED = "commands.architect.%s.removed";
    public static final String SUBCOMMAND_MAPPING_CURRENT = "commands.architect.%s.mapping";
    public static final String SUBCOMMAND_MAPPING_NO_MAPPING = "commands.architect.%s.nomapping";
    public static final String SUBCOMMAND_MAPPING_ADDED = "commands.architect.%s.added";
    public static final String SUBCOMMAND_MAPPING_REMOVED = "commands.architect.%s.removed";
    public static final String SUBCOMMAND_COPY_SUCCESS = "commands.architect.copy.success";
    public static final String SUBCOMMAND_PASTE_INVALID = "commands.architect.paste.invalid";
    public static final String SUBCOMMAND_PASTE_SUCCESS = "commands.architect.paste.success";

    // --------------------------------------------------------------------- //
    // Key bindings

    public static final String KEY_BINDINGS_CATEGORY_NAME = API.MOD_ID + " (" + MOD_NAME + ")";
    public static final String KEY_BINDINGS_BLUEPRINT_ROTATE = "key." + API.MOD_ID + ".rotateBlueprint";

    // --------------------------------------------------------------------- //

    private Constants() {
    }
}
