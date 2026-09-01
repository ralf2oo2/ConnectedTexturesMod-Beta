package team.chisel.ctm.client.texture.context;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.client.texture.TextureAlterR;
import team.chisel.ctm.client.util.MathUtil;

import java.util.Random;

public class TextureContextAlterR extends TextureContextPosition{
    private static final Random rand = new Random();

    private final int texture;

    public int getTexture() {
        return texture;
    }

    public TextureContextAlterR(@NotNull BlockPos pos) {
        super(pos);

        int num = 0;

        rand.setSeed(MathUtil.getPositionRandom(pos));
        rand.nextBoolean();

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        num += rand.nextInt(2)*2;
        boolean type = true;

        // If even, switch boolean
        // If we have a set of multiple coords that are [x, y, z]
        // [5, 8, 9], [0, 0, 1], [9, 8, 8]...

        if(x % 2 == 0)
        {
            type = !type;
        }
        // Odd Even Odd
        // True False True

        if(y % 2 == 0)
        {
            type = !type;
        }
        // Even Even Even
        // False True False

        if(z % 2 == 0)
        {
            type = !type;
        }
        // Odd Odd Even
        // False True True

        num += type ? 0 : 1;

        this.texture = num;
    }

    @Override
    public long serialize() {
        return texture;
    }
}
