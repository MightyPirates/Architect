package li.cil.architect.common.integration.railcraft;

import li.cil.architect.common.integration.ModProxy;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.items.IItemHandler;

public class ProxyRailcraft implements ModProxy {
    static final String MOD_ID = "railcraft";
    public static ITrainHelper trainHelper = new ITrainHelper() {
        @Override
        public IItemHandler getTrainItemHandler(EntityMinecart cart) {
            return null;
        }

        @Override
        public IFluidHandler getTrainFluidHandler(EntityMinecart cart) {
            return null;
        }
    };

    @Override
    public boolean isAvailable() {
        return Loader.isModLoaded(MOD_ID);
    }

    @Override
    public void init(final FMLInitializationEvent event) {
        trainHelper = new TrainHelper();
    }

    public interface ITrainHelper {
        IItemHandler getTrainItemHandler(EntityMinecart cart);
        IFluidHandler getTrainFluidHandler(EntityMinecart cart);
    }
}
