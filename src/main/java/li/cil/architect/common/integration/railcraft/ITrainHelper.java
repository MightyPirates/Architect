package li.cil.architect.common.integration.railcraft;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public interface ITrainHelper {
    @Nullable
    IItemHandler getTrainItemHandler(EntityMinecart cart);

    @Nullable
    IFluidHandler getTrainFluidHandler(EntityMinecart cart);
}
