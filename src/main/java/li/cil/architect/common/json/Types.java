package li.cil.architect.common.json;

import com.google.gson.reflect.TypeToken;
import li.cil.architect.common.config.ConverterFilter;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class Types {
    public static final Type SET_RESOURCE_LOCATION = new TypeToken<LinkedHashSet<ResourceLocation>>(){}.getType();
    public static final Type MAP_CONVERTER_FILTER = new TypeToken<LinkedHashMap<ResourceLocation, ConverterFilter>>(){}.getType();
    public static final Type MAP_RESOURCE_LOCATION = new TypeToken<LinkedHashMap<ResourceLocation, ResourceLocation>>(){}.getType();
}
