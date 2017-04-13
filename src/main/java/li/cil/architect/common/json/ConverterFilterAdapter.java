package li.cil.architect.common.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.architect.api.converter.SortIndex;
import li.cil.architect.common.config.ConverterFilter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConverterFilterAdapter implements JsonSerializer<ConverterFilter>, JsonDeserializer<ConverterFilter> {
    private static final String KEY_SORT_INDEX = "sortIndex";
    private static final String KEY_NBT_FILTER = "nbt";
    private static final String SORT_INDEX_SOLID = "solid";
    private static final String SORT_INDEX_FALLING = "falling";
    private static final String SORT_INDEX_ATTACHED = "attached";
    private static final String SORT_INDEX_FLUID = "fluid";

    // --------------------------------------------------------------------- //
    // JsonSerializer

    @Override
    public JsonElement serialize(final ConverterFilter src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject converterJson = new JsonObject();
        final int sortIndex = src.getSortIndex();
        if (sortIndex == SortIndex.SOLID_BLOCK) {
            // Default, don't need to save.
        } else if (sortIndex == SortIndex.FALLING_BLOCK) {
            converterJson.addProperty(KEY_SORT_INDEX, SORT_INDEX_FALLING);
        } else if (sortIndex == SortIndex.ATTACHED_BLOCK) {
            converterJson.addProperty(KEY_SORT_INDEX, SORT_INDEX_ATTACHED);
        } else if (sortIndex == SortIndex.FLUID_BLOCK) {
            converterJson.addProperty(KEY_SORT_INDEX, SORT_INDEX_FLUID);
        } else {
            converterJson.addProperty(KEY_SORT_INDEX, src.getSortIndex());
        }
        if (!src.getNbtFilter().isEmpty()) {
            converterJson.add(KEY_NBT_FILTER, serialize(src.getNbtFilter()));
        }
        return converterJson;
    }

    // --------------------------------------------------------------------- //
    // JsonDeserializer

    @Override
    public ConverterFilter deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            return new ConverterFilter(Collections.emptyMap(), 0);
        }
        final JsonObject converterJson = json.getAsJsonObject();
        final JsonElement sortIndexJson = converterJson.get(KEY_SORT_INDEX);
        final int sortIndex;
        if (sortIndexJson == null || SORT_INDEX_SOLID.equals(sortIndexJson.getAsString())) {
            sortIndex = SortIndex.SOLID_BLOCK;
        } else if (SORT_INDEX_FALLING.equals(sortIndexJson.getAsString())) {
            sortIndex = SortIndex.FALLING_BLOCK;
        } else if (SORT_INDEX_ATTACHED.equals(sortIndexJson.getAsString())) {
            sortIndex = SortIndex.ATTACHED_BLOCK;
        } else if (SORT_INDEX_FLUID.equals(sortIndexJson.getAsString())) {
            sortIndex = SortIndex.FLUID_BLOCK;
        } else {
            sortIndex = sortIndexJson.getAsInt();
        }
        final JsonElement nbtJson = converterJson.get(KEY_NBT_FILTER);
        final Map<String, Object> nbtFilter;
        if (nbtJson != null) {
            nbtFilter = deserialize(nbtJson);
        } else {
            nbtFilter = Collections.emptyMap();
        }
        return new ConverterFilter(nbtFilter, sortIndex);
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("unchecked")
    private JsonObject serialize(final Map<String, Object> filter) {
        final JsonObject filterJson = new JsonObject();
        for (final String key : filter.keySet()) {
            final Object value = filter.get(key);
            if (value instanceof Map) {
                filterJson.add(key, serialize((Map<String, Object>) value));
            } else {
                filterJson.addProperty(key, value.toString());
            }
        }
        return filterJson;
    }

    private Map<String, Object> deserialize(final JsonElement value) {
        if (!value.isJsonObject()) {
            return Collections.emptyMap();
        }
        final JsonObject valueJson = value.getAsJsonObject();
        final Map<String, Object> result = new HashMap<>();
        for (final Map.Entry<String, JsonElement> entry : valueJson.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                result.put(entry.getKey(), deserialize(entry.getValue()));
            } else {
                result.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return result;
    }
}
