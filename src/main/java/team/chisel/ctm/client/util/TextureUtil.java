package team.chisel.ctm.client.util;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.modificationstation.stationapi.api.client.render.model.ItemModelGenerator;
import net.modificationstation.stationapi.api.client.render.model.ModelLoader;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.ModelElement;
import net.modificationstation.stationapi.api.client.render.model.json.ModelElementFace;
import net.modificationstation.stationapi.api.client.texture.MissingSprite;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.CTM;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.util.ResourceUtil;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;

import java.util.*;
import java.util.function.Function;

public class TextureUtil {

    public static Map<Identifier, CTMTexture<?>> initializeTextures(Set<SpriteIdentifier> textureDependencies, Function<SpriteIdentifier, Sprite> spriteGetter) {
        Map<Identifier, CTMTexture<?>> textures = new HashMap<>();
        initializeTextures(textureDependencies, textures, spriteGetter);
        return textures;
    }

    public static void initializeTextures(Set<SpriteIdentifier> textureDependencies, Map<Identifier, CTMTexture<?>> textures, Function<SpriteIdentifier, Sprite> spriteGetter) {
        for (SpriteIdentifier identifier : textureDependencies) {
            Sprite sprite = spriteGetter.apply(identifier);
            CTMMetadataSection metadata = ResourceUtil.getMetadataSafe(sprite);
            if (metadata != null) {
                textures.put(sprite.getContents().getId(), makeTexture(metadata, sprite, spriteGetter));
            }
        }
    }

    public static CTMTexture<?> makeTexture(CTMMetadataSection metadata, Sprite sprite, Function<SpriteIdentifier, Sprite> spriteGetter) {
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

    public static CTMTexture<?> makeDefaultTexture(Sprite sprite) {
        return TextureTypeNormal.INSTANCE.makeTexture(new TextureInfo(new Sprite[] { sprite }, Optional.empty(), null));
    }

    public static SpriteIdentifier toSpriteIdentifier(Identifier identifier) {
        return SpriteIdentifier.of(Atlases.GAME_ATLAS_TEXTURE, identifier);
    }

    public static boolean isTextureReference(String texture) {
        return texture.charAt(0) == '#';
    }

    public static Collection<SpriteIdentifier> getTextureDependencies(JsonUnbakedModel model, Function<Identifier, UnbakedModel> modelLoader, Set<Pair<String, String>> unresolvedTextureReferences) {
        Set<SpriteIdentifier> textures = Sets.newHashSet(model.resolveSprite("particle"));

        for (ModelElement modelElement : model.getElements()) {
            SpriteIdentifier spriteIdentifier;
            for (ModelElementFace modelElementFace : modelElement.faces.values()) {
                spriteIdentifier = model.resolveSprite(modelElementFace.textureId);

                if (spriteIdentifier.texture == MissingSprite.getMissingSpriteId()) {
                    unresolvedTextureReferences.add(Pair.of(modelElementFace.textureId, model.id));
                }

                textures.add(spriteIdentifier);
            }
        }

        if (model.getRootModel() == ModelLoader.GENERATION_MARKER) {
            ItemModelGenerator.LAYERS.forEach((string) -> textures.add(model.resolveSprite(string)));
        }

        return textures;
    }
}
