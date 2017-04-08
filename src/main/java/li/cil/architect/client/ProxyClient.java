package li.cil.architect.client;

import li.cil.architect.client.gui.GuiHandlerClient;
import li.cil.architect.client.renderer.BlueprintRenderer;
import li.cil.architect.client.renderer.SketchRenderer;
import li.cil.architect.common.Architect;
import li.cil.architect.common.ProxyCommon;
import li.cil.architect.common.event.MouseEventHandlerBlueprint;
import li.cil.architect.common.event.MouseEventHandlerSketch;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.function.Supplier;

/**
 * Takes care of client-side only setup.
 */
public final class ProxyClient extends ProxyCommon {
    @Override
    public void onPreInit(final FMLPreInitializationEvent event) {
        super.onPreInit(event);

        MinecraftForge.EVENT_BUS.register(BlueprintRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(SketchRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(MouseEventHandlerBlueprint.INSTANCE);
        MinecraftForge.EVENT_BUS.register(MouseEventHandlerSketch.INSTANCE);
    }

    @Override
    public void onInit(final FMLInitializationEvent event) {
        super.onInit(event);

        // Register GUI handler for fancy GUIs in our almost GUI-less mod!
        NetworkRegistry.INSTANCE.registerGuiHandler(Architect.instance, new GuiHandlerClient());
    }

    // --------------------------------------------------------------------- //

    @Override
    public Item registerItem(final String name, final Supplier<Item> constructor) {
        final Item item = super.registerItem(name, constructor);
        setCustomItemModelResourceLocation(item);
        return item;
    }

    // --------------------------------------------------------------------- //

    private static void setCustomItemModelResourceLocation(final Item item) {
        final ResourceLocation registryName = item.getRegistryName();
        assert registryName != null;
        final ModelResourceLocation location = new ModelResourceLocation(registryName, "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, location);
    }
}
