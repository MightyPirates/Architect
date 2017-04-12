package li.cil.architect.common.command;

import li.cil.architect.common.config.Jasons;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class SubCommandMapToItem extends AbstractMappingCommand {
    @Override
    public String getName() {
        return "mapToItem";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("m2i");
    }

    @Nullable
    @Override
    protected ResourceLocation getMapping(final ResourceLocation location) {
        return Jasons.getItemMapping(location);
    }

    @Override
    protected boolean addMapping(final ResourceLocation location, final ResourceLocation mapping) {
        return Jasons.addItemMapping(location, mapping);
    }

    @Override
    protected boolean removeMapping(final ResourceLocation location) {
        return Jasons.removeItemMapping(location);
    }

    @Override
    protected Collection<ResourceLocation> getCandidates() {
        return Item.REGISTRY.getKeys();
    }
}
