package li.cil.architect.common.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.architect.common.config.Blacklist;
import li.cil.architect.common.config.BlockStateFilter;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class BlacklistAdapter implements JsonSerializer<Blacklist>, JsonDeserializer<Blacklist> {
    @Override
    public JsonElement serialize(final Blacklist src, final Type typeOfSrc, final JsonSerializationContext context) {
        final Map<ResourceLocation, List<BlockStateFilter>> entries = src.getEntries();

        final JsonArray entriesJson = new JsonArray();
        for (final List<BlockStateFilter> filters : entries.values()) {
            for (final BlockStateFilter filter : filters) {
                entriesJson.add(context.serialize(filter));
            }
        }
        return entriesJson;
    }

    @Override
    public Blacklist deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonArray()) {
            throw new JsonParseException("The blacklist should be a JSON array.");
        }
        final JsonArray entriesJson = json.getAsJsonArray();

        final Blacklist blacklist = new Blacklist();
        for (final JsonElement entryJsonMixed : entriesJson) {
            blacklist.add(context.deserialize(entryJsonMixed, BlockStateFilter.class));
        }
        return blacklist;
    }
}
