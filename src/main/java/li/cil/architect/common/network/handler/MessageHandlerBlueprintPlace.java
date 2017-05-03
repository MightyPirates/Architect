package li.cil.architect.common.network.handler;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.common.network.message.MessageBlueprintPlace;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerBlueprintPlace extends AbstractMessageHandler<MessageBlueprintPlace> {
    @Override
    protected void onMessageSynchronized(final MessageBlueprintPlace message, final MessageContext context) {
        final EntityPlayer player = context.getServerHandler().playerEntity;
        final ItemStack stack = player.getHeldItem(message.getHand());
        if (!Items.isBlueprint(stack)) {
            return;
        }

        assert stack != null : "Items.isBlueprint returned true for null stack";

        final BlueprintData data = ItemBlueprint.getData(stack);
        PlayerUtils.setAimDistance(message.getAimDistance());
        data.createJobs(player, message.allowPartial(), message.getPos());
    }
}
