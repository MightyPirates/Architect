package li.cil.architect.client.config;

import li.cil.architect.api.API;
import li.cil.architect.common.config.Constants;
import li.cil.architect.common.config.Settings;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class GuiConfigArchitect extends GuiConfig {
    private static Property blacklist;
    private static Property whitelist;
    private static Property attachedBlocks;

    GuiConfigArchitect(final GuiScreen parentScreen) {
        super(parentScreen, collectConfigElements(ConfigManager.getModConfigClasses(API.MOD_ID)), API.MOD_ID, null, false, false, Constants.MOD_NAME, null);
    }

    private static List<IConfigElement> collectConfigElements(Class<?>[] configClasses) {
        List<IConfigElement> toReturn;
        if (configClasses.length == 1) {
            toReturn = ConfigElement.from(configClasses[0]).getChildElements();
        } else {
            toReturn = new ArrayList<>();
            for (Class<?> clazz : configClasses) {
                toReturn.add(ConfigElement.from(clazz));
            }
        }

        blacklist = new Property("blacklist", Settings.getBlacklist(), Property.Type.STRING, Constants.CONFIG_BLACKLIST);
        blacklist.setComment("Blacklisted blocks will never be handled by the built-in converters.");
        blacklist.setDefaultValues(new String[0]);
        toReturn.add(new ConfigElement(blacklist));
        whitelist = new Property("whitelist", Settings.getWhitelist(), Property.Type.STRING, Constants.CONFIG_WHITELIST);
        whitelist.setComment("Whitelisted blocks may be handled by the built-in converters even if they have a tile entity.");
        whitelist.setDefaultValues(new String[0]);
        toReturn.add(new ConfigElement(whitelist));
        attachedBlocks = new Property("attached", Settings.getAttachedBlocks(), Property.Type.STRING, Constants.CONFIG_ATTACHED);
        attachedBlocks.setComment("Attached blocks are reported by the built-in converters as having to be placed after solid blocks.");
        attachedBlocks.setDefaultValues(new String[0]);
        toReturn.add(new ConfigElement(attachedBlocks));

        toReturn.sort(Comparator.comparing(e -> I18n.format(e.getLanguageKey())));
        return toReturn;
    }

    @Override
    public void onGuiClosed() {
        if (blacklist.hasChanged()) {
            Settings.setBlacklist(blacklist.getStringList());
        }
        if (whitelist.hasChanged()) {
            Settings.setWhitelist(whitelist.getStringList());
        }
        if (attachedBlocks.hasChanged()) {
            Settings.setAttachedBlocks(attachedBlocks.getStringList());
        }
        if (blacklist.hasChanged() || whitelist.hasChanged() || attachedBlocks.hasChanged()) {
            Settings.saveJSON();
        }
        super.onGuiClosed();
    }
}
