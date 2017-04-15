package li.cil.architect.common.command;

import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Jasons;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public final class SubCommandReload extends AbstractSubCommand {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        Jasons.loadJSON(false);
        notifyCommandListener(sender, this, Constants.COMMAND_RELOAD_SUCCESS);
    }
}
