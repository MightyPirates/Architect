package li.cil.architect.client.input;

import li.cil.architect.client.KeyBindings;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageBlueprintRotate;
import li.cil.architect.util.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rotation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public enum KeyboardEventHandlerBlueprint {
    INSTANCE;

    @SubscribeEvent
    public void handleMouseEvent(final InputEvent.KeyInputEvent event) {
        if (!KeyBindings.rotateBlueprint.isKeyDown()) {
            return;
        }

        final EntityPlayer player = Minecraft.getMinecraft().player;
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (ItemStackUtils.isEmpty(stack)) {
            return;
        }

        final Rotation rotation = player.isSneaking() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        Network.INSTANCE.getWrapper().sendToServer(new MessageBlueprintRotate(rotation));
    }
}
