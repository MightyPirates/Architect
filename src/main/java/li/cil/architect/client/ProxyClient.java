package li.cil.architect.client;

import li.cil.architect.client.event.KeyboardEventHandlerBlueprint;
import li.cil.architect.client.event.MouseEventHandlerBlueprint;
import li.cil.architect.client.event.MouseEventHandlerSketch;
import li.cil.architect.client.renderer.BlueprintRenderer;
import li.cil.architect.client.renderer.ProviderRenderer;
import li.cil.architect.client.renderer.SketchRenderer;
import li.cil.architect.common.ProxyCommon;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.function.Supplier;

/**
 * Takes care of client-side only setup.
 */
public final class ProxyClient extends ProxyCommon {
    @Override
    public void onPreInit(final FMLPreInitializationEvent event) {
        super.onPreInit(event);

        MinecraftForge.EVENT_BUS.register(BlueprintRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ProviderRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(SketchRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(KeyboardEventHandlerBlueprint.INSTANCE);
        MinecraftForge.EVENT_BUS.register(MouseEventHandlerBlueprint.INSTANCE);
        MinecraftForge.EVENT_BUS.register(MouseEventHandlerSketch.INSTANCE);
    }

    @Override
    public void onInit(final FMLInitializationEvent event) {
        super.onInit(event);

        KeyBindings.init();
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
