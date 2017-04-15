package li.cil.architect.common.network.handler;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.item.ItemBlueprint;
import li.cil.architect.common.item.data.BlueprintData;
import li.cil.architect.common.network.message.MessageBlueprintShift;
import li.cil.architect.util.ItemStackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerBlueprintShift extends AbstractMessageHandler<MessageBlueprintShift> {
    @Override
    protected void onMessageSynchronized(final MessageBlueprintShift message, final MessageContext context) {
        final EntityPlayer player = context.getServerHandler().playerEntity;
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (ItemStackUtils.isEmpty(stack)) {
            return;
        }

        final BlueprintData data = ItemBlueprint.getData(stack);
        data.addShift(message.getFacing().getDirectionVec());
        ItemBlueprint.setData(stack, data);
    }
}
