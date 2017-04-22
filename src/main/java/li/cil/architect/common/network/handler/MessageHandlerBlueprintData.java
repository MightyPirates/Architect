package li.cil.architect.common.network.handler;

import com.google.common.base.Strings;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.network.message.MessageBlueprintData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerBlueprintData extends AbstractMessageHandler<MessageBlueprintData> {
    @Override
    protected void onMessageSynchronized(final MessageBlueprintData message, final MessageContext context) {
        final EntityPlayer player = context.getServerHandler().player;
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (stack.isEmpty()) {
            return;
        }

        if (Strings.isNullOrEmpty(message.getName())) {
            final NBTTagCompound nbt = stack.getSubCompound("display");
            if (nbt != null) {
                nbt.removeTag("Name");
            }
        } else {
            stack.setStackDisplayName(message.getName());
        }
        ItemBlueprint.setColor(stack, message.getColor());
    }
}
