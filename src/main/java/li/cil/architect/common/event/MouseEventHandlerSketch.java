package li.cil.architect.common.event;

import li.cil.architect.common.Settings;
import li.cil.architect.common.init.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
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

        final ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.getItem() != Items.sketch) {
            return;
        }

        final float delta = Math.signum(event.getDwheel());
        Settings.freeAimDistance = MathHelper.clamp(Settings.freeAimDistance + delta, 1, 5);

        // Avoid selecting different item.
        event.setCanceled(true);
    }
}
