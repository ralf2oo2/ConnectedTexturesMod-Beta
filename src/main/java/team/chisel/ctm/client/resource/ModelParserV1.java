package team.chisel.ctm.client.resource;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.CTM;

import java.lang.reflect.Type;
import java.util.Map;

public class ModelParserV1 implements ModelParser {
    private static final Gson GSON = new Gson();
    private static final Type OVERRIDE_TYPE = new TypeToken<Map<String, JsonElement>>() { } .getType();

    @Override
    @NotNull
    public Int2ObjectMap<JsonElement> parse(JsonUnbakedModel jsonModel, JsonObject jsonObject, Type type, JsonDeserializationContext context) {
        try {
            Map<String, JsonElement> unparsedOverrides = GSON.fromJson(jsonObject.getAsJsonObject("ctm_overrides"), OVERRIDE_TYPE);
            if (unparsedOverrides != null && unparsedOverrides.size() > 0) {
                Int2ObjectMap<JsonElement> overrides = new Int2ObjectArrayMap<>(unparsedOverrides.size());
                for (Map.Entry<String, JsonElement> entry : unparsedOverrides.entrySet()) {
                    try {
                        int tintIndex = Integer.parseInt(entry.getKey());
                        overrides.put(tintIndex, entry.getValue());
                    } catch (NumberFormatException e) {
                        CTM.LOGGER.error("Error parsing model {}: \"{}\" is not a valid tintindex.", jsonObject, entry.getKey());
                    }
                }
                return overrides;
            }
        } catch (Exception e) {
            CTM.LOGGER.error("Error parsing model " + jsonObject + ".", e);
        }
        return null;
    }
}
