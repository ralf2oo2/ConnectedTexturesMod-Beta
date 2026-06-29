package team.chisel.ctm.client.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class MathUtil {
    public static long getCoordinateRandom(int x, int y, int z) {
        long random = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
        random = random * random * 42317861L + random * 11L;
        return random;
    }

    public static long getPositionRandom(Vec3i pos) {
        return getCoordinateRandom(pos.x, pos.y, pos.z);
    }

    public static long getPositionRandom(BlockPos pos) {
        return getCoordinateRandom(pos.x, pos.y, pos.z);
    }

    public static int lerp(float delta, int start, int end) {
        return (int) (start + delta * (end - start));
    }

    public static float getLerpProgress(float value, float start, float end) {
        return (value - start) / (end - start);
    }
}


