package li.cil.architect.client.input;

import li.cil.architect.api.API;
import li.cil.architect.client.KeyBindings;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.network.Network;
import li.cil.architect.common.network.message.MessageBlueprintRotate;
import li.cil.architect.util.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rotation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

public enum KeyboardEventHandlerBlueprint {
    INSTANCE;

    @SubscribeEvent
    public void handleMouseEvent(final InputEvent.KeyInputEvent event) {
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
        if (ItemStackUtils.isEmpty(stack)) {
            return;
        }

        final Rotation rotation = player.isSneaking() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        Network.INSTANCE.getWrapper().sendToServer(new MessageBlueprintRotate(rotation));
    }

    private void toggleGrid() {
        Settings.enablePlacementGrid = !Settings.enablePlacementGrid;
        syncConfig();
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentTranslation(Settings.enablePlacementGrid ? Constants.MESSAGE_GRID_ENABLED : Constants.MESSAGE_GRID_DISABLED), Constants.CHAT_LINE_ID);
    }

    private void toggleAllowPartial() {
        Settings.allowPlacePartial = !Settings.allowPlacePartial;
        syncConfig();
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentTranslation(Settings.allowPlacePartial ? Constants.MESSAGE_PARTIAL_ENABLED : Constants.MESSAGE_PARTIAL_DISABLED), Constants.CHAT_LINE_ID);
    }

    @SuppressWarnings("unchecked")
    private void syncConfig() {
        try {
            // Why u no accessor D: Also why do I even bother in 1.10 -.-
            final File configDir = Loader.instance().getConfigDir();
            final File file = new File(configDir, API.MOD_ID + ".cfg");
            final Field configsField = ConfigManager.class.getDeclaredField("CONFIGS");
            configsField.setAccessible(true);
            final Map<String, Configuration> configs = (Map<String, Configuration>) configsField.get(null);
            final Configuration config = configs.get(file.getAbsolutePath());
            config.get(Configuration.CATEGORY_GENERAL, "enablePlacementGrid", true, "Whether to snap to a grid the size of the blueprint bounds when placing blueprints.").set(Settings.enablePlacementGrid);
            config.save();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }
}
