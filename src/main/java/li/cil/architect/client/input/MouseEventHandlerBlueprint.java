package li.cil.architect.client.input;

import li.cil.architect.common.init.Items;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageBlueprintShift;
import li.cil.architect.util.ItemStackUtils;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public enum MouseEventHandlerBlueprint {
    INSTANCE;

    @SubscribeEvent
    public void handleMouseEvent(final MouseEvent event) {
        if (event.getDwheel() == 0) {
            return;
        }

        final EntityPlayer player = Minecraft.getMinecraft().player;
        if (!player.isSneaking()) {
            return;
        }

        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (ItemStackUtils.isEmpty(stack)) {
            return;
        }

        final EnumFacing facing = PlayerUtils.getPrimaryFacing(player);
        final EnumFacing shift = event.getDwheel() > 0 ? facing : facing.getOpposite();
        Network.INSTANCE.getWrapper().sendToServer(new MessageBlueprintShift(shift));

        // Avoid selecting different item.
        event.setCanceled(true);
    }
}
