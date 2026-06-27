package team.chisel.ctm.client.util;

import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.CTM;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.util.ResourceUtil;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.resource.CTMMetadataSection;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
}
