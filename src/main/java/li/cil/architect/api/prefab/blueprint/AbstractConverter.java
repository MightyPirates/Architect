package li.cil.architect.api.prefab.blueprint;

import li.cil.architect.api.blueprint.Converter;

import java.util.UUID;

/**
 * Base implementation of a converter.
 */
public abstract class AbstractConverter implements Converter {
    // --------------------------------------------------------------------- //
    // Computed data

    private final UUID uuid;

    // --------------------------------------------------------------------- //

    protected AbstractConverter(final UUID uuid) {
        this.uuid = uuid;
    }

    // --------------------------------------------------------------------- //
    // Converter

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
