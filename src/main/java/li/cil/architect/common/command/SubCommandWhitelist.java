package li.cil.architect.common.command;

import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.common.config.Jasons;
import net.minecraft.command.CommandException;
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
    protected boolean addToList(final ResourceLocation location, final String[] args) throws CommandException {
        final int sortIndex;
        if (args.length == 0) {
            sortIndex = SortIndex.SOLID_BLOCK;
        } else if ("attached".equals(args[0])) {
            sortIndex = SortIndex.ATTACHED_BLOCK;
        } else if ("falling".equals(args[0])) {
            sortIndex = SortIndex.FALLING_BLOCK;
        } else if ("fluid".equals(args[0])) {
            sortIndex = SortIndex.FLUID_BLOCK;
        } else {
            sortIndex = parseInt(args[0]);
        }
        return Jasons.addToWhitelist(location, sortIndex);
    }

    @Override
    protected boolean removeFromList(final ResourceLocation location) {
        return Jasons.removeFromWhitelist(location);
    }
}
