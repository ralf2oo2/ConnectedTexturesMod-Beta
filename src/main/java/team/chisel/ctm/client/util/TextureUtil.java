package team.chisel.ctm.client.util;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.modificationstation.stationapi.api.client.StationRenderAPI;
import net.modificationstation.stationapi.api.client.render.model.ItemModelGenerator;
import net.modificationstation.stationapi.api.client.render.model.ModelLoader;
import net.modificationstation.stationapi.api.client.render.model.SpriteAtlasManager;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.ModelElement;
import net.modificationstation.stationapi.api.client.render.model.json.ModelElementFace;
import net.modificationstation.stationapi.api.client.render.model.json.ModelOverride;
import net.modificationstation.stationapi.api.client.texture.MissingSprite;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.SpriteAtlasTexture;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.client.texture.atlas.AtlasLoader;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.CTM;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.util.ResourceUtil;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;
import team.chisel.ctm.mixin.JsonUnbakedModelAccessor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextureUtil {

    public static Map<Identifier, ICTMTexture<?>> initializeTextures(Set<SpriteIdentifier> textureDependencies, Function<SpriteIdentifier, Sprite> spriteGetter) {
        Map<Identifier, ICTMTexture<?>> textures = new HashMap<>();
        initializeTextures(textureDependencies, textures, spriteGetter);
        return textures;
    }

    public static void initializeTextures(Set<SpriteIdentifier> textureDependencies, Map<Identifier, ICTMTexture<?>> textures, Function<SpriteIdentifier, Sprite> spriteGetter) {
        for (SpriteIdentifier identifier : textureDependencies) {
            Sprite sprite = spriteGetter.apply(identifier);
            CTMMetadataSection metadata = ResourceUtil.getMetadataSafe(sprite);
            if (metadata != null) {
                textures.put(sprite.getContents().getId(), makeTexture(metadata, sprite, spriteGetter));
            }
        }
    }

    public static ICTMTexture<?> makeTexture(CTMMetadataSection metadata, Sprite sprite, Function<SpriteIdentifier, Sprite> spriteGetter) {
        if (metadata.getProxy() != null) {
            try {
                Sprite proxySprite = spriteGetter.apply(toSpriteIdentifier(metadata.getProxy()));
                CTMMetadataSection proxyMetadata = ResourceUtil.getMetadata(proxySprite);
                if (proxyMetadata == null) {
                    return makeDefaultTexture(proxySprite);
                }
                sprite = proxySprite;
                metadata = proxyMetadata;
            } catch (Exception e) {
                CTM.LOGGER.error("Could not load metadata of proxy sprite " + metadata.getProxy() + ". Ignoring proxy and using base texture.", e);
            }
        }

        Identifier[] textures = metadata.getAdditionalTextures();
        int provided = textures.length + 1;
        int required = metadata.getType().requiredTextures();
        Sprite[] sprites = new Sprite[required];
        sprites[0] = sprite;
        for (int i = 1; i < required; i++) {
            Identifier identifier = null;
            if (i < provided) {
                identifier = textures[i - 1];
            }
            sprites[i] = spriteGetter.apply(toSpriteIdentifier(identifier));
        }
        return metadata.getType().makeTexture(new TextureInfo(sprites, Optional.ofNullable(metadata.getExtraData()), metadata.getLayer()));
    }

    public static ICTMTexture<?> makeDefaultTexture(Sprite sprite) {
        return TextureTypeNormal.INSTANCE.makeTexture(new TextureInfo(new Sprite[] { sprite }, Optional.empty(), null));
    }

    public static SpriteIdentifier toSpriteIdentifier(Identifier identifier) {
        return SpriteIdentifier.of(Atlases.GAME_ATLAS_TEXTURE, identifier);
    }

    public static boolean isTextureReference(String texture) {
        return texture.charAt(0) == '#';
    }

    public static Collection<SpriteIdentifier> getTextureDependencies(
            JsonUnbakedModel model,
            Function<Identifier, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences
    ) {
        Set<JsonUnbakedModel> set = Sets.newLinkedHashSet();

        JsonUnbakedModel current = model;
        JsonUnbakedModelAccessor currentAccessor = (JsonUnbakedModelAccessor)(Object) current;

        while (currentAccessor.getParentId() != null && currentAccessor.getParent() == null) {
            set.add(current);

            UnbakedModel unbakedModel = unbakedModelGetter.apply(currentAccessor.getParentId());
            if (unbakedModel == null) {
                CTM.LOGGER.warn("No parent '{}' while loading model '{}'", currentAccessor.getParentId(), current);
            }

            if (unbakedModel instanceof JsonUnbakedModel && set.contains((JsonUnbakedModel) unbakedModel)) {
                CTM.LOGGER.warn("Found 'parent' loop while loading model '{}' in chain: {} -> {}",
                        current, set.stream().map(Object::toString).collect(Collectors.joining(" -> ")), currentAccessor.getParentId());
                unbakedModel = null;
            }

            if (unbakedModel == null) {
                unbakedModel = unbakedModelGetter.apply(ModelLoader.MISSING_ID.id);
            }

            if (!(unbakedModel instanceof JsonUnbakedModel)) {
                throw new IllegalStateException("BlockModel parent has to be a block model.");
            }

            current = (JsonUnbakedModel) unbakedModel;
            currentAccessor = (JsonUnbakedModelAccessor)(Object) current;
        }

        Set<SpriteIdentifier> dependencies = Sets.newHashSet(model.resolveSprite("particle"));

        for (ModelElement modelElement : model.getElements()) {
            for (ModelElementFace modelElementFace : modelElement.faces.values()) {
                SpriteIdentifier spriteIdentifier = model.resolveSprite(modelElementFace.textureId);

                if (Objects.equals(spriteIdentifier.texture, MissingSprite.getMissingSpriteId())) {
                    unresolvedTextureReferences.add(Pair.of(modelElementFace.textureId, model.toString()));
                }

                dependencies.add(spriteIdentifier);
            }
        }

        model.getOverrides().forEach((modelOverride) -> {
            UnbakedModel overrideModel = unbakedModelGetter.apply(modelOverride.getModelId());
            if (overrideModel instanceof JsonUnbakedModel && !Objects.equals(overrideModel, model)) {
                dependencies.addAll(getTextureDependencies((JsonUnbakedModel) overrideModel, unbakedModelGetter, unresolvedTextureReferences));
            }
        });

        if (model.getRootModel() == net.modificationstation.stationapi.api.client.render.model.ModelLoader.GENERATION_MARKER) {
            ItemModelGenerator.LAYERS.forEach((layer) -> dependencies.add(model.resolveSprite(layer)));
        }

        return dependencies;
    }
}
