package li.cil.architect.common.blueprint;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public enum ProviderManager {
    INSTANCE;

    public void addProvider(final World world, final Provider provider) {
        getInstance(world).addProvider(provider);
    }

    public void removeProvider(final World world, final Provider provider) {
        getInstance(world).removeProvider(provider);
    }

    public Stream<Provider> getProviders(final World world, final Vec3d pos) {
        return getInstance(world).getProviders(pos);
    }

    public boolean hasProviders(final World world, final Vec3d pos) {
        return getInstance(world).hasProviders(pos);
    }

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        final int dimension = event.getWorld().provider.getDimension();
        MANAGERS.remove(dimension);
    }

    // --------------------------------------------------------------------- //

    private final TIntObjectMap<ProviderManagerImpl> MANAGERS = new TIntObjectHashMap<>();

    private ProviderManagerImpl getInstance(final World world) {
        final int dimension = world.provider.getDimension();
        ProviderManagerImpl manager = MANAGERS.get(dimension);
        if (manager == null) {
            manager = new ProviderManagerImpl();
            MANAGERS.put(dimension, manager);
        }
        return manager;
    }

    private static final class ProviderManagerImpl {
        private final List<Provider> providers = new ArrayList<>();

        void addProvider(final Provider provider) {
            providers.add(provider);
        }

        void removeProvider(final Provider provider) {
            providers.remove(provider);
        }

        Stream<Provider> getProviders(final Vec3d pos) {
            return providers.stream().filter(provider -> isProviderInRange(provider, pos));
        }

        boolean hasProviders(final Vec3d pos) {
            for (final Provider provider : providers) {
                if (isProviderInRange(provider, pos)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isProviderInRange(final Provider provider, final Vec3d pos) {
            final Vec3d delta = provider.getPosition().subtract(pos);
            final float radius = provider.getRadius();
            return delta.xCoord < radius || delta.yCoord < radius || delta.zCoord < radius;
        }
    }
}
