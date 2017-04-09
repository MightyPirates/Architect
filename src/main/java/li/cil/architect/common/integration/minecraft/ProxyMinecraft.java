package li.cil.architect.common.integration.minecraft;

import li.cil.architect.common.integration.ModProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public final class ProxyMinecraft implements ModProxy {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void init(final FMLInitializationEvent event) {
//        ConverterAPI.addConverter(new ConverterMinecraft());
    }
}
