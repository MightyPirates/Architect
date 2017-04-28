package li.cil.architect.util;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerFluidMap;

import java.util.HashMap;
import java.util.Map;

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

    public static IFluidHandler copy(final IFluidHandler fluidHandler) {
        final Map<Fluid, IFluidHandler> tanks = new HashMap<>();
        for (final IFluidTankProperties properties : fluidHandler.getTankProperties()) {
            final FluidStack stack = properties.getContents();
            if (stack != null) {
                tanks.computeIfAbsent(stack.getFluid(), fluid -> new FluidTank(fluid, 0, Integer.MAX_VALUE)).fill(stack, true);
            }
        }
        return new FluidHandlerFluidMap(tanks);
    }

    // --------------------------------------------------------------------- //

    private FluidHandlerUtils() {
    }
}
