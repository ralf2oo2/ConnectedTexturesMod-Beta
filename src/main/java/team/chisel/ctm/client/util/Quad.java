package team.chisel.ctm.client.util;

import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import lombok.*;
import net.modificationstation.stationapi.api.client.StationRenderAPI;
import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.client.texture.MissingSprite;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.math.Direction;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import team.chisel.ctm.api.texture.ISubmap;
import team.chisel.ctm.api.util.NonnullType;
import team.chisel.ctm.mixin.BakedQuadAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@ToString(of = { "vertPos", "vertUv" })
public class Quad {

    @Deprecated
    public static final ISubmap TOP_LEFT = Submap.X2[0][0];
    @Deprecated
    public static final ISubmap TOP_RIGHT = Submap.X2[0][1];
    @Deprecated
    public static final ISubmap BOTTOM_LEFT = Submap.X2[1][0];
    @Deprecated
    public static final ISubmap BOTTOM_RIGHT = Submap.X2[1][1];

    @Value
    public static class Vertex {
        Vector3f pos;
        Vector2f uvs;
    }

    // TODO: confirm this works
//    private static final Sprite BASE = Atlases.getTerrain().getTexture(MissingSprite.getMissingSpriteId()).getSprite();

    @ToString
    public static class UVs {

        @Getter
        private float minU, minV, maxU, maxV;

        @Getter
        private final Sprite sprite;

        private final Vector2f[] data;

        private UVs(Vector2f... data) {
            this(Atlases.getTerrain().getTexture(0).getSprite(), data);
        }

        private UVs(Sprite sprite, Vector2f... data) {
            this.data = data;
            this.sprite = sprite;

            float minU = Float.MAX_VALUE;
            float minV = Float.MAX_VALUE;
            float maxU = 0, maxV = 0;
            for (Vector2f v : data) {
                minU = Math.min(minU, v.x);
                minV = Math.min(minV, v.y);
                maxU = Math.max(maxU, v.x);
                maxV = Math.max(maxV, v.y);
            }
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
        }

        public UVs(float minU, float minV, float maxU, float maxV, Sprite sprite) {
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
            this.sprite = sprite;
            this.data = vectorize();
        }

        public UVs transform(Sprite other, ISubmap submap) {
            UVs normal = normalize();
            submap = submap.normalize();

            float width = normal.maxU - normal.minU;
            float height = normal.maxV - normal.minV;

            float minU = submap.getXOffset();
            float minV = submap.getYOffset();
            minU += normal.minU * submap.getWidth();
            minV += normal.minV * submap.getHeight();

            float maxU = minU + (width * submap.getWidth());
            float maxV = minV + (height * submap.getHeight());

            // TODO this is horrid
            return new UVs(other,
                    new Vector2f(data[0].x == this.minU ? minU : maxU, data[0].y == this.minV ? minV : maxV),
                    new Vector2f(data[1].x == this.minU ? minU : maxU, data[1].y == this.minV ? minV : maxV),
                    new Vector2f(data[2].x == this.minU ? minU : maxU, data[2].y == this.minV ? minV : maxV),
                    new Vector2f(data[3].x == this.minU ? minU : maxU, data[3].y == this.minV ? minV : maxV))
                           .relativize();
        }

        UVs normalizeQuadrant() {
            UVs normal = normalize();

            int quadrant = normal.getQuadrant();
            float minUInterp = quadrant == 1 || quadrant == 2 ? 0.5f : 0;
            float minVInterp = quadrant < 2 ? 0.5f : 0;
            float maxUInterp = quadrant == 0 || quadrant == 3 ? 0.5f : 1;
            float maxVInterp = quadrant > 1 ? 0.5f : 1;

            normal = new UVs(sprite, normalize(new Vector2f(minUInterp, minVInterp), new Vector2f(maxUInterp, maxVInterp), normal.vectorize()));
            return normal.relativize();
        }

        public UVs normalize() {
            Vector2f min = new Vector2f(sprite.getMinU(), sprite.getMinV());
            Vector2f max = new Vector2f(sprite.getMaxU(), sprite.getMaxV());
            return new UVs(sprite, normalize(min, max, data));
        }

        public UVs relativize() {
            return relativize(sprite);
        }

        public UVs relativize(Sprite sprite) {
            Vector2f min = new Vector2f(sprite.getMinU(), sprite.getMinV());
            Vector2f max = new Vector2f(sprite.getMaxU(), sprite.getMaxV());
            return new UVs(sprite, lerp(min, max, data));
        }

