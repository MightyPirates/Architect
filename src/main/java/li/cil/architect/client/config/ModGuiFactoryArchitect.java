package li.cil.architect.client.config;

import li.cil.architect.api.API;
import li.cil.architect.common.config.Constants;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.DefaultGuiFactory;

public class ModGuiFactoryArchitect extends DefaultGuiFactory {
    public ModGuiFactoryArchitect() {
        super(API.MOD_ID, Constants.MOD_NAME);
    }

    @Override
    public GuiScreen createConfigGui(final GuiScreen parentScreen) {
        return new GuiConfigArchitect(parentScreen);
    }
}
