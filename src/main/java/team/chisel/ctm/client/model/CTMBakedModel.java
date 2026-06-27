package team.chisel.ctm.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.client.render.model.BakedModel;
import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.client.render.model.ForwardingBakedModel;
import net.modificationstation.stationapi.api.util.math.Direction;
import team.chisel.ctm.api.util.RenderContextList;
import team.chisel.ctm.client.state.CTMRenderContext;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CTMBakedModel extends ForwardingBakedModel {
    private static final Cache<BlockMeshCacheKey, List<BakedQuad>> BLOCK_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).build();
//    private static final Cache<BakedModel, Mesh> ITEM_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();


    @Override
    public ImmutableList<BakedQuad> getQuads(BlockState blockState, Direction face, Random rand) {
        if(blockState == null || face == null) {
            return this.wrapped.getQuads(blockState, face, rand);
        }

        RenderContextList contextList = CTMRenderContext.getContextList(blockState, this);

        if(contextList == null) {
            return this.wrapped.getQuads(blockState, face, rand);
        }
    }

    private static class BlockMeshCacheKey {
        private final BakedModel parent;
        private final BlockState blockState;
        private final long[] data;

        private BlockMeshCacheKey(BakedModel parent, BlockState blockState, long[] data) {
            this.parent = parent;
            this.blockState = blockState;
            this.data = data;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            BlockMeshCacheKey other = (BlockMeshCacheKey) obj;
            if (parent != other.parent) return false;
            if (blockState != other.blockState) return false;
            if (!Arrays.equals(data, other.data)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, blockState, Arrays.hashCode(data));
        }
    }
}
