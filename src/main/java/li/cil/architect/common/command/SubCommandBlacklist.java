package li.cil.architect.common.command;

import li.cil.architect.common.config.Jasons;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

final class SubCommandBlacklist extends AbstractListCommand {
    @Override
    public String getName() {
        return "blacklist";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("bl");
    }

    @Override
    protected boolean addToList(final ResourceLocation location, final String[] args) {
        return Jasons.addToBlacklist(location);
    }

    @Override
    protected boolean removeFromList(final ResourceLocation location) {
        return Jasons.removeFromBlacklist(location);
    }
}
