package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.texture.TexturePlane;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;

public class TextureTypePlane implements TextureType {
    public static final TextureTypePlane HORIZONRAL = new TextureTypePlane(Direction.Type.HORIZONTAL);
    public static final TextureTypePlane VERTICAL = new TextureTypePlane(Direction.Type.VERTICAL);

    private final Direction.Type plane;

    public TextureTypePlane(Direction.Type plane) {
        this.plane = plane;
    }

    @Override
    public CTMTexture<TextureTypePlane> makeTexture(TextureInfo info) {
        return new TexturePlane(this, info);
    }

    @Override
    public TextureContext getTextureContext(BlockState state, BlockStateView world, BlockPos pos, CTMTexture<?> texture) {
        return new TextureContextConnecting(state, world, pos, (AbstractConnectingTexture<?>) texture);
    }

    @Override
    public int requiredTextures() {
        return 1;
    }

    public Direction.Type getPlane() {
        return plane;
    }
}
