package li.cil.architect.common.api;

import li.cil.architect.api.API;
import li.cil.architect.common.init.Items;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class CreativeTab extends CreativeTabs {
    public CreativeTab() {
        super(API.MOD_ID);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Item getTabIconItem() {
        return Items.sketch;
    }
}