        @SuppressWarnings("null")
        public Vector2f[] vectorize() {
            return data == null ? new Vector2f[]{ new Vector2f(minU, minV), new Vector2f(minU, maxV), new Vector2f(maxU, maxV), new Vector2f(maxU, minV) } : data;
        }

        private Vector2f[] normalize(Vector2f min, Vector2f max, @NonnullType Vector2f... vecs) {
            Vector2f[] ret = new Vector2f[vecs.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = normalize(min, max, vecs[i]);
            }
            return ret;
        }

        private Vector2f normalize(Vector2f min, Vector2f max, Vector2f vec) {
            return new Vector2f(Quad.normalize(min.x, max.x, vec.x), Quad.normalize(min.y, max.y, vec.y));
        }

        private Vector2f[] lerp(Vector2f min, Vector2f max, @NonnullType Vector2f... vecs) {
            Vector2f[] ret = new Vector2f[vecs.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = lerp(min, max, vecs[i]);
            }
            return ret;
        }

        private Vector2f lerp(Vector2f min, Vector2f max, Vector2f vec) {
            return new Vector2f(Quad.lerp(min.x, max.x, vec.x), Quad.lerp(min.y, max.y, vec.y));
        }

        public int getQuadrant() {
            if (maxU <= 0.5f) {
                if (maxV <= 0.5f) {
                    return 3;
                } else {
                    return 0;
                }
            } else {
                if (maxV <= 0.5f) {
                    return 2;
                } else {
                    return 1;
                }
            }
        }
    }

    private final Vector3f[] vertPos;
    private final Vector2f[] vertUv;

    // Technically nonfinal, but treated as such except in constructor
    @Getter
    private UVs uvs;

    private final Builder builder;

    private final int blocklight, skylight;

    private Quad(Vector3f[] verts, Vector2f[] uvs, Builder builder, Sprite sprite) {
        this(verts, uvs, builder, sprite, 0, 0);
    }

    @Deprecated
    private Quad(Vector3f[] verts, Vector2f[] uvs, Builder builder, Sprite sprite, boolean fullbright) {
        this(verts, uvs, builder, sprite, fullbright ? 15 : 0, fullbright ? 15 : 0);
    }

    private Quad(Vector3f[] verts, Vector2f[] uvs, Builder builder, Sprite sprite, int blocklight, int skylight) {
        this.vertPos = verts;
        this.vertUv = uvs;
        this.builder = builder;
        this.uvs = new UVs(sprite, uvs);
        this.blocklight = blocklight;
        this.skylight = skylight;
    }

    private Quad(Vector3f[] verts, UVs uvs, Builder builder) {
        this(verts, uvs.vectorize(), builder, uvs.getSprite());
    }

    @Deprecated
    private Quad(Vector3f[] verts, UVs uvs, Builder builder, boolean fullbright) {
        this(verts, uvs.vectorize(), builder, uvs.getSprite(), fullbright);
    }

    private Quad(Vector3f[] verts, UVs uvs, Builder builder, int blocklight, int skylight) {
        this(verts, uvs.vectorize(), builder, uvs.getSprite(), blocklight, skylight);
    }

    public Vector3f getVert(int index) {
        return new Vector3f(vertPos[index % 4]);
    }

    public Quad withVert(int index, Vector3f vert) {
        Vector3f[] newverts = new Vector3f[4];
        System.arraycopy(vertPos, 0, newverts, 0, newverts.length);
        newverts[index] = vert;
        return new Quad(newverts, getUvs(), builder);
    }

    public Vector2f getUv(int index) {
        return new Vector2f(vertUv[index % 4]);
    }

    public Quad withUv(int index, Vector2f uv) {
        Vector2f[] newuvs = new Vector2f[4];
        System.arraycopy(getUvs().vectorize(), 0, newuvs, 0, newuvs.length);
        newuvs[index] = uv;
        return new Quad(vertPos, new UVs(newuvs), builder);
    }

    public void compute() {

    }

    public Quad[] subdivide(int count) {
        if (count == 1) {
            return new Quad[] { this };
        } else if (count != 4) {
            throw new UnsupportedOperationException();
        }

        List<Quad> rects = Lists.newArrayList();

        Pair<Quad, Quad> firstDivide = divide(false);
        Pair<Quad, Quad> secondDivide = firstDivide.getLeft().divide(true);
        rects.add(secondDivide.getLeft());

        if (firstDivide.getRight() != null) {
            Pair<Quad, Quad> thirdDivide = firstDivide.getRight().divide(true);
            rects.add(thirdDivide.getLeft());
            rects.add(thirdDivide.getRight());
        } else {
            rects.add(null);
            rects.add(null);
        }

        rects.add(secondDivide.getRight());

        return rects.toArray(new Quad[rects.size()]);
    }

