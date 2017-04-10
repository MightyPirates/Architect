package li.cil.architect.common.json;

import com.google.gson.reflect.TypeToken;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;

public class Types {
    public static final Type SET_RESOURCE_LOCATION = new TypeToken<HashSet<ResourceLocation>>(){}.getType();
    public static final Type MAP_RESOURCE_LOCATION = new TypeToken<HashMap<ResourceLocation, ResourceLocation>>(){}.getType();
}
