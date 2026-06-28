package team.chisel.ctm.client.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.modificationstation.stationapi.api.client.render.model.*;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.ModelElement;
import net.modificationstation.stationapi.api.client.render.model.json.ModelElementFace;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import team.chisel.ctm.CTM;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.util.ResourceUtil;
import team.chisel.ctm.client.resource.CTMMetadataReader;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.util.TextureUtil;
import team.chisel.ctm.client.util.VoidSet;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class JsonCTMUnbakedModel implements UnbakedModel {
    private static final CTMOverrideReader OVERRIDE_READER = new CTMOverrideReader();


    private final JsonUnbakedModel parent;
    private final Int2ObjectMap<JsonElement> overrides;

    private final Int2ObjectMap<SpriteIdentifier> identifierOverrides = new Int2ObjectArrayMap<>();
    private final Int2ObjectMap<CTMMetadataSection> metadataOverrides = new Int2ObjectArrayMap<>();
    private final Set<SpriteIdentifier> extraTextureDependencies = new HashSet<>();

    @Override
    public void setParents(Function<Identifier, UnbakedModel> parents) {
        parent.setParents(parents);
    }

    public JsonCTMUnbakedModel(JsonUnbakedModel parent, Int2ObjectMap<JsonElement> overrides) {
        this.parent = parent;
        this.overrides = overrides;

        for (Int2ObjectMap.Entry<JsonElement> entry : this.overrides.int2ObjectEntrySet()) {
            int tintIndex = entry.getIntKey();
            JsonElement element = entry.getValue();
            CTMMetadataSection metadata = null;

            try {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    Identifier identifier = Identifier.of(element.getAsString());
                    SpriteIdentifier spriteId = TextureUtil.toSpriteIdentifier(identifier);
                    identifierOverrides.put(tintIndex, spriteId);
                    extraTextureDependencies.add(spriteId);
                    metadata = ResourceUtil.getMetadata(ResourceUtil.toTextureIdentifier(identifier));
                } else if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    if (!object.has("ctm_version")) {
                        object.add("ctm_version", new JsonPrimitive(1));
                    }
                    OVERRIDE_READER.jsonModel = parent;
                    metadata = OVERRIDE_READER.fromJson(object);

                    int required = metadata.getType().requiredTextures();
                    int provided = metadata.getAdditionalTextures().length + 1;
                    if (required > provided) {
                        CTM.LOGGER.error("Too few textures provided for override {} in model {}: TextureType {} requires {} textures, but {} were provided.", tintIndex, parent.id, metadata.getType(), required, provided);
                    } else if (required < provided) {
                        CTM.LOGGER.warn("Too many textures provided for override {} in model {}: TextureType {} requires {} textures, but {} were provided.", tintIndex, parent.id, metadata.getType(), required, provided);
                    }
                }
            } catch (Exception e) {
                CTM.LOGGER.error("Error processing CTM override.", e);
            }
            if (metadata != null) {
                for (Identifier identifier : metadata.getAdditionalTextures()) {
                    extraTextureDependencies.add(TextureUtil.toSpriteIdentifier(identifier));
                }
                metadataOverrides.put(tintIndex, metadata);
            }
        }
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return parent.getModelDependencies();
    }

    @Override
    public @org.jetbrains.annotations.Nullable BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {

        VoidSet<com.mojang.datafixers.util.Pair<String, String>> voidSet = VoidSet.get();
        Collection<SpriteIdentifier> textureDependencies = TextureUtil.getTextureDependencies(
                this.parent,
                baker::getOrLoadModel,
                voidSet
        );

        Set<SpriteIdentifier> textureSet = new HashSet<>(textureDependencies);

        Map<Identifier, ICTMTexture<?>> textures = TextureUtil.initializeTextures(textureSet, textureGetter);

        Int2ObjectMap<Sprite> spriteOverrides = new Int2ObjectArrayMap<>();
        for (Int2ObjectMap.Entry<SpriteIdentifier> entry : identifierOverrides.int2ObjectEntrySet()) {
            spriteOverrides.put(entry.getIntKey(), textureGetter.apply(entry.getValue()));
        }

        Multimap<Integer, SpriteIdentifier> spriteIds = HashMultimap.create();
        for (ModelElement element : parent.getElements()) {
            for (ModelElementFace face : element.faces.values()) {
                spriteIds.put(face.tintIndex, parent.resolveSprite(face.textureId));
            }
        }

        Map<Pair<Integer, Identifier>, ICTMTexture<?>> textureOverrides = new HashMap<>();
        for (Int2ObjectMap.Entry<CTMMetadataSection> entry : metadataOverrides.int2ObjectEntrySet()) {
            int tintIndex = entry.getIntKey();
            Sprite sprite = spriteOverrides.get(tintIndex);
            if (sprite == null) {
                for (SpriteIdentifier id : spriteIds.get(tintIndex)) {
                    sprite = textureGetter.apply(id);
                    textureOverrides.put(Pair.of(tintIndex, sprite.getContents().getId()), TextureUtil.makeTexture(entry.getValue(), sprite, textureGetter));
                }
            } else {
                textureOverrides.put(Pair.of(tintIndex, sprite.getContents().getId()), TextureUtil.makeTexture(entry.getValue(), sprite, textureGetter));
            }
        }

        return new CTMBakedModel(Objects.requireNonNull(parent.bake(baker, textureGetter, rotationContainer, modelId)), new JsonCTMModelInfo(textures, spriteOverrides, textureOverrides));
    }

    private static class JsonCTMModelInfo implements CTMModelInfo {
        private final List<ICTMTexture<?>> allTextures;
        private final Map<Identifier, ICTMTexture<?>> textures;
        private final Int2ObjectMap<Sprite> spriteOverrides;
        private final Map<Pair<Integer, Identifier>, ICTMTexture<?>> textureOverrides;

        private JsonCTMModelInfo(Map<Identifier, ICTMTexture<?>> textures, Int2ObjectMap<Sprite> spriteOverrides, Map<Pair<Integer, Identifier>, ICTMTexture<?>> textureOverrides) {
            this.textures = textures;
            this.spriteOverrides = spriteOverrides;
            this.textureOverrides = textureOverrides;
            allTextures = ImmutableList.<ICTMTexture<?>>builder()
                                  .addAll(this.textures.values())
                                  .addAll(this.textureOverrides.values())
                                  .build();
        }

        @Override
        public Collection<ICTMTexture<?>> getTextures() {
            return allTextures;
        }

        @Override
        public ICTMTexture<?> getTexture(Identifier identifier) {
            return textures.get(identifier);
        }

        @Override
        public @Nullable Sprite getOverrideSprite(int tintIndex) {
            return spriteOverrides.get(tintIndex);
        }

        @Override
        public @Nullable ICTMTexture<?> getOverrideTexture(int tintIndex, Identifier identifier) {
            return textureOverrides.get(Pair.of(tintIndex, identifier));
        }
    }

    private static class CTMOverrideReader extends CTMMetadataReader {
        private JsonUnbakedModel jsonModel;

        @Override
        public Identifier makeIdentifier(String string) {
            if (TextureUtil.isTextureReference(string)) {
                return jsonModel.resolveSprite(string).texture;
            }
            return super.makeIdentifier(string);
        }
    }
}
