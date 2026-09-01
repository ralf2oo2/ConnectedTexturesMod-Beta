package team.chisel.ctm.client.texture;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.Submap;
import team.chisel.ctm.client.render.SubmapImpl;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextAlterR;
import team.chisel.ctm.client.texture.type.TextureTypeAlterR;

import java.util.Collections;
import java.util.List;

public class TextureAlterR extends AbstractTexture<TextureTypeAlterR>{
    public TextureAlterR(TextureTypeAlterR type, TextureInfo info) {
        super(type, info);
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, @Nullable TextureContext context) {
        Submap outputQuad;

        int num = context == null ? 0 : ((TextureContextAlterR)context).getTexture();

        outputQuad = switch (num) {
            case 1 -> SubmapImpl.X2[0][1];
            case 2 -> SubmapImpl.X2[1][0];
            case 3 -> SubmapImpl.X2[1][1];
            default -> SubmapImpl.X2[0][0];
        };

        UnbakedQuad quad = unbake(bakedQuad, cullFace);
        quad.setUVBounds(sprites[0]);
        quad.applySubmap(outputQuad);

        return Collections.singletonList(quad.bake());
    }
}
