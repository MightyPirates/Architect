package li.cil.architect.common.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Charsets;

import li.cil.architect.api.API;
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
import net.minecraftforge.fml.common.Loader;

public final class SubCommandSave extends AbstractSubCommand {

	@Override
	public String getName() {
		return "save";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (stack.isEmpty()) {
            throw new WrongUsageException(getUsage(sender));
        }

        final String configDirectory = Loader.instance().getConfigDir().getPath();
        final File path = Paths.get(configDirectory, API.MOD_ID, stack.getDisplayName() + ".bp").toFile();

        try {
            final NBTTagCompound nbt = ItemBlueprint.getData(stack).serializeNBT();
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(nbt, bytes);
            final String value = Base64.getEncoder().encodeToString(bytes.toByteArray());
            FileUtils.writeStringToFile(path, value, Charsets.UTF_8);            
            notifyCommandListener(sender, this, Constants.COMMAND_SAVE_SUCCES);
        } catch (final IOException e) {
            Architect.getLog().warn("Failed writing " + path.toString() + ".", e);
        }
	}
}
