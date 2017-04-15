package li.cil.architect.common.network.handler;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.common.network.message.MessageBlueprintRotate;
import li.cil.architect.util.ItemStackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerBlueprintRotate extends AbstractMessageHandler<MessageBlueprintRotate> {
    @Override
    protected void onMessageSynchronized(final MessageBlueprintRotate message, final MessageContext context) {
        final EntityPlayer player = context.getServerHandler().playerEntity;
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (ItemStackUtils.isEmpty(stack)) {
            return;
        }

        final BlueprintData data = ItemBlueprint.getData(stack);
        data.rotate(message.getRotation());
        ItemBlueprint.setData(stack, data);
    }
}
