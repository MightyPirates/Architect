package li.cil.architect.client.gui;

import li.cil.architect.common.init.Items;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandlerClient implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z) {
        switch (GuiId.VALUES[id]) {
            case BLUEPRINT: {
                final ItemStack stack = Items.getHeldItem(player, Items::isBlueprint);
                return new GuiBlueprint(player, stack);
            }
            default:
                break;
        }
        return null;
    }
}
