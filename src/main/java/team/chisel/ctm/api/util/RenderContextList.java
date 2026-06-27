package team.chisel.ctm.api.util;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.world.BlockStateView;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.client.util.IdentityStrategy;
import team.chisel.ctm.client.util.RegionCache;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Map;

/**
 * List of IBlockRenderContext's
 */
@ParametersAreNonnullByDefault
public class RenderContextList {

    private static final ThreadLocal<RegionCache> regionMetaCache = ThreadLocal.withInitial(
            () -> new RegionCache(BlockPos.ORIGIN, 0, null));

    private final Map<ICTMTexture<?>, ITextureContext> contextMap = Maps.newIdentityHashMap();
    private final Object2LongMap<ICTMTexture<?>> serialized = new Object2LongOpenCustomHashMap<>(new IdentityStrategy<>());

    public RenderContextList(BlockState state, Collection<ICTMTexture<?>> textures, final BlockView world, BlockPos pos) {
//        ProfileUtil.start("ctm_region_cache_update");
        BlockStateView cachedWorld = regionMetaCache.get().updateWorld(world);

//        ProfileUtil.endAndStart("ctm_context_gather");
        for (ICTMTexture<?> tex : textures) {
            ITextureType type = tex.getType();
            ITextureContext ctx = type.getBlockRenderContext(state, cachedWorld, pos, tex);
            if (ctx != null) {
                contextMap.put(tex, ctx);
            }
        }

//        ProfileUtil.endAndStart("ctm_context_serialize");
        for (Map.Entry<ICTMTexture<?>, ITextureContext> e : contextMap.entrySet()) {
            serialized.put(e.getKey(), e.getValue().getCompressedData());
        }
//        ProfileUtil.end();
    }

    public @Nullable ITextureContext getRenderContext(ICTMTexture<?> tex) {
        return this.contextMap.get(tex);
    }

    public boolean contains(ICTMTexture<?> tex) {
        return getRenderContext(tex) != null;
    }

    public Object2LongMap<ICTMTexture<?>> serialized() {
        return serialized;
    }
}
