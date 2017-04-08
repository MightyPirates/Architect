package li.cil.architect.api.blueprint;

/**
 * Values for {@link Converter#getSortIndex()} used by built-in converters.
 * <p>
 * Use these for custom converters as is, or use them as a guideline.
 */
public final class SortIndex {
    /**
     * Blocks that have no dependencies, such as cobble or dirt.
     */
    public static final int SOLID_BLOCK = 0;

    /**
     * Blocks that require a solid block below them, such as gravel or sand.
     */
    public static final int FALLING_BLOCK = 100;

    /**
     * Blocks that require a hard surface, such as levers or torches.
     */
    public static final int ATTACHED_BLOCK = 200;

    /**
     * Blocks that are fluids, such as water or lava.
     */
    public static final int FLUID_BLOCK = 300;

    // --------------------------------------------------------------------- //

    private SortIndex() {
    }
}
