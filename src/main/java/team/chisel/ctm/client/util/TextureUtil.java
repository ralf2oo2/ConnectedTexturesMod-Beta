package team.chisel.ctm.client.util;

import com.mojang.datafixers.util.Either;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.ModelOverride;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import team.chisel.ctm.CTM;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.util.ResourceUtil;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;
import team.chisel.ctm.mixin.JsonUnbakedModelAccessor;

import java.util.*;
import java.util.function.Function;

public class TextureUtil {

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
        return SpriteIdentifier.of(Atlases.getTerrain().id, identifier);
    }

    public static boolean isTextureReference(String texture) {
        return texture.charAt(0) == '#';
    }

    public static Collection<SpriteIdentifier> getTextureDependencies(
            JsonUnbakedModel model,
            Function<Identifier, UnbakedModel> unbakedModelGetter,
            Set<Pair<String, String>> unresolvedTextureReferences
    ) {
        Set<SpriteIdentifier> dependencies = new HashSet<>();
        gatherDependencies(model, unbakedModelGetter, unresolvedTextureReferences, dependencies, new HashSet<>());
        return dependencies;
    }

    private static void gatherDependencies(JsonUnbakedModel model, Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences, Set<SpriteIdentifier> accumulator, Set<JsonUnbakedModel> visited) {
        if (model == null || !visited.add(model)) {
            return;
        }

        for (Either<SpriteIdentifier, String> either : ((JsonUnbakedModelAccessor)model).getTextureMap().values()) {
            either.left().ifPresent(accumulator::add);
        }

        if (((JsonUnbakedModelAccessor)model).getParent() != null) {
            gatherDependencies(((JsonUnbakedModelAccessor)model).getParent(), unbakedModelGetter, unresolvedTextureReferences, accumulator, visited);
        } else if (((JsonUnbakedModelAccessor)model).getParentId() != null) {
            UnbakedModel unbakedParent = unbakedModelGetter.apply(((JsonUnbakedModelAccessor)model).getParentId());
            if (unbakedParent instanceof JsonUnbakedModel jsonParent) {
                gatherDependencies(jsonParent, unbakedModelGetter, unresolvedTextureReferences, accumulator, visited);
            }
        }

        for (ModelOverride override : model.getOverrides()) {
            UnbakedModel overrideModel = unbakedModelGetter.apply(override.getModelId());
            if (overrideModel instanceof JsonUnbakedModel jsonOverride) {
                gatherDependencies(jsonOverride, unbakedModelGetter, unresolvedTextureReferences, accumulator, visited);
            }
        }
    }
}
