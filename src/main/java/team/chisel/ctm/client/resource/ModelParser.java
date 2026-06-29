package team.chisel.ctm.client.resource;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public interface ModelParser {
    @Nullable
    Int2ObjectMap<JsonElement> parse(JsonUnbakedModel jsonModel, JsonObject jsonObject, Type type, JsonDeserializationContext context);
}
