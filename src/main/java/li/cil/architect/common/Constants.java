package li.cil.architect.common;

import li.cil.architect.api.API;

import java.util.UUID;

/**
 * Collection of constants used throughout the mod.
 */
public final class Constants {
    // --------------------------------------------------------------------- //
    // Mod data

    static final String PROXY_CLIENT = "li.cil.architect.client.ProxyClient";
    static final String PROXY_COMMON = "li.cil.architect.common.ProxyCommon";

    // --------------------------------------------------------------------- //
    // Block, item, entity and container names

    public static final String NAME_ITEM_SKETCH = "sketch";
    public static final String NAME_ITEM_BLUEPRINT = "blueprint";
    public static final String NAME_ITEM_PROVIDER = "provider";

    // --------------------------------------------------------------------- //
    // Converter UUIDs

    static final UUID UUID_CONVERTER_GENERIC = UUID.fromString("20a9cafc-d21c-49cd-9a40-f8ea34c91984");

    // --------------------------------------------------------------------- //
    // Tooltips

    public static final int MAX_TOOLTIP_WIDTH = 200;

    public static final String TOOLTIP_SKETCH_BOUNDS = "architect.tooltip.sketch.bounds";
    public static final String TOOLTIP_SKETCH_CONVERT = "architect.tooltip.sketch.convert";
    public static final String TOOLTIP_SKETCH_EMPTY = "architect.tooltip.sketch.empty";
    public static final String TOOLTIP_BLUEPRINT = "architect.tooltip.blueprint";
    public static final String TOOLTIP_BLUEPRINT_COSTS_HINT = "architect.tooltip.blueprint.costs.hint";
    public static final String TOOLTIP_BLUEPRINT_COSTS_TITLE = "architect.tooltip.blueprint.costs.title";
    public static final String TOOLTIP_BLUEPRINT_COSTS_LINE = "architect.tooltip.blueprint.costs.line";
    public static final String TOOLTIP_BLUEPRINT_COSTS_UNKNOWN = "architect.tooltip.blueprint.costs.unknown";
    public static final String TOOLTIP_PROVIDER = "architect.tooltip.provider";
    public static final String TOOLTIP_PROVIDER_TARGET = "architect.tooltip.target";
    public static final String KEY_BINDINGS_CATEGORY_NAME = API.MOD_ID + " (" + Architect.MOD_NAME + ")";
    public static final String KEY_BINDINGS_BLUEPRINT_ROTATE = "key.architect.rotateBlueprint";

    // --------------------------------------------------------------------- //

    private Constants() {
    }
}
