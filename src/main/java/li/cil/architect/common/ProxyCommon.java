package li.cil.architect.common;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import li.cil.architect.api.API;
import li.cil.architect.api.ConverterAPI;
import li.cil.architect.common.api.ConverterAPIImpl;
import li.cil.architect.common.api.CreativeTab;
import li.cil.architect.common.config.ConverterFilter;
import li.cil.architect.common.config.Jasons;
import li.cil.architect.common.converter.ConverterComplex;
import li.cil.architect.common.converter.ConverterFallingBlock;
import li.cil.architect.common.converter.ConverterSimpleBlock;
import li.cil.architect.common.init.Items;
import li.cil.architect.common.integration.Integration;
import li.cil.architect.common.jobs.JobManager;
import li.cil.architect.common.network.Network;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Takes care of common setup.
 */
public class ProxyCommon {
    public void onPreInit(final FMLPreInitializationEvent event) {
        // Load additional JSON based settings (black/whitelists, mappings).
        Jasons.loadJSON(true);

        // Initialize API.
        API.creativeTab = new CreativeTab();

        API.converterAPI = new ConverterAPIImpl();

        // Register blocks and items.
        Items.register(this);

        // Mod integration.
        Integration.preInit(event);
    }

    public void onInit(final FMLInitializationEvent event) {
        // Hardcoded recipes!
        Items.addRecipes();

        // Register network handler.
        Network.INSTANCE.init();

        // Register event handlers.
        MinecraftForge.EVENT_BUS.register(JobManager.INSTANCE);

        // Register built-in dynamic converter.
        ConverterAPI.addConverter(new ConverterSimpleBlock());
        ConverterAPI.addConverter(new ConverterFallingBlock());
        ConverterAPI.addConverter(new ConverterComplex());

        // Mod integration.
        Integration.init(event);
    }

    public void onPostInit(final FMLPostInitializationEvent event) {
        // Mod integration.
        Integration.postInit(event);
    }

    public void onIMC(final FMLInterModComms.IMCEvent event) {
        for (final FMLInterModComms.IMCMessage message : event.getMessages()) {
            switch (message.key) {
                case API.IMC_BLACKLIST: {
                    if (message.isResourceLocationMessage()) {
                        addBlacklistEntry(message, message.getResourceLocationValue(), new NBTTagCompound());
                    } else if (message.isNBTMessage()) {
                        addBlacklistEntry(message, new ResourceLocation(message.getNBTValue().getString("name")), message.getNBTValue().getCompoundTag("properties"));
                    } else {
                        Architect.getLog().warn("Mod {} tried to add something to the blacklist but the value is not a ResourceLocation or tag compound.", message.getSender());
                    }
                    break;
                }
                case API.IMC_WHITELIST: {
                    if (message.isNBTMessage()) {
                        final ResourceLocation name = new ResourceLocation(message.getNBTValue().getString("name"));
                        final int sortIndex = message.getNBTValue().getInteger("sortIndex");
                        final NBTTagCompound nbtFilter = message.getNBTValue().getCompoundTag("nbtFilter");
                        final NBTTagCompound nbtStripper = message.getNBTValue().getCompoundTag("nbtStripper");
                        Jasons.addToIMCWhitelist(name, new ConverterFilter(nbtFilter, nbtStripper, sortIndex));
                    } else {
                        Architect.getLog().warn("Mod {} tried to add something to the whitelist but the value is not a tag compound.", message.getSender());
                    }
                    break;
                }
                case API.IMC_MAP_TO_BLOCK: {
                    if (message.isNBTMessage()) {
                        final ResourceLocation from = new ResourceLocation(message.getNBTValue().getString("from"));
                        final ResourceLocation to = new ResourceLocation(message.getNBTValue().getString("to"));
                        Jasons.addIMCBlockMapping(from, to);
                        Architect.getLog().info("Mod {} added a mapping from block {} to block {}.", message.getSender(), from, to);
                    } else {
                        Architect.getLog().warn("Mod {} tried to add a block mapping but the value is not a tag compound.", message.getSender());
                    }
                    break;
                }
                case API.IMC_MAP_TO_ITEM: {
                    if (message.isNBTMessage()) {
                        final ResourceLocation from = new ResourceLocation(message.getNBTValue().getString("from"));
                        final ResourceLocation to = new ResourceLocation(message.getNBTValue().getString("to"));
                        Jasons.addIMCItemMapping(from, to);
                        Architect.getLog().info("Mod {} added a mapping from block {} to item {}.", message.getSender(), from, to);
                    } else {
                        Architect.getLog().warn("Mod {} tried to add an item mapping but the value is not a tag compound.", message.getSender());
                    }
                    break;
                }
            }
        }
    }

    // --------------------------------------------------------------------- //

    public Item registerItem(final String name, final Supplier<Item> constructor) {
        final Item item = constructor.get().
                setUnlocalizedName(API.MOD_ID + "." + name).
                setCreativeTab(API.creativeTab).
                setRegistryName(name);
        GameRegistry.register(item);
        return item;
    }

    // --------------------------------------------------------------------- //

    private static void addBlacklistEntry(final FMLInterModComms.IMCMessage message, final ResourceLocation location, final NBTTagCompound propertiesNbt) {
        final Block block = ForgeRegistries.BLOCKS.getValue(location);
        if (block == null) {
            Architect.getLog().info("Mod {} tried to add non-existent block {} to the blacklist.", message.getSender(), location);
            return;
        }

        final IBlockState state = block.getDefaultState();
        final Collection<IProperty<?>> properties = state.getPropertyKeys();
        final Map<IProperty<?>, Comparable<?>> constraintList = new HashMap<>();
        outer:
        for (final String key : propertiesNbt.getKeySet()) {
            if (!(propertiesNbt.getTag(key) instanceof NBTTagString)) {
                Architect.getLog().warn("Mod {} tried to add {} with non-string property value of property '{}' to blacklist.", message.getSender(), location, key);
                continue;
            }
            final String valueName = propertiesNbt.getString(key);
            for (final IProperty<?> property : properties) {
                if (Objects.equals(key, property.getName())) {
                    final Optional<? extends Comparable<?>> value = property.parseValue(valueName);
                    if (value.isPresent()) {
                        constraintList.put(property, value.get());
                    } else {
                        Architect.getLog().warn("Mod {} tried to add {} with non-existent value '{}' for property '{}' to blacklist.", message.getSender(), location, valueName, key);
                    }
                    continue outer;
                }
            }
            Architect.getLog().warn("Mod {} tried to add {} with non-existent property '{}' to blacklist.", message.getSender(), location, key);
        }

        Jasons.addToIMCBlacklist(block, ImmutableMap.copyOf(constraintList));
        Architect.getLog().info("Mod {} added {} with properties {} to the blacklist.", message.getSender(), location, propertiesNbt);
    }
}
