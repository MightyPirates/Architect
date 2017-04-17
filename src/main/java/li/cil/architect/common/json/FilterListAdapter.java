package li.cil.architect.common.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.architect.common.config.converter.AbstractFilterList;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public abstract class FilterListAdapter<F, L extends AbstractFilterList<F>> implements JsonSerializer<L>, JsonDeserializer<L> {
    @Override
    public JsonElement serialize(final L src, final Type typeOfSrc, final JsonSerializationContext context) {
        final Map<ResourceLocation, List<F>> entries = src.getEntries();

        final JsonArray entriesJson = new JsonArray();
        for (final List<F> filters : entries.values()) {
            for (final F filter : filters) {
                entriesJson.add(context.serialize(filter));
            }
        }
        return entriesJson;
    }

    @Override
    public L deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonArray entriesJson = json.getAsJsonArray();

        final L filterList = newInstance();
        for (final JsonElement entryJson : entriesJson) {
            filterList.add(context.deserialize(entryJson, getFilterClass()));
        }
        return filterList;
    }

    protected abstract L newInstance();

    protected abstract Class<F> getFilterClass();
}
