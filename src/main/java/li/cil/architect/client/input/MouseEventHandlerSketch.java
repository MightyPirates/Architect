package li.cil.architect.client.input;

import li.cil.architect.common.init.Items;
import li.cil.architect.util.ItemStackUtils;
import li.cil.architect.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public enum MouseEventHandlerSketch {
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

        final ItemStack stack = Items.getHeldItem(player, Items::isSketch);
        if (ItemStackUtils.isEmpty(stack)) {
            return;
        }

        final float delta = Math.signum(event.getDwheel());
        PlayerUtils.changeFreeAimDistance(delta);

        // Avoid selecting different item.
        event.setCanceled(true);
    }

}
