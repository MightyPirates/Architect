package li.cil.architect.common.command;

import li.cil.architect.common.config.Constants;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract class AbstractListCommand extends AbstractSubCommand {
    private static final String COMMAND_ADD = "add";
    private static final String COMMAND_REMOVE = "remove";

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final ResourceLocation location = getLookedAtResourceLocation(sender);
        if (location == null) {
            return;
        }

        if (args.length < 1) {
            if (addToList(location)) {
                notifyCommandListener(sender, this, String.format(Constants.COMMAND_LIST_ADDED, getName()), location);
            } else {
                removeFromList(location);
                notifyCommandListener(sender, this, String.format(Constants.COMMAND_LIST_REMOVED, getName()), location);
            }
        } else if (args.length == 1) {
            if (COMMAND_ADD.equals(args[0])) {
                if (addToList(location)) {
                    notifyCommandListener(sender, this, String.format(Constants.COMMAND_LIST_ADDED, getName()), location);
                }
            } else if (COMMAND_REMOVE.equals(args[0])) {
                if (removeFromList(location)) {
                    notifyCommandListener(sender, this, String.format(Constants.COMMAND_LIST_REMOVED, getName()), location);
                }
            } else {
                throw new WrongUsageException(getUsage(sender));
            }
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, @Nullable final BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList(COMMAND_ADD, COMMAND_REMOVE));
        }

        return Collections.emptyList();
    }

    // --------------------------------------------------------------------- //

    protected abstract boolean addToList(final ResourceLocation location);

    protected abstract boolean removeFromList(final ResourceLocation location);
}