    @SuppressWarnings("null")
    private Pair<@NonnullType Quad, Quad> divide(boolean vertical) {
        float min, max;
        UVs uvs = getUvs().normalize();
        if (vertical) {
            min = uvs.minV;
            max = uvs.maxV;
        } else {
            min = uvs.minU;
            max = uvs.maxU;
        }
        if (min < 0.5 && max > 0.5) {
            UVs first = new UVs(vertical ? uvs.minU : 0.5f, vertical ? 0.5f : uvs.minV, uvs.maxU, uvs.maxV, uvs.getSprite());
            UVs second = new UVs(uvs.minU, uvs.minV, vertical ? uvs.maxU : 0.5f, vertical ? 0.5f : uvs.maxV, uvs.getSprite());

            int firstIndex = 0;
            for (int i = 0; i < vertUv.length; i++) {
                if (vertUv[i].y == getUvs().minV && vertUv[i].x == getUvs().minU) {
                    firstIndex = i;
                    break;
                }
            }

            float f = (0.5f - min) / (max - min);

            Vector3f[] firstQuad = new Vector3f[4];
            Vector3f[] secondQuad = new Vector3f[4];
            for (int i = 0; i < 4; i++) {
                int idx = (firstIndex + i) % 4;
                firstQuad[i] = new Vector3f(vertPos[idx]);
                secondQuad[i] = new Vector3f(vertPos[idx]);
            }

            int i1 = 0;
            int i2 = vertical ? 1 : 3;
            int j1 = vertical ? 3 : 1;
            int j2 = 2;

            firstQuad[i1].x = lerp(firstQuad[i1].x, firstQuad[i2].x, f);
            firstQuad[i1].y = lerp(firstQuad[i1].y, firstQuad[i2].y, f);
            firstQuad[i1].z = lerp(firstQuad[i1].z, firstQuad[i2].z, f);
            firstQuad[j1].x = lerp(firstQuad[j1].x, firstQuad[j2].x, f);
            firstQuad[j1].y = lerp(firstQuad[j1].y, firstQuad[j2].y, f);
            firstQuad[j1].z = lerp(firstQuad[j1].z, firstQuad[j2].z, f);

            secondQuad[i2].x = lerp(secondQuad[i1].x, secondQuad[i2].x, f);
            secondQuad[i2].y = lerp(secondQuad[i1].y, secondQuad[i2].y, f);
            secondQuad[i2].z = lerp(secondQuad[i1].z, secondQuad[i2].z, f);
            secondQuad[j2].x = lerp(secondQuad[j1].x, secondQuad[j2].x, f);
            secondQuad[j2].y = lerp(secondQuad[j1].y, secondQuad[j2].y, f);
            secondQuad[j2].z = lerp(secondQuad[j1].z, secondQuad[j2].z, f);

            Quad q1 = new Quad(firstQuad, first.relativize(), builder, blocklight, skylight);
            Quad q2 = new Quad(secondQuad, second.relativize(), builder, blocklight, skylight);
            return Pair.of(q1, q2);
        } else {
            return Pair.of(this, null);
        }
    }

    public static float lerp(float a, float b, float f) {
        float ret = (a * (1 - f)) + (b * f);
        return ret;
    }

    public static float normalize(float min, float max, float x) {
        float ret = (x - min) / (max - min);
        return ret;
    }

    public Quad rotate(int amount) {
        Vector2f[] uvs = new Vector2f[4];

        Sprite s = getUvs().getSprite();

        for (int i = 0; i < 4; i++) {
            Vector2f normalized = new Vector2f(normalize(s.getMinU(), s.getMaxU(), vertUv[i].x), normalize(s.getMinV(), s.getMaxV(), vertUv[i].y));
            Vector2f uv;
            switch (amount) {
                case 1:
                    uv = new Vector2f(normalized.y, 1 - normalized.x);
                    break;
                case 2:
                    uv = new Vector2f(1 - normalized.x, 1 - normalized.y);
                    break;
                case 3:
                    uv = new Vector2f(1 - normalized.y, normalized.x);
                    break;
                default:
                    uv = new Vector2f(normalized.x, normalized.y);
                    break;
            }
            uvs[i] = uv;
        }

        for (int i = 0; i < uvs.length; i++) {
            uvs[i] = new Vector2f(lerp(s.getMinU(), s.getMaxU(), uvs[i].x), lerp(s.getMinV(), s.getMaxV(), uvs[i].y));
        }

        Quad ret = new Quad(vertPos, uvs, builder, getUvs().getSprite(), blocklight, skylight);
        return ret;
    }

