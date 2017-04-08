package li.cil.architect.common.blueprint;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.IItemHandler;

public interface Provider {
    Vec3d getPosition();

    float getRadius();

    int getOperationsPerTick();

    IItemHandler getMaterials();
}
