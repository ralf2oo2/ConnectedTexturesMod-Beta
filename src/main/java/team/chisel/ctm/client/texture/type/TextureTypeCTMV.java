package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.ctx.TextureContextCTMV;
import team.chisel.ctm.client.texture.render.TextureCTMV;

public class TextureTypeCTMV implements ITextureType {

    @Override
    public ICTMTexture<TextureTypeCTMV> makeTexture(TextureInfo info) {
        return new TextureCTMV(this, info);
    }

    @Override
    public TextureContextCTMV getBlockRenderContext(BlockState state, BlockStateView world, BlockPos pos, ICTMTexture<?> tex) {
        return new TextureContextCTMV(world, pos);
    }

    @Override
    public int requiredTextures() {
        return 2;
    }

    @Override
    public ITextureContext getContextFromData(long data){
        return new TextureContextCTMV(data);
    }
}
