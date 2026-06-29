package team.chisel.ctm.client.texture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.util.BlockRenderLayer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Abstract implementation of {@link CTMTexture}
 */
public abstract class AbstractTexture<T extends TextureType> implements CTMTexture<T> {

    protected T type;
    @NotNull
    protected Sprite[] sprites;
    protected BlockRenderLayer layer;
    protected boolean hasLight;
    protected int skyLight;
    protected int blockLight;

    @Deprecated
    public AbstractTexture(T type, BlockRenderLayer layer, Sprite... sprites) {
        this.type = type;
        this.layer = layer;
        this.sprites = sprites;
        this.skyLight = this.blockLight = 0;
    }

    public AbstractTexture(T type, TextureInfo info) {
        this.type = type;
        sprites = info.getSprites();

        boolean isEmissive = false;
        if (info.getExtraInfo().isPresent()) {
            JsonElement light = info.getExtraInfo().get().get("light");
            if (light != null) {
                if (light.isJsonPrimitive()) {
                    hasLight = true;
                    skyLight = blockLight = parseLightValue(light);
                } else if (light.isJsonObject()) {
                    this.hasLight = true;
                    JsonObject lightObject = light.getAsJsonObject();
                    skyLight = parseLightValue(lightObject.get("sky"));
                    blockLight = parseLightValue(lightObject.get("block"));
                }
                if (skyLight == 15 && blockLight == 15) {
                    isEmissive = true;
                }
            }
        }

        layer = info.getRenderLayer();
    }

    private static int parseLightValue(@Nullable JsonElement data) {
        if (data != null && data.isJsonPrimitive() && data.getAsJsonPrimitive().isNumber()) {
            return MathHelper.clamp(data.getAsInt(), 0, 15);
        }
        return 0;
    }

    @Override
    public T getType() {
        return type;
    }

    @Override
    public Sprite getParticle() {
        return sprites[0];
    }

    @Override
    public Collection<Identifier> getTextures() {
        return Arrays.stream(sprites).map(Sprite::getAtlasId).collect(Collectors.toList());
    }

    protected UnbakedQuad unbake(BakedQuad bakedQuad, Direction cullFace) {
        UnbakedQuad quad = new UnbakedQuad(bakedQuad);
        quad.cullFace = cullFace;
        quad.layer = layer;
        if (hasLight) {
            quad.setLight(skyLight, blockLight);
        }
        return quad;
    }

    @Override
    public BlockRenderLayer getLayer() {
        return layer;
    }
}
