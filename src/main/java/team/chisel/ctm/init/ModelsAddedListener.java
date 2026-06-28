package team.chisel.ctm.init;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.render.model.ModelLoader;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import team.chisel.ctm.api.event.ModelsAddedEvent;
import team.chisel.ctm.client.model.JsonCTMUnbakedModel;
import team.chisel.ctm.client.util.VoidSet;
import team.chisel.ctm.mixin.JsonUnbakedModelAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ModelsAddedListener {
    private Map<JsonUnbakedModel, Int2ObjectMap<JsonElement>> jsonOverrideMap;

    @EventListener
    public void modelsAdded(ModelsAddedEvent event) {
        Map<Identifier, UnbakedModel> wrappedModels = new HashMap<>();

        UnbakedModel missingModel = event.unbakedModels.get(ModelLoader.MISSING_ID);

        Function<Identifier, UnbakedModel> unbakedModelGetter = id -> {
            UnbakedModel unbakedModel = event.unbakedModels.get(id);
            if (unbakedModel == null) {
                return missingModel;
            }
            return unbakedModel;
        };
        VoidSet<Pair<String, String>> voidSet = VoidSet.get();


        Collection<SpriteIdentifier> dependencies = new ArrayList<>();

        for (Map.Entry<Identifier, UnbakedModel> entry : event.unbakedModels.entrySet()) {
            Identifier identifier = entry.getKey();
            UnbakedModel unbakedModel = entry.getValue();

            if(unbakedModel instanceof JsonUnbakedModel jsonModel) {
                if(jsonModel.getRootModel() == ModelLoader.GENERATION_MARKER || jsonModel.getRootModel() == ModelLoader.BLOCK_ENTITY_MARKER) {
                    continue;
                }

                Int2ObjectMap<JsonElement> overrides = getOverrides(jsonModel);
                if (overrides != null && !overrides.isEmpty()) {
                    // Wrap models with overrides
                    wrappedModels.put(identifier, new JsonCTMUnbakedModel(jsonModel, overrides));
                    continue;
                }
            }
        }
        jsonOverrideMap.clear();

        // Inject wrapped models
        for (Map.Entry<Identifier, UnbakedModel> entry : wrappedModels.entrySet()) {
            Identifier identifier = entry.getKey();
            UnbakedModel wrapped = entry.getValue();

            event.unbakedModels.put(identifier, wrapped);
            if (event.modelsToBake.containsKey(identifier)) {
                event.modelsToBake.put(identifier, wrapped);
            }
        }
    }

    private Int2ObjectMap<JsonElement> getOverrides(JsonUnbakedModel unbakedModel) {
        Int2ObjectMap<JsonElement> overrides = jsonOverrideMap.get(unbakedModel);
        if (overrides == null) {
            JsonUnbakedModel parent = ((JsonUnbakedModelAccessor) unbakedModel).getParent();
            if (parent != null) {
                return getOverrides(parent);
            }
            return null;
        }
        return overrides;
    }
}
