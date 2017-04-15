package li.cil.architect.common.command;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageRequestBlueprintData;
import li.cil.architect.util.ItemStackUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public final class SubCommandPaste extends AbstractSubCommand {
    @Override
    public String getName() {
        return "paste";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (ItemStackUtils.isEmpty(stack)) {
            throw new WrongUsageException(getUsage(sender));
        }

        Network.INSTANCE.getWrapper().sendTo(new MessageRequestBlueprintData(), player);
    }
}
