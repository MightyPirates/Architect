package li.cil.architect.common.command;

import li.cil.architect.common.Architect;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageClipboard;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;

public final class SubCommandCopy extends AbstractSubCommand {
    @Override
    public String getName() {
        return "copy";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (stack.isEmpty()) {
            throw new WrongUsageException(getUsage(sender));
        }

        try {
            final NBTTagCompound nbt = ItemBlueprint.getData(stack).serializeNBT();
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutput output = new DataOutputStream(bytes);
            CompressedStreamTools.write(nbt, output);
            final String value = Base64.getEncoder().encodeToString(bytes.toByteArray());
            Network.INSTANCE.getWrapper().sendTo(new MessageClipboard(value), player);
            notifyCommandListener(sender, this, Constants.COMMAND_COPY_SUCCESS);
        } catch (final IOException e) {
            Architect.getLog().warn("Failed serializing blueprint to base64.", e);
        }
    }
}
