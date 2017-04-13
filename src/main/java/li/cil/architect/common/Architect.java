package li.cil.architect.common;

import li.cil.architect.api.API;
import li.cil.architect.common.command.CommandArchitect;
import li.cil.architect.common.config.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for FML.
 */
@Mod(modid = API.MOD_ID, version = API.MOD_VERSION, name = Constants.MOD_NAME,
     guiFactory = Constants.GUI_FACTORY, useMetadata = true)
public final class Architect {

    // --------------------------------------------------------------------- //
    // FML / Forge

    @Mod.Instance(API.MOD_ID)
    public static Architect instance;

    @SidedProxy(clientSide = Constants.PROXY_CLIENT, serverSide = Constants.PROXY_COMMON)
    public static ProxyCommon proxy;

    @EventHandler
    public void onPreInit(final FMLPreInitializationEvent event) {
        log = event.getModLog();
        proxy.onPreInit(event);
    }

    @EventHandler
    public void onInit(final FMLInitializationEvent event) {
        proxy.onInit(event);
    }

    @EventHandler
    public void onPostInit(final FMLPostInitializationEvent event) {
        proxy.onPostInit(event);
    }

    @EventHandler
    public void onIMC(final FMLInterModComms.IMCEvent event) {
        proxy.onIMC(event);
    }

    @EventHandler
    public void onServerStarting(final FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandArchitect());
    }

    // --------------------------------------------------------------------- //

    /**
     * Logger the mod should use, filled in pre-init.
     */
    private static Logger log;

    /**
     * Get the logger to be used by the mod.
     *
     * @return the mod's logger.
     */
    public static Logger getLog() {
        return log;
    }
}
