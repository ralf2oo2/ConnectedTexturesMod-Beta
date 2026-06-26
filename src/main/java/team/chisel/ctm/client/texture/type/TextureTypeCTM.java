package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextCTM;
import team.chisel.ctm.client.texture.render.TextureCTM;

public class TextureTypeCTM implements ITextureType {
    @Override
    public ICTMTexture<? extends TextureTypeCTM> makeTexture(TextureInfo info) {
        return new TextureCTM<TextureTypeCTM>(this, info);
    }

    @Override
    public TextureContextCTM getBlockRenderContext(BlockState state, BlockStateView world, BlockPos pos, ICTMTexture<?> tex) {
        return new TextureContextCTM(state, world, pos, (TextureCTM<?>) tex);
    }

    @Override
    public int getQuadsPerSide() {
        return 4;
    }

    @Override
    public int requiredTextures() {
        return 2;
    }

    @Override
    public ITextureContext getContextFromData(long data) {
        throw new UnsupportedOperationException();
    }
}
