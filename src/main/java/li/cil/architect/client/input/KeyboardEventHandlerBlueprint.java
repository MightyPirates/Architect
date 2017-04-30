package li.cil.architect.client.input;

import li.cil.architect.api.API;
import li.cil.architect.client.KeyBindings;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageBlueprintRotate;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rotation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public enum KeyboardEventHandlerBlueprint {
    INSTANCE;

    @SubscribeEvent
    public void handleKeyEvent(final InputEvent.KeyInputEvent event) {
        if (KeyBindings.rotateBlueprint.isKeyDown()) {
            rotateBlueprint();
        } else if (KeyBindings.toggleGrid.isKeyDown()) {
            toggleGrid();
        } else if (KeyBindings.toggleAllowPartial.isKeyDown()) {
            toggleAllowPartial();
        }
    }

    private void rotateBlueprint() {
        final EntityPlayer player = Minecraft.getMinecraft().player;
        final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
        if (stack.isEmpty()) {
            return;
        }

        final Rotation rotation = player.isSneaking() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        Network.INSTANCE.getWrapper().sendToServer(new MessageBlueprintRotate(rotation));
    }

    private void toggleGrid() {
        Settings.enablePlacementGrid = !Settings.enablePlacementGrid;
        ConfigManager.sync(API.MOD_ID, Config.Type.INSTANCE);
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentTranslation(Settings.enablePlacementGrid ? Constants.MESSAGE_GRID_ENABLED : Constants.MESSAGE_GRID_DISABLED), Constants.CHAT_LINE_ID);
    }

    private void toggleAllowPartial() {
        Settings.allowPlacePartial = !Settings.allowPlacePartial;
        ConfigManager.sync(API.MOD_ID, Config.Type.INSTANCE);
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentTranslation(Settings.allowPlacePartial ? Constants.MESSAGE_PARTIAL_ENABLED : Constants.MESSAGE_PARTIAL_DISABLED), Constants.CHAT_LINE_ID);
    }
}
