package li.cil.architect.common.integration.railcraft;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

enum TrainHelperDefault implements ITrainHelper {
    INSTANCE;

    @Nullable
    @Override
    public IItemHandler getTrainItemHandler(final EntityMinecart cart) {
        return null;
    }

    @Nullable
    @Override
    public IFluidHandler getTrainFluidHandler(final EntityMinecart cart) {
        return null;
    }
}
