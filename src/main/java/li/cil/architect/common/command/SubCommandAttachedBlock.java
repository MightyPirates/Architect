package li.cil.architect.common.command;

import li.cil.architect.common.config.Jasons;
import net.minecraft.util.ResourceLocation;

final class SubCommandAttachedBlock extends AbstractListCommand {
    @Override
    public String getName() {
        return "attached";
    }

    @Override
    protected boolean addToList(final ResourceLocation location) {
        return Jasons.addToAttachedBlockList(location);
    }

    @Override
    protected boolean removeFromList(final ResourceLocation location) {
        return Jasons.removeFromAttachedBlockList(location);
    }
}
