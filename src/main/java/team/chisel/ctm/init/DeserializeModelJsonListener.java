package team.chisel.ctm.init;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import team.chisel.ctm.CTM;
import team.chisel.ctm.client.resource.ModelParser;
import team.chisel.ctm.client.resource.ModelParserV1;
import team.chisel.ctm.event.DeserializeModelJsonEvent;

import java.util.HashMap;
import java.util.Map;

public class DeserializeModelJsonListener {
    private static final Map<Integer, ModelParser> PARSERS = new ImmutableMap.Builder<Integer, ModelParser>()
                                                                     .put(1, new ModelParserV1())
                                                                     .build();

    public static final Map<JsonUnbakedModel, Int2ObjectMap<JsonElement>> jsonOverrideMap = new HashMap<>();

    @EventListener
    public void onDeserializeModelJson(DeserializeModelJsonEvent event) {
        if(event.jsonElement.isJsonObject()) {
            JsonObject jsonObject = event.jsonElement.getAsJsonObject();
            if (jsonObject.has("ctm_version")) {
                ModelParser parser = PARSERS.get(jsonObject.get("ctm_version").getAsInt());
                if (parser == null) {
                    CTM.LOGGER.error("Invalid \"ctm_version\" in model {}.", event.jsonElement);
                } else {
                    Int2ObjectMap<JsonElement> overrides = parser.parse(event.jsonModel, jsonObject, event.type, event.context);
                    if (overrides != null) {
                        jsonOverrideMap.put(event.jsonModel, overrides);
                    }
                }
            }
        }
    }
}
