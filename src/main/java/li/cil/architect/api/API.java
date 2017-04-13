package li.cil.architect.api;

import li.cil.architect.api.detail.ConverterAPI;
import li.cil.architect.common.api.CreativeTab;

/**
 * Glue / actual references for the Architect API.
 */
public final class API {
    /**
     * The ID of the mod, i.e. the internal string it is identified by.
     */
    public static final String MOD_ID = "architect";

    /**
     * The current version of the mod.
     */
    public static final String MOD_VERSION = "@VERSION@";

    // --------------------------------------------------------------------- //
    // IMC Messages

    /**
     * Blacklist command, add a block to the blacklist so it is not handled by
     * any of the built-in converters. Takes a ResourceLocation value.
     */
    public static final String IMC_BLACKLIST = "blacklist";

    /**
     * Whitelist a block and provide some additional processing information.
     * Takes a tag compound with a 'name' string entry that can be used to
     * construct a ResourceLocation, an optional 'sortIndex' integer and an
     * optional tag compound 'nbtFilter'. The filter must be structured the same
     * as an entry in the <code>whitelist.json</code> file, i.e. keys for
     * allowed keys in a filtered tag compound, with arbitrary values, or
     * another tag compound as value for nested filtering.
     */
    public static final String IMC_WHITELIST = "whitelist";

    /**
     * Add a block to block mapping, replacing a block with another in
     * blueprints. Takes a tag compound with a 'from' and a 'to' entry, both
     * of which must be strings that can be used to construct ResourceLocations.
     */
    public static final String IMC_MAP_TO_BLOCK = "mapToBlock";

    /**
     * Add a block to item mapping, defining which item to associate with a
     * serialized block when computing required materials. Takes a tag compound
     * with a 'from' and a 'to' entry, both of which must be strings that can
     * be used to construct ResourceLocations.
     */
    public static final String IMC_MAP_TO_ITEM = "mapToItem";

    // --------------------------------------------------------------------- //

    public static CreativeTab creativeTab;

    // Set in pre-init, prefer using static entry point classes instead.
    public static ConverterAPI converterAPI;

    private API() {
    }
}