    public Quad derotate() {
        int start = 0;
        for (int i = 0; i < 4; i++) {
            if (vertUv[i].x <= getUvs().minU && vertUv[i].y <= getUvs().minV) {
                start = i;
                break;
            }
        }

        Vector2f[] uvs = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            uvs[i] = vertUv[(i + start) % 4];
        }
        return new Quad(vertPos, uvs, builder, getUvs().getSprite(), blocklight, skylight);
    }

    public Quad setLight(int blocklight, int skylight) {
        return new Quad(this.vertPos, uvs, builder, blocklight, skylight);
    }

    // TODO: confirm this works
    @SuppressWarnings("null")
    public BakedQuad rebake() {
        int[] vertexData = new int[32];

        for (int v = 0; v < 4; v++) {
            int offset = v * 8;

            // Position
            Vector3f p = vertPos[v];
            vertexData[offset + 0] = Float.floatToRawIntBits(p.x);
            vertexData[offset + 1] = Float.floatToRawIntBits(p.y);
            vertexData[offset + 2] = Float.floatToRawIntBits(p.z);

            // UV
            Vector2f uv = vertUv[v];
            vertexData[offset + 3] = Float.floatToRawIntBits(uv.x);
            vertexData[offset + 4] = Float.floatToRawIntBits(uv.y);

            // Color
            vertexData[offset + 5] = 0xFFFFFFFF;

            // Normal
            vertexData[offset + 6] = 0;

            vertexData[offset + 7] = 0;
        }

        boolean isFullbright = (this.blocklight == 15 || this.skylight == 15);
        float emissionLevel = isFullbright ? 1.0f : 0.0f;

        boolean applyShading = this.builder.applyDiffuseLighting;

        return new BakedQuad(
                vertexData,
                this.builder.getQuadTint(),
                this.builder.getQuadOrientation(),
                this.uvs.getSprite(),
                applyShading,
                emissionLevel
        );
    }

    public Quad transformUVs(Sprite sprite) {
        return transformUVs(sprite, CTMLogic.FULL_TEXTURE.normalize());
    }

    public Quad transformUVs(Sprite sprite, ISubmap submap) {
        return new Quad(vertPos, getUvs().transform(sprite, submap), builder, blocklight, skylight);
    }

    public Quad grow() {
        return new Quad(vertPos, getUvs().normalizeQuadrant(), builder, blocklight, skylight);
    }

    @Deprecated
    public Quad setFullbright(boolean fullbright){
        if (this.blocklight == 15 != fullbright || this.skylight == 15 != fullbright) {
            return new Quad(vertPos, getUvs(), builder, fullbright);
        } else {
            return this;
        }
    }

    // TODO: confirm this works
    public static Quad from(BakedQuad baked) {
        int[] vertexData = baked.getVertexData();
        Vector3f[] positions = new Vector3f[4];
        Vector2f[] uvs = new Vector2f[4];

        for (int v = 0; v < 4; v++) {
            int offset = v * 8;

            // Position
            float x = Float.intBitsToFloat(vertexData[offset + 0]);
            float y = Float.intBitsToFloat(vertexData[offset + 1]);
            float z = Float.intBitsToFloat(vertexData[offset + 2]);
            positions[v] = new Vector3f(x, y, z);

            // UV
            float u = Float.intBitsToFloat(vertexData[offset + 3]);
            float vCoord = Float.intBitsToFloat(vertexData[offset + 4]);
            uvs[v] = new Vector2f(u, vCoord);
        }

        Builder b = new Builder(((BakedQuadAccessor)baked).getSprite());
        b.setQuadOrientation(baked.getFace());
        b.setQuadTint(baked.getColorIndex());
        b.setApplyDiffuseLighting(baked.hasShade());

        int lightVal = (baked.getEmission() > 0.0f) ? 15 : 0;

        return new Quad(positions, uvs, b, ((BakedQuadAccessor)baked).getSprite(), lightVal, lightVal);
    }

    @RequiredArgsConstructor
    public static class Builder {

        @Getter
        private final Sprite sprite;

        @Setter @Getter
        private int quadTint = -1;

        @Setter @Getter
        private Direction quadOrientation;

        @Setter @Getter
        private boolean applyDiffuseLighting;
    }
}
