package li.cil.architect.client;

import li.cil.architect.common.config.Constants;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public final class KeyBindings {
    public static final KeyBinding rotateBlueprint = new KeyBinding(Constants.KEY_BINDINGS_BLUEPRINT_ROTATE, KeyConflictContext.IN_GAME, Keyboard.KEY_R, Constants.KEY_BINDINGS_CATEGORY_NAME);
    public static final KeyBinding toggleGrid = new KeyBinding(Constants.KEY_BINDINGS_TOGGLE_GRID, KeyConflictContext.IN_GAME, Keyboard.KEY_G, Constants.KEY_BINDINGS_CATEGORY_NAME);

    public static void init() {
        ClientRegistry.registerKeyBinding(rotateBlueprint);
        ClientRegistry.registerKeyBinding(toggleGrid);
    }

    private KeyBindings() {
    }
}
