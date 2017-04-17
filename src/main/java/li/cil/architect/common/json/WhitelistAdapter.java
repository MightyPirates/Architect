package li.cil.architect.common.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.architect.common.config.TileEntityFilter;
import li.cil.architect.common.config.Whitelist;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class WhitelistAdapter implements JsonSerializer<Whitelist>, JsonDeserializer<Whitelist> {
    @Override
    public JsonElement serialize(final Whitelist src, final Type typeOfSrc, final JsonSerializationContext context) {
        final Map<ResourceLocation, List<TileEntityFilter>> entries = src.getEntries();

        final JsonArray entriesJson = new JsonArray();
        for (final List<TileEntityFilter> filters : entries.values()) {
            for (final TileEntityFilter filter : filters) {
                entriesJson.add(context.serialize(filter));
            }
        }
        return entriesJson;
    }

    @Override
    public Whitelist deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) { // Compat with old format.
            final Whitelist whitelist = new Whitelist();
            for (final Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                final JsonObject value = entry.getValue().getAsJsonObject();
                value.addProperty("block", entry.getKey());
                whitelist.add(context.deserialize(value, TileEntityFilter.class));
            }
            return whitelist;
        }

        if (!json.isJsonArray()) {
            throw new JsonParseException("The whitelist should be a JSON array.");
        }
        final JsonArray entriesJson = json.getAsJsonArray();

        final Whitelist whitelist = new Whitelist();
        for (final JsonElement entryJson : entriesJson) {
            whitelist.add(context.deserialize(entryJson, TileEntityFilter.class));
        }
        return whitelist;
    }
}
