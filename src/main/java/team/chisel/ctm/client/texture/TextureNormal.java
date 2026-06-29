package team.chisel.ctm.client.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.type.TextureTypeNormal;

import java.util.List;

/**
 * CTM texture for a normal texture.
 */
public class TextureNormal extends AbstractTexture<TextureTypeNormal> {
    public TextureNormal(TextureTypeNormal type, TextureInfo info) {
        super(type, info);
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
        UnbakedQuad quad = unbake(bakedQuad, cullFace);
        quad.setUVBounds(sprites[0]);
        return List.of(quad.bake());
    }
}
