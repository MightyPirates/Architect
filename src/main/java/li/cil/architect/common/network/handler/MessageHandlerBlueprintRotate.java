package li.cil.architect.common.network.handler;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.common.network.message.MessageBlueprintRotate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerBlueprintRotate extends AbstractMessageHandler<MessageBlueprintRotate> {
    @Override
    protected void onMessageSynchronized(final MessageBlueprintRotate message, final MessageContext context) {
        final EntityPlayer player = context.getServerHandler().player;
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (stack.isEmpty()) {
            return;
        }

        final BlueprintData data = ItemBlueprint.getData(stack);
        data.rotate(message.getRotation());
        ItemBlueprint.setData(stack, data);
    }
}
