package team.chisel.ctm.client.texture;

import com.google.common.collect.ObjectArrays;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Getter;
import lombok.ToString;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.resource.metadata.ResourceMetadataReader;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.CTM;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.util.ResourceUtil;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.type.TextureTypeRegistry;
import team.chisel.ctm.client.util.BlockRenderLayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public interface IMetadataSectionCTM {

    public static final String SECTION_NAME = "ctm";

    int getVersion();

    ITextureType getType();

    BlockRenderLayer getLayer();

    Identifier[] getAdditionalTextures();

    @Nullable
    String getProxy();

    JsonObject getExtraData();

    default ICTMTexture<?> makeTexture(Sprite sprite, Function<Identifier, Sprite> bakedTextureGetter) {
        IMetadataSectionCTM meta = this;
        if (getProxy() != null) {
            Sprite proxySprite = bakedTextureGetter.apply(Identifier.of(getProxy()));
            try {
                meta = ResourceUtil.getMetadata(proxySprite);
                if (meta == null) {
                    meta = new V1();
                }
                sprite = proxySprite;
            } catch (IOException e) {
                CTM.LOGGER.error("Could not parse metadata of proxy, ignoring proxy and using base texture." + getProxy(), e);
                meta = this;
            }
        }
        return meta.getType().makeTexture(new TextureInfo(
                Arrays.stream(ObjectArrays.concat(sprite.getAtlasId(), meta.getAdditionalTextures())).map(bakedTextureGetter::apply).toArray(Sprite[]::new),
                Optional.of(meta.getExtraData()),
                meta.getLayer()
        ));
    }

    @ToString
    @Getter
    public static class V1 implements IMetadataSectionCTM {

        private ITextureType type = TextureTypeRegistry.getType("NORMAL");
        private BlockRenderLayer layer = BlockRenderLayer.SOLID;
        private String proxy;
        private Identifier[] additionalTextures = new Identifier[0];
        private JsonObject extraData = new JsonObject();

        @Override
        public int getVersion() {
            return 1;
        }

        public static IMetadataSectionCTM fromJson(JsonObject obj) throws JsonParseException {
            V1 ret = new V1();

            if (obj.has("proxy")) {
                JsonElement proxyEle = obj.get("proxy");
                if (proxyEle.isJsonPrimitive() && proxyEle.getAsJsonPrimitive().isString()) {
                    ret.proxy = proxyEle.getAsString();
                }

                if (obj.entrySet().stream().filter(e -> e.getKey().equals("ctm_version")).count() > 1) {
                    throw new JsonParseException("Cannot define other fields when using proxy");
                }
            }

            if (obj.has("type")) {
                JsonElement typeEle = obj.get("type");
                if (typeEle.isJsonPrimitive() && typeEle.getAsJsonPrimitive().isString()) {
                    ITextureType type = TextureTypeRegistry.getType(typeEle.getAsString());
                    if (type == null) {
                        throw new JsonParseException("Invalid render type given: " + typeEle);
                    } else {
                        ret.type = type;
                    }
                }
            }

            if (obj.has("layer")) {
                JsonElement layerEle = obj.get("layer");
                if (layerEle.isJsonPrimitive() && layerEle.getAsJsonPrimitive().isString()) {
                    try {
                        ret.layer = BlockRenderLayer.valueOf(layerEle.getAsString());
                    } catch (IllegalArgumentException e) {
                        throw new JsonParseException("Invalid block layer given: " + layerEle);
                    }
                }
            }

            if (obj.has("textures")) {
                JsonElement texturesEle = obj.get("textures");
                if (texturesEle.isJsonArray()) {
                    JsonArray texturesArr = texturesEle.getAsJsonArray();
                    ret.additionalTextures = new Identifier[texturesArr.size()];
                    for (int i = 0; i < texturesArr.size(); i++) {
                        JsonElement e = texturesArr.get(i);
                        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
                            ret.additionalTextures[i] = Identifier.of(e.getAsString());
                        }
                    }
                }
            }

            if (obj.has("extra") && obj.get("extra").isJsonObject()) {
                ret.extraData = obj.getAsJsonObject("extra");
            }
            return ret;
        }
    }

    public static class Serializer implements ResourceMetadataReader<IMetadataSectionCTM> {

        @Override
        public @Nullable IMetadataSectionCTM fromJson(@Nullable JsonObject json) throws JsonParseException {
            if (json != null && json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                if (obj.has("ctm_version")) {
                    JsonElement version = obj.get("ctm_version");
                    if (version.isJsonPrimitive() && version.getAsJsonPrimitive().isNumber()) {
                        switch (version.getAsInt()) {
                            case 1:
                                return V1.fromJson(obj);
                        }
                    }
                } else {
                    throw new JsonParseException("Found ctm section without ctm_version");
                }
            }
            return null;
        }

        @Override
        public @Nonnull String getKey() {
            return SECTION_NAME;
        }
    }

}
