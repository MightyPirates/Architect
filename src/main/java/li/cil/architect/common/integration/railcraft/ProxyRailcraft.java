package li.cil.architect.common.integration.railcraft;

import li.cil.architect.common.integration.ModProxy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ProxyRailcraft implements ModProxy {
    static final String MOD_ID = "railcraft";
    public static ITrainHelper trainHelper = TrainHelperDefault.INSTANCE;

    @Override
    public boolean isAvailable() {
        return Loader.isModLoaded(MOD_ID);
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        trainHelper = new TrainHelper();
    }
}
