package team.chisel.ctm.client.texture.ctx;

import com.google.common.base.Preconditions;
import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.util.math.Vec3i;
import org.lwjgl.util.Point;
import team.chisel.ctm.client.texture.render.TextureMap;
import team.chisel.ctm.client.util.FaceOffset;
import team.chisel.ctm.client.util.MathUtil;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;

@ParametersAreNonnullByDefault
public abstract class TextureContextGrid extends TextureContextPosition {

    public static class Patterned extends TextureContextGrid {

        public Patterned(BlockPos pos, TextureMap tex, boolean applyOffset) {
            super(pos, tex, applyOffset);
        }

        @Override
        protected Point calculateTextureCoord(BlockPos pos, int w, int h, Direction side) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            int tx, ty;

            // Calculate submap x/y from x/y/z by ignoring the direction which the side is offset on
            // Negate the y coordinate when calculating non-vertical directions, otherwise it is reverse order
            if (side.getAxis().isVertical()) {
                // DOWN || UP
                tx = x % w;
                ty = (side.getOffsetY() * z + 1) % h;
            } else if (side.getAxis() == Direction.Axis.Z) {
                // NORTH || SOUTH
                tx = x % w;
                ty = -y % h;
            } else {
                // WEST || EAST
                tx = (z + 1) % w;
                ty = -y % h;
            }

            // Reverse x order for north and east
            if (side == Direction.NORTH || side == Direction.EAST) {
                tx = (w - tx - 1) % w;
            }

            // Remainder can produce negative values, so wrap around
            if (tx < 0) {
                tx += w;
            }
            if (ty < 0) {
                ty += h;
            }

            return new Point(tx, ty);
        }
    }

    public static class Random extends TextureContextGrid {

        private static final java.util.Random rand = new java.util.Random();

        public Random(BlockPos pos, TextureMap tex, boolean applyOffset) {
            super(pos, tex, applyOffset);
        }

        @Override
        protected Point calculateTextureCoord(BlockPos pos, int w, int h, Direction side) {

            rand.setSeed(MathUtil.getPositionRandom(pos) + side.ordinal());
            rand.nextBoolean();

            int tx = rand.nextInt(w) + 1;
            int ty = rand.nextInt(h) + 1;

            return new Point(tx, ty);
        }
    }

    private final EnumMap<Direction, Point> textureCoords = new EnumMap<>(Direction.class);
    private final long serialized;

    @SuppressWarnings("null")
    public TextureContextGrid(BlockPos pos, TextureMap tex, boolean applyOffset) {
        super(pos);

        // Since we can only return a long, we must limit to 10 bits of data per face = 60 bits
        Preconditions.checkArgument(tex.getXSize() * tex.getYSize() < 1024, "V* Texture size too large for texture %s", tex.getParticle());

        if (applyOffset) {
            applyOffset();
        }

        long serialized = 0;
        for (@Nonnull Direction side : Direction.values()) {
            BlockPos bp = FaceOffset.getBlockPosOffsetFromFaceOffset(side, tex.getXOffset(), tex.getYOffset());
            BlockPos modifiedPosition = position.add(new Vec3i(bp.x, bp.y, bp.z));

            Point coords = calculateTextureCoord(modifiedPosition, tex.getXSize(), tex.getYSize(), side);
            textureCoords.put(side, coords);

            // Calculate a unique index for a submap (x + (y * x-size)), then shift it left by the max bit storage (10 bits = 1024 unique indices)
            serialized |= (coords.getX() + (coords.getY() * tex.getXSize())) << (10 * side.ordinal());
        }

        this.serialized = serialized;
    }

    protected abstract Point calculateTextureCoord(BlockPos pos, int w, int h, Direction side);

    @SuppressWarnings("null")
    public Point getTextureCoords(Direction side) {
        return textureCoords.get(side);
    }

    @Override
    public long getCompressedData() {
        return serialized;
    }
}
