package team.chisel.ctm.client.texture;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import net.modificationstation.stationapi.api.util.math.Vec2f;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.texture.context.TextureContextPosition;
import team.chisel.ctm.client.texture.type.TextureTypeEldritch;
import team.chisel.ctm.client.util.MathUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TextureEldritch extends AbstractTexture<TextureTypeEldritch> {
    private static final Random RANDOM = new Random();

    public TextureEldritch(TextureTypeEldritch type, TextureInfo info) {
        super(type, info);
    }

    @Override
    public List<BakedQuad> transformQuad(BakedQuad bakedQuad, Direction cullFace, TextureContext context) {
        UnbakedQuad quad = unbake(bakedQuad, cullFace);

        float[] bounds = quad.getSmallestUVBounds();
        Vec2f min = new Vec2f(bounds[0], bounds[1]);
        Vec2f max = new Vec2f(bounds[2], bounds[3]);

        BlockPos pos;
        if (context instanceof TextureContextPosition) {
            pos = ((TextureContextPosition) context).getPosition();
        } else {
            pos = BlockPos.ORIGIN;
        }
        RANDOM.setSeed(MathHelper.hashCode(pos.x, pos.y, pos.z) + quad.lightFace.ordinal());

        float xOffset = getRandomOffset();
        float yOffset = getRandomOffset();

        UnbakedQuad[] quads = quad.toQuadrants();
        for (int i = 0; i < quads.length; i++) {
            UnbakedQuad quadrant = quads[i];
            if (quadrant != null) {
                for (int j = 0; j < 4; j++) {
                    Vec2f uv = new Vec2f(quadrant.vertexes[j].u, quadrant.vertexes[j].v);
                    if (uv.x != min.x && uv.x != max.x && uv.y != min.y && uv.y != max.y) {
                        float xInterp = MathUtil.getLerpProgress(uv.x, min.x, max.x) + xOffset;
                        float yInterp = MathUtil.getLerpProgress(uv.y, min.y, max.y) + yOffset;
                        quadrant.vertexes[j].u = MathHelper.lerp(xInterp, min.x, max.x);
                        quadrant.vertexes[j].v = MathHelper.lerp(yInterp, min.y, max.y);
                    }
                }
            }
        }

        return Arrays.stream(quads).map(UnbakedQuad::bake).toList();
    }

    private static float getRandomOffset() {
        return (float) RANDOM.nextGaussian() * 0.08F;
    }
}
