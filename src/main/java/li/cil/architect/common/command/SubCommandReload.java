package li.cil.architect.common.command;

import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Jasons;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Map;

public final class SubCommandReload extends AbstractSubCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final Map<String, Throwable> errors = Jasons.loadJSON(false);
        if (errors.size() > 0) {
            notifyCommandListener(sender, this, Constants.COMMAND_RELOAD_ERRORS);
            errors.forEach((fileName, error) -> notifyCommandListener(sender, this, Constants.COMMAND_RELOAD_ERROR, fileName, error.getCause() != null ? error.getCause().getMessage() : error.getMessage()));
        } else {
            notifyCommandListener(sender, this, Constants.COMMAND_RELOAD_SUCCESS);
        }
    }
}
