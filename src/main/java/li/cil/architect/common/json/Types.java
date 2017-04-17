package li.cil.architect.common.json;

import com.google.gson.reflect.TypeToken;
import li.cil.architect.common.config.ConverterFilter;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

public class Types {
    public static final Type MAP_STRING_STRING = new TypeToken<LinkedHashMap<String, String>>(){}.getType();
    public static final Type MAP_CONVERTER_FILTER = new TypeToken<LinkedHashMap<ResourceLocation, ConverterFilter>>(){}.getType();
    public static final Type MAP_RESOURCE_LOCATION = new TypeToken<LinkedHashMap<ResourceLocation, ResourceLocation>>(){}.getType();
}
