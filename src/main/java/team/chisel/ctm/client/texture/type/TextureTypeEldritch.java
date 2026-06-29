package team.chisel.ctm.client.texture.type;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.StationBlockPos;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.api.util.TextureInfo;
import team.chisel.ctm.client.texture.context.TextureContextEldritch;
import team.chisel.ctm.client.texture.TextureEldritch;

import javax.annotation.ParametersAreNonnullByDefault;

public class TextureTypeEldritch implements TextureType {
    @Override
    public CTMTexture<TextureTypeEldritch> makeTexture(TextureInfo info) {
        return new TextureEldritch(this, info);
    }

    @Override
    public TextureContext getTextureContext(BlockState state, BlockStateView world, BlockPos pos, CTMTexture<?> texture) {
        return new TextureContextEldritch(pos);
    }

    @Override
    public TextureContext deserializeContext(long data) {
        return new TextureContextEldritch(StationBlockPos.fromLong(data));
    }
}
