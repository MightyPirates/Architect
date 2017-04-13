package li.cil.architect.common.network.handler;

import li.cil.architect.common.config.Constants;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.common.network.message.MessageClipboard;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Base64;

public final class MessageHandlerClipboardServer extends AbstractMessageHandler<MessageClipboard> {
    @Override
    protected void onMessageSynchronized(final MessageClipboard message, final MessageContext context) {
        final EntityPlayerMP player = context.getServerHandler().player;
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (stack.isEmpty()) {
            return;
        }

        try {
            final byte[] value = Base64.getDecoder().decode(message.getValue());
            final ByteArrayInputStream bytes = new ByteArrayInputStream(value);
            final DataInputStream input = new DataInputStream(bytes);
            final NBTTagCompound nbt = CompressedStreamTools.read(input);
            final BlueprintData data = new BlueprintData();
            data.deserializeNBT(nbt);
            ItemBlueprint.setData(stack, data);
            player.sendMessage(new TextComponentTranslation(Constants.COMMAND_PASTE_SUCCESS));
        } catch (final IOException e) {
            player.sendMessage(new TextComponentTranslation(Constants.COMMAND_PASTE_INVALID));
        }
    }
}
