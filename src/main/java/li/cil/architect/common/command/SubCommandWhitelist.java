package li.cil.architect.common.command;

import li.cil.architect.common.config.Jasons;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

final class SubCommandWhitelist extends AbstractListCommand {
    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("wl");
    }

    @Override
    protected boolean addToList(final ResourceLocation location) {
        return Jasons.addToWhitelist(location);
    }

    @Override
    protected boolean removeFromList(final ResourceLocation location) {
        return Jasons.removeFromWhitelist(location);
    }
}
