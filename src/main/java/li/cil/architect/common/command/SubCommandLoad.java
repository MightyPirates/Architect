package li.cil.architect.common.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Charsets;

import li.cil.architect.api.API;
import li.cil.architect.common.Architect;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageClipboard;
import li.cil.architect.common.network.message.MessageRequestBlueprintData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;

public final class SubCommandLoad extends AbstractSubCommand {

	@Override
	public String getName() {
		return "load";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (stack.isEmpty()) {
            throw new WrongUsageException(getUsage(sender));
        }

        final String configDirectory = Loader.instance().getConfigDir().getPath();
        final File path = Paths.get(configDirectory, API.MOD_ID, args[0] + ".bp").toFile();
        try {
			String value = FileUtils.readFileToString(path, Charsets.UTF_8);
			Network.INSTANCE.getWrapper().sendTo(new MessageClipboard(value), player);
	        Network.INSTANCE.getWrapper().sendTo(new MessageRequestBlueprintData(), player);		
		} catch (IOException e) {
			Architect.getLog().warn("Failed reading " + path.toString() + ".", e);
		}
	}
}
