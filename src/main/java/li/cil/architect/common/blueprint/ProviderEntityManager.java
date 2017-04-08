package li.cil.architect.common.blueprint;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public enum ProviderEntityManager {
    INSTANCE;

    // List for fast iteration, map for fast lookup in updates.
    private final List<ProviderEntity> providerList = new ArrayList<>();
    private final Map<Entity, ProviderEntity> providerMap = new HashMap<>();

    // --------------------------------------------------------------------- //

    public void updateEntity(final Entity entity, final float radius, final int operationsPerTick) {
        final ProviderEntity provider;
        if (providerMap.containsKey(entity)) {
            provider = providerMap.get(entity);
        } else {
            provider = new ProviderEntity(entity);
            providerList.add(provider);
            providerMap.put(entity, provider);
            ProviderManager.INSTANCE.addProvider(provider.world, provider);
        }
        provider.updateValues(radius, operationsPerTick);
    }

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent event) {
        final Iterator<ProviderEntity> iterator = providerList.iterator();
        while (iterator.hasNext()) {
            final ProviderEntity provider = iterator.next();
            if (provider.isInvalid()) {
                iterator.remove();
                providerMap.remove(provider.entity, provider);
                ProviderManager.INSTANCE.removeProvider(provider.world, provider);
            }
        }
    }

    // --------------------------------------------------------------------- //

    private static final class ProviderEntity implements Provider {
        private static final long KEEP_ALIVE_TICKS = 5;

        final Entity entity;
        final World world;
        long lastUpdated;
        float radius = 1;
        int operationsPerTick = 1;

        // --------------------------------------------------------------------- //

        ProviderEntity(final Entity entity) {
            this.entity = entity;
            world = entity.getEntityWorld();
            lastUpdated = world.getTotalWorldTime();
            ProviderEntityManager.INSTANCE.providerList.add(this);
        }

        void updateValues(final float radius, final int operationsPerTick) {
            final long worldTime = world.getTotalWorldTime();
            if (worldTime > lastUpdated) {
                lastUpdated = worldTime;
                this.radius = Math.max(1, radius);
                this.operationsPerTick = Math.max(1, operationsPerTick);
            } else {
                this.radius = Math.max(this.radius, radius);
                this.operationsPerTick = Math.max(this.operationsPerTick, operationsPerTick);
            }
        }

        boolean isInvalid() {
            return entity.isDead || entity.getEntityWorld() != world || (world.getTotalWorldTime() - lastUpdated) > KEEP_ALIVE_TICKS;
        }

        // --------------------------------------------------------------------- //
        // Provider

        @Override
        public Vec3d getPosition() {
            return entity.getPositionVector();
        }

        @Override
        public float getRadius() {
            return radius;
        }

        @Override
        public int getOperationsPerTick() {
            return operationsPerTick;
        }

        @Override
        public IItemHandler getMaterials() {
            final IItemHandler itemHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            return itemHandler != null ? itemHandler : EmptyHandler.INSTANCE;
        }
    }
}
