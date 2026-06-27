package team.chisel.ctm.client.model;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TextureContextMap {
    private final Map<ICTMTexture<?>, ITextureContext> contextMap = new HashMap<>();
    private final LongList dataList = new LongArrayList();

    public void fill(Collection<ICTMTexture<?>> textures, BlockState state, BlockStateView world, BlockPos pos) {
        for (ICTMTexture<?> texture : textures) {
            ITextureContext context = texture.getType().getBlockRenderContext(state, world, pos, texture);
            if (context != null) {
                contextMap.put(texture, context);
                dataList.add(context.getCompressedData());
            }
        }
    }

    public void reset() {
        contextMap.clear();
        dataList.clear();
    }

    @Nullable
    public ITextureContext getContext(ICTMTexture<?> texture) {
        return contextMap.get(texture);
    }

    public boolean containsContext(ICTMTexture<?> texture) {
        return contextMap.containsKey(texture);
    }

    public long[] toDataArray() {
        return dataList.toLongArray();
    }
}
