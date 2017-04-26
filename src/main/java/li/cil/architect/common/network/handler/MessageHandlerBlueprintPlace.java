package li.cil.architect.common.network.handler;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.common.network.message.MessageBlueprintPlace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerBlueprintPlace extends AbstractMessageHandler<MessageBlueprintPlace> {
    @Override
    protected void onMessageSynchronized(final MessageBlueprintPlace message, final MessageContext context) {
        final EntityPlayer player = context.getServerHandler().player;
        final ItemStack stack = player.getHeldItem(message.getHand());
        if (stack.isEmpty() || !Items.isBlueprint(stack)) {
            return;
        }

        final BlueprintData data = ItemBlueprint.getData(stack);
        data.createJobs(player, message.allowPartial(), message.getPos());
    }
}
