package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.texture.TextureCTM;
import team.chisel.ctm.client.texture.context.TextureContextConnecting;

public class TextureTypeCTM implements TextureType {
    @Override
    public CTMTexture<? extends TextureTypeCTM> makeTexture(TextureInfo info) {
        return new TextureCTM(this, info);
    }

    @Override
    public TextureContextConnecting getTextureContext(BlockState state, BlockStateView world, BlockPos pos, CTMTexture<?> texture) {
        return new TextureContextConnecting(state, world, pos, (AbstractConnectingTexture<?>) texture);
    }

    @Override
    public int requiredTextures() {
        return 2;
    }
}
