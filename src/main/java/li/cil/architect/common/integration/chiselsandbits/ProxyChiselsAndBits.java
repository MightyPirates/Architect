package li.cil.architect.common.integration.chiselsandbits;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.integration.ModProxy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.util.UUID;

public class ProxyChiselsAndBits implements ModProxy {
    static final String MOD_ID = "chiselsandbits";
    static final UUID UUID_CONVERTER_CHISELED_BLOCK = UUID.fromString("287e0e8e-160a-4d97-afb3-6a1a9fd71d6c");

    @Override
    public boolean isAvailable() {
        return Loader.isModLoaded(MOD_ID);
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        ConverterAPI.addConverter(new ConverterChiseledBlock());
    }
}
