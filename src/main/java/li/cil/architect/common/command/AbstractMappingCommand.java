package li.cil.architect.common.command;

import li.cil.architect.common.config.Constants;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

abstract class AbstractMappingCommand extends AbstractSubCommand {
    private static final String COMMAND_CLEAR = "clear";

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final ResourceLocation location = getLookedAtResourceLocation(sender);
        if (location == null) {
            return;
        }

        if (args.length < 1) {
            final ResourceLocation mapping = getMapping(location);
            if (mapping != null) {
                notifyCommandListener(sender, this, String.format(Constants.SUBCOMMAND_MAPPING_CURRENT, getName()), location, mapping);
            } else {
                notifyCommandListener(sender, this, String.format(Constants.SUBCOMMAND_MAPPING_NO_MAPPING, getName()), location);
            }
        } else if (args.length == 1) {
            if (COMMAND_CLEAR.equals(args[0])) {
                if (removeMapping(location)) {
                    notifyCommandListener(sender, this, String.format(Constants.SUBCOMMAND_MAPPING_REMOVED, getName()), location);
                } else {
                    notifyCommandListener(sender, this, String.format(Constants.SUBCOMMAND_MAPPING_NO_MAPPING, getName()), location);
                }
            } else {
                final ResourceLocation mapping = new ResourceLocation(args[0]);
                if (addMapping(location, mapping)) {
                    notifyCommandListener(sender, this, String.format(Constants.SUBCOMMAND_MAPPING_ADDED, getName()), location, mapping);
                } else {
                    throw new WrongUsageException(getUsage(sender));
                }
            }
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos targetPos) {
        if (args.length == 1) {
            final List<Object> candidates = new ArrayList<>();
            candidates.add(COMMAND_CLEAR);
            candidates.addAll(getCandidates());
            return getListOfStringsMatchingLastWord(args, candidates);
        }

        return Collections.emptyList();
    }

    // --------------------------------------------------------------------- //

    @Nullable
    protected abstract ResourceLocation getMapping(final ResourceLocation location);

    protected abstract boolean addMapping(final ResourceLocation location, final ResourceLocation mapping);

    protected abstract boolean removeMapping(final ResourceLocation location);

    protected abstract Collection<ResourceLocation> getCandidates();
}
