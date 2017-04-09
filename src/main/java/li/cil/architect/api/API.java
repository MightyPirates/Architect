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

    public static CreativeTab creativeTab;

    // Set in pre-init, prefer using static entry point classes instead.
    public static ConverterAPI converterAPI;

    private API() {
    }
}
