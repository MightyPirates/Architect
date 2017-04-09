package li.cil.architect.api.prefab.blueprint;

import li.cil.architect.api.blueprint.Converter;
import li.cil.architect.api.blueprint.SortIndex;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.UUID;

/**
 * Base implementation of a converter.
 */
public abstract class AbstractConverter implements Converter {
    // --------------------------------------------------------------------- //
    // Computed data

    private final UUID uuid;
    private final int sortIndex;

    // --------------------------------------------------------------------- //

    protected AbstractConverter(final UUID uuid, final int sortIndex) {
        this.uuid = uuid;
        this.sortIndex = sortIndex;
    }

    protected AbstractConverter(final UUID uuid) {
        this(uuid, SortIndex.SOLID_BLOCK);
    }

    // --------------------------------------------------------------------- //
    // Converter

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public int getSortIndex(final NBTBase data) {
        return sortIndex;
    }

    @Override
    public Iterable<ItemStack> getItemCosts(final NBTBase data) {
        return Collections.emptyList();
    }

    @Override
    public Iterable<FluidStack> getFluidCosts(final NBTBase data) {
        return Collections.emptyList();
    }
}
