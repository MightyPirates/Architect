package li.cil.architect.common.command;

import li.cil.architect.common.config.Jasons;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class SubCommandMapToBlock extends AbstractMappingCommand {
    @Override
    public String getName() {
        return "mapToBlock";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("m2b");
    }

    @Nullable
    @Override
    protected ResourceLocation getMapping(final ResourceLocation location) {
        return Jasons.getBlockMapping(location);
    }

    @Override
    protected boolean addMapping(final ResourceLocation location, final ResourceLocation mapping) {
        return Jasons.addBlockMapping(location, mapping);
    }

    @Override
    protected boolean removeMapping(final ResourceLocation location) {
        return Jasons.removeBlockMapping(location);
    }

    @Override
    protected Collection<ResourceLocation> getCandidates() {
        return Block.REGISTRY.getKeys();
    }
}
