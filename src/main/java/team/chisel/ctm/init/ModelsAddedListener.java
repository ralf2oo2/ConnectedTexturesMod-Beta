package team.chisel.ctm.init;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.render.model.ModelLoader;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.api.event.ModelsAddedEvent;
import team.chisel.ctm.api.util.ResourceUtil;
import team.chisel.ctm.client.model.CTMUnbakedModel;
import team.chisel.ctm.client.model.JsonCTMUnbakedModel;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.TextureUtil;
import team.chisel.ctm.client.util.VoidSet;
import team.chisel.ctm.mixin.JsonUnbakedModelAccessor;

import java.util.*;
import java.util.function.Function;

public class ModelsAddedListener {
    private Map<JsonUnbakedModel, Int2ObjectMap<JsonElement>> jsonOverrideMap;

    @EventListener
    public void modelsAdded(ModelsAddedEvent event) {
        Map<Identifier, UnbakedModel> wrappedModels = new HashMap<>();

        UnbakedModel missingModel = event.unbakedModels.get(ModelLoader.MISSING_ID);

        Function<Identifier, UnbakedModel> unbakedModelGetter = id -> {
            UnbakedModel unbakedModel = event.unbakedModels.get(id);
            if (!(unbakedModel instanceof JsonUnbakedModel)) {
                return missingModel;
            }
            return unbakedModel;
        };
        VoidSet<Pair<String, String>> voidSet = VoidSet.get();

        for (Map.Entry<Identifier, UnbakedModel> entry : event.unbakedModels.entrySet()) {
            Identifier identifier = entry.getKey();
            UnbakedModel unbakedModel = entry.getValue();

            Collection<SpriteIdentifier> dependencies = null;

            if(unbakedModel instanceof JsonUnbakedModel jsonModel) {
                if(jsonModel.getRootModel() == ModelLoader.GENERATION_MARKER || jsonModel.getRootModel() == ModelLoader.BLOCK_ENTITY_MARKER) {
                    continue;
                }

                dependencies = TextureUtil.getTextureDependencies(jsonModel, unbakedModelGetter, voidSet);
                Int2ObjectMap<JsonElement> overrides = getOverrides(jsonModel);
                if (overrides != null && !overrides.isEmpty()) {
                    // Wrap models with overrides
                    wrappedModels.put(identifier, new JsonCTMUnbakedModel(jsonModel, overrides));
                    continue;
                }
            }
            if(dependencies != null) {
                for (SpriteIdentifier spriteId : dependencies) {
                    CTMMetadataSection metadata = ResourceUtil.getMetadataSafe(ResourceUtil.toTextureIdentifier(spriteId.texture));
                    if (metadata != null) {
                        // At least one texture has CTM metadata, so this model should be wrapped
                        wrappedModels.put(identifier, new CTMUnbakedModel(unbakedModel));
                        break;
                    }
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
            JsonUnbakedModel parent = ((JsonUnbakedModelAccessor)(Object) unbakedModel).getParent();
            if (parent != null) {
                return getOverrides(parent);
            }
            return null;
        }
        return overrides;
    }
}
