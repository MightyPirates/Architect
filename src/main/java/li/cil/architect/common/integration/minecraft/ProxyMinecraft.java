package li.cil.architect.common.integration.minecraft;

import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.integration.ModProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.util.UUID;

public final class ProxyMinecraft implements ModProxy {
    static final UUID UUID_CONVERTER_MINECRAFT_DOOR = UUID.fromString("b73a07f1-d551-4f0a-b464-5cd08432dab0");
    static final UUID UUID_CONVERTER_MINECRAFT_BED = UUID.fromString("e6364da1-6794-4523-bbbb-517caf44a607");

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        ConverterAPI.addConverter(new ConverterDoor());
        ConverterAPI.addConverter(new ConverterBed());
    }
}
