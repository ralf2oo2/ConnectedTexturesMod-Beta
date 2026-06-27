package team.chisel.ctm.client.util;

import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import team.chisel.ctm.mixin.BakedQuadAccessor;

import java.util.Arrays;

public class BakedQuadRetextured extends BakedQuad {
    private final Sprite texture;

    public BakedQuadRetextured(BakedQuad quad, Sprite texture) {
        super(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length), quad.getColorIndex(), quad.getFace(), texture, quad.hasShade(), quad.getEmission());
        this.texture = texture;

        Sprite oldSprite = ((BakedQuadAccessor)quad).getSprite();
        if (oldSprite != null) {
            this.remapQuad(oldSprite);
        }
    }

    private void remapQuad(Sprite oldSprite) {
        for (int i = 0; i < 4; ++i) {
            int offset = i * 8;
            int uIndex = offset + 4;
            int vIndex = offset + 5;

            float oldU = Float.intBitsToFloat(this.vertexData[uIndex]);
            float oldV = Float.intBitsToFloat(this.vertexData[vIndex]);

            float unInterpolatedU = getUnInterpolatedU(oldSprite, oldU);
            float unInterpolatedV = getUnInterpolatedV(oldSprite, oldV);

            float newU = this.texture.getFrameU(unInterpolatedU);
            float newV = this.texture.getFrameV(unInterpolatedV);

            this.vertexData[uIndex] = Float.floatToRawIntBits(newU);
            this.vertexData[vIndex] = Float.floatToRawIntBits(newV);
        }
    }

    private static float getUnInterpolatedU(Sprite sprite, float u) {
        float f = sprite.getMaxU() - sprite.getMinU();
        return (u - sprite.getMinU()) / f * 16.0F;
    }

    private static float getUnInterpolatedV(Sprite sprite, float v) {
        float f = sprite.getMaxV() - sprite.getMinV();
        return (v - sprite.getMinV()) / f * 16.0F;
    }
}
