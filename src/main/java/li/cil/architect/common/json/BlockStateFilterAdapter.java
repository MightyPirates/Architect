package li.cil.architect.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.architect.common.config.BlockStateFilter;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public final class BlockStateFilterAdapter implements JsonSerializer<BlockStateFilter>, JsonDeserializer<BlockStateFilter> {
    private static final String NAME_BLOCK = "block";
    private static final String NAME_PROPERTIES = "properties";

    @Override
    public JsonElement serialize(final BlockStateFilter src, final Type typeOfSrc, final JsonSerializationContext context) {
        if (src.getProperties().isEmpty()) {
            return context.serialize(src.getLocation());
        }

        final JsonObject matcherJson = new JsonObject();
        matcherJson.add(NAME_BLOCK, context.serialize(src.getLocation()));
        matcherJson.add(NAME_PROPERTIES, context.serialize(src.getProperties()));
        return matcherJson;
    }

    @Override
    public BlockStateFilter deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            final ResourceLocation location = context.deserialize(json, ResourceLocation.class);
            final Map<String, String> properties = Collections.emptyMap();
            return new BlockStateFilter(location, properties);
        }

        final JsonObject matcherJson = json.getAsJsonObject();
        final ResourceLocation location = context.deserialize(matcherJson.get(NAME_BLOCK), ResourceLocation.class);
        final Map<String, String> properties = context.deserialize(matcherJson.get(NAME_PROPERTIES), Types.MAP_STRING_STRING);
        return new BlockStateFilter(location, properties);
    }
}
