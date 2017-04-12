package li.cil.architect.common.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import li.cil.architect.common.Architect;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageClipboard;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public final class SubCommandNbt extends AbstractSubCommand {
    @Override
    public String getName() {
        return "nbt";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        final World world = player.getEntityWorld();

        final BlockPos pos = getLookedAtBlockPos(sender);
        if (pos == null || !world.isBlockLoaded(pos)) {
            throw new WrongUsageException(getUsage(sender));
        }

        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        final ResourceLocation location = Block.REGISTRY.getNameForObject(block);
        if (location == null) {
            notifyCommandListener(sender, this, Constants.COMMAND_NBT_INVALID_BLOCK);
            return;
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            notifyCommandListener(sender, this, Constants.COMMAND_NBT_NO_TILE_ENTITY);
            return;
        }

        // Grab nbt from tile entity.
        final NBTTagCompound nbt = new NBTTagCompound();
        try {
            tileEntity.writeToNBT(nbt);
        } catch (final Throwable e) {
            notifyCommandListener(sender, this, Constants.COMMAND_NBT_ERROR);
            Architect.getLog().warn("Failed getting tile entity NBT.", e);
            return;
        }

        // Strip stuff written by TileEntity base class.
        nbt.removeTag("x");
        nbt.removeTag("y");
        nbt.removeTag("z");
        nbt.removeTag("id");

        // Convert it to our filter format.
        final StringBuilder builder = new StringBuilder();
        convert(nbt, builder);

        // Clean it up.
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String json = gson.toJson(new JsonParser().parse(builder.toString()));

        // Add remaining filter data for easier pasting to whitelist.
        builder.setLength(0);
        builder.append("  \"").append(escape(location.toString())).append("\": {\n");
        builder.append("    \"nbt\": ");
        indent(json, builder);
        builder.append("  },\n");

        // Send it to the client!
        Network.INSTANCE.getWrapper().sendTo(new MessageClipboard(builder.toString()), player);
        notifyCommandListener(sender, this, Constants.COMMAND_NBT_SUCCESS);
    }

    // --------------------------------------------------------------------- //

    private static void convert(final NBTTagCompound nbt, final StringBuilder builder) {
        builder.append('{');
        for (final String key : nbt.getKeySet()) {
            builder.append('"').append(escape(key)).append('"').append(':');
            final NBTBase value = nbt.getTag(key);
            if (value instanceof NBTTagCompound) {
                convert((NBTTagCompound) value, builder);
            } else {
                builder.append('"').append(escape(NBTBase.NBT_TYPES[value.getId()])).append('"');
            }
            builder.append(',');
        }
        if (!nbt.hasNoTags()) {
            builder.setLength(builder.length() - 1);
        }
        builder.append('}');
    }

    private static String escape(final String value) {
        return value.replace("\"", "\\\"");
    }

    private static void indent(final String json, final StringBuilder builder) {
        try (final StringReader reader = new StringReader(json)) {
            final BufferedReader buffer = new BufferedReader(reader);
            String line = buffer.readLine();
            if (line != null) {
                builder.append(line).append('\n');
            }
            while ((line = buffer.readLine()) != null) {
                builder.append("      ").append(line).append('\n');
            }
        } catch (final IOException e) {
            builder.append("IT BROKE, OOPS!");
        }
    }
}