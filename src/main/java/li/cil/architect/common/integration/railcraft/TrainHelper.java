package li.cil.architect.common.integration.railcraft;

import mods.railcraft.api.carts.CartToolsAPI;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class TrainHelper implements ProxyRailcraft.ITrainHelper {
    @Override
    public IItemHandler getTrainItemHandler(EntityMinecart cart) {
        return CartToolsAPI.transferHelper.getTrainItemHandler(cart);
    }
    @Override
    public IFluidHandler getTrainFluidHandler(EntityMinecart cart) {
        return CartToolsAPI.transferHelper.getTrainFluidHandler(cart);
    }
}
