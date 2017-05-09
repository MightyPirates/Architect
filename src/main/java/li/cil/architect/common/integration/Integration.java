package li.cil.architect.common.integration;

import li.cil.architect.common.integration.chiselsandbits.ProxyChiselsAndBits;
import li.cil.architect.common.integration.minecraft.ProxyMinecraft;
import li.cil.architect.common.integration.railcraft.ProxyRailcraft;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Central registry tracking mod proxies and initializing them.
 */
public final class Integration {
    private static final List<ModProxy> proxies = new ArrayList<>();

    static {
        proxies.add(new ProxyMinecraft());
        proxies.add(new ProxyChiselsAndBits());
        proxies.add(new ProxyRailcraft());
    }

    // --------------------------------------------------------------------- //

    public static void preInit(final FMLPreInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.preInit(event));
    }

    public static void init(final FMLInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.init(event));
    }

    public static void postInit(final FMLPostInitializationEvent event) {
        proxies.stream().filter(ModProxy::isAvailable).forEach(proxy -> proxy.postInit(event));
    }

    private Integration() {
    }
}
