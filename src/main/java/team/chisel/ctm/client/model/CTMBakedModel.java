package team.chisel.ctm.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.client.render.model.BakedModel;
import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.client.render.model.ForwardingBakedModel;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.util.RenderContextList;
import team.chisel.ctm.client.state.CTMRenderContext;
import team.chisel.ctm.client.util.BakedQuadRetextured;
import team.chisel.ctm.mixin.BakedQuadAccessor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CTMBakedModel extends ForwardingBakedModel {
    private static final Cache<BlockMeshCacheKey, List<BakedQuad>> BLOCK_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).build();

    @NotNull
    protected CTMModelInfo modelInfo;
    protected Sprite sprite;

    public CTMBakedModel(@NotNull final BakedModel parent, @NotNull final CTMModelInfo modelInfo) {
        this.wrapped = Objects.requireNonNull(parent, "parent is marked non-null but is null");
        this.modelInfo = Objects.requireNonNull(modelInfo, "modelInfo is marked non-null but is null");
    }

    public static void invalidateCaches() {
        BLOCK_MESH_CACHE.invalidateAll();
    }

    @NotNull
    public BakedModel getParent() {
        return wrapped;
    }

    @NotNull
    public CTMModelInfo getModelInfo() {
        return modelInfo;
    }

    protected Sprite initSprite() {
        ICTMTexture<?> texture = getModelInfo().getTexture(getParent().getSprite().getContents().getId());
        if (texture != null) {
            return texture.getParticle();
        }
        return null;
    }

    @Override
    public Sprite getSprite() {
        if (sprite != null) {
            return sprite;
        }
        return super.getSprite();
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public ImmutableList<BakedQuad> getQuads(BlockState blockState, Direction face, Random rand) {
        if(blockState == null || face == null) {
            return this.wrapped.getQuads(blockState, face, rand);
        }

        RenderContextList contextList = CTMRenderContext.getContextList(blockState, getModelInfo());

        if(contextList == null) {
            return this.wrapped.getQuads(blockState, face, rand);
        }

        try {
            BlockMeshCacheKey key = new BlockMeshCacheKey(this.wrapped, blockState, contextList.toDataArray());

            return BLOCK_MESH_CACHE.get(key, () -> {
                List<BakedQuad> parentQuads = this.wrapped.getQuads(blockState, face, rand);
                List<BakedQuad> transformedQuads = new ArrayList<>();

                Map<BakedQuad, ICTMTexture<?>> textureMap = new LinkedHashMap<>();

                for (BakedQuad q : parentQuads) {
                    Identifier spriteId = ((BakedQuadAccessor)q).getSprite().getContents().getId();

                    ICTMTexture<?> tex = getModelInfo().getOverrideTexture(q.getColorIndex(), spriteId);
                    if (tex == null) {
                        tex = getModelInfo().getTexture(spriteId);
                    }

                    if (tex != null) {
                        Sprite spriteReplacement = getModelInfo().getOverrideSprite(q.getColorIndex());
                        if (spriteReplacement != null) {
                            q = new BakedQuadRetextured(q, spriteReplacement);
                        }
                        textureMap.put(q, tex);
                    } else {
                        transformedQuads.add(q);
                    }
                }

                int quadGoal = textureMap.values().stream()
                                       .mapToInt(tex -> tex.getType().getQuadsPerSide())
                                       .max()
                                       .orElse(1);

                for (Map.Entry<BakedQuad, ICTMTexture<?>> entry : textureMap.entrySet()) {
                    ICTMTexture<?> texture = entry.getValue();
                    BakedQuad quad = entry.getKey();

                    ITextureContext textureContext = contextList.getRenderContext(texture);


                    transformedQuads.addAll(texture.transformQuad(quad, textureContext, quadGoal));
                }

                return ImmutableList.copyOf(transformedQuads);
            });

        } catch (ExecutionException e) {
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
