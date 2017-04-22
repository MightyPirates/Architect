package li.cil.architect.util;

import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public final class FluidHandlerUtils {
    public static boolean canDrain(final IFluidHandler handler) {
        final IFluidTankProperties[] properties = handler.getTankProperties();
        if (properties == null || properties.length == 0) {
            return false;
        }

        for (final IFluidTankProperties property : properties) {
            if (property != null && property.canDrain()) {
                return true;
            }
        }

        return false;
    }

    // --------------------------------------------------------------------- //

    private FluidHandlerUtils() {
    }
}
