package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.AbstractConnectingTexture;
import team.chisel.ctm.client.texture.TextureEdgesFull;
import team.chisel.ctm.client.texture.context.TextureContextEdges;

public class TextureTypeEdgesFull implements TextureType {
    @Override
    public CTMTexture<TextureTypeEdgesFull> makeTexture(TextureInfo info) {
        return new TextureEdgesFull(this, info);
    }

    @Override
    public TextureContext getTextureContext(BlockState state, BlockStateView world, BlockPos pos, CTMTexture<?> texture) {
        return new TextureContextEdges(state, world, pos, (AbstractConnectingTexture<?>) texture);
    }

    @Override
    public int requiredTextures() {
        return 2;
    }
}
