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
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.texture.CTMTexture;
import team.chisel.ctm.api.texture.TextureContext;
import team.chisel.ctm.client.state.CTMRenderContext;
import team.chisel.ctm.client.util.BakedQuadRetextured;
import team.chisel.ctm.mixin.BakedQuadAccessor;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CTMBakedModel extends ForwardingBakedModel {
    private static final Cache<BlockMeshCacheKey, CTMBakedModel> BLOCK_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).build();

    @NotNull
    protected CTMModelInfo modelInfo;
    protected Sprite sprite;

    private final Map<Direction, ImmutableList<BakedQuad>> cachedQuadsByFace = new EnumMap<>(Direction.class);
    private ImmutableList<BakedQuad> nullFaceQuads = ImmutableList.of();

    public CTMBakedModel(@NotNull final BakedModel parent, @NotNull final CTMModelInfo modelInfo) {
        this.wrapped = Objects.requireNonNull(parent, "parent is marked non-null but is null");
        this.modelInfo = Objects.requireNonNull(modelInfo, "modelInfo is marked non-null but is null");
        sprite = initSprite();
    }

    private CTMBakedModel(BakedModel parent, CTMModelInfo modelInfo, BlockState blockState, @Nullable TextureContextMap contextMap, Random rand) {
        this(parent, modelInfo);

        List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
        directions.add(null);

        for (Direction face : directions) {
            List<BakedQuad> parentQuads = this.wrapped.getQuads(blockState, face, rand);

            if (parentQuads.isEmpty()) {
                if (face != null) {
                    cachedQuadsByFace.put(face, ImmutableList.of());
                } else {
                    nullFaceQuads = ImmutableList.of();
                }
                continue;
            }

            List<BakedQuad> transformedQuads = new ArrayList<>();
            Map<BakedQuad, CTMTexture<?>> textureMap = new LinkedHashMap<>();

            for (BakedQuad q : parentQuads) {
                Identifier spriteId = ((BakedQuadAccessor) q).getSprite().getContents().getId();
                CTMTexture<?> tex = modelInfo.getOverrideTexture(q.getColorIndex(), spriteId);
                if (tex == null) {
                    tex = modelInfo.getTexture(spriteId);
                }

                if (tex != null) {
                    Sprite spriteReplacement = modelInfo.getOverrideSprite(q.getColorIndex());
                    if (spriteReplacement != null) {
                        q = new BakedQuadRetextured(q, spriteReplacement);
                    }
                    textureMap.put(q, tex);
                } else {
                    transformedQuads.add(q);
                }
            }


            for (Map.Entry<BakedQuad, CTMTexture<?>> entry : textureMap.entrySet()) {
                CTMTexture<?> texture = entry.getValue();
                BakedQuad quad = entry.getKey();

                TextureContext textureContext = contextMap == null ? null : contextMap.getContext(texture);

                transformedQuads.addAll(texture.transformQuad(quad, face, textureContext));
            }

            if (face != null) {
                cachedQuadsByFace.put(face, ImmutableList.copyOf(transformedQuads));
            } else {
                nullFaceQuads = ImmutableList.copyOf(transformedQuads);
            }
        }
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
        CTMTexture<?> texture = getModelInfo().getTexture(getParent().getSprite().getContents().getId());
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
        if (blockState == null) {
            return this.wrapped.getQuads(null, face, rand);
        }

        List<BakedQuad> parentQuads = this.wrapped.getQuads(blockState, face, rand);
        if (parentQuads.isEmpty()) {
            return ImmutableList.of();
        }

        if (!this.cachedQuadsByFace.isEmpty() && face != null) {
            return this.cachedQuadsByFace.getOrDefault(face, ImmutableList.of());
        }

        CTMBakedModel bakedModel = CTMRenderContext.getBakedModel();
        if(bakedModel != null) {
            return face == null ? bakedModel.nullFaceQuads : bakedModel.cachedQuadsByFace.getOrDefault(face, ImmutableList.of());
        }

        TextureContextMap contextMap = CTMRenderContext.getTextureContextMap(blockState, getModelInfo());
        if (contextMap == null) {
            return this.wrapped.getQuads(blockState, face, rand);
        }

        try {
            BlockMeshCacheKey key = new BlockMeshCacheKey(this.wrapped, blockState, contextMap);

            CTMBakedModel processedModel = BLOCK_MESH_CACHE.get(key,
                    () -> new CTMBakedModel(this.wrapped, this.modelInfo, blockState, contextMap, rand)
            );

            CTMRenderContext.setBakedModel(processedModel);

            return face == null ? processedModel.nullFaceQuads : processedModel.cachedQuadsByFace.getOrDefault(face, ImmutableList.of());

        } catch (ExecutionException e) {
            return this.wrapped.getQuads(blockState, face, rand);
        }
    }

    private static class BlockMeshCacheKey {
        private final BakedModel parent;
        private final BlockState blockState;
        private final int contextDataHash;
        private final int precomputedHash;

        private BlockMeshCacheKey(BakedModel parent, BlockState blockState, TextureContextMap contextMap) {
            this.parent = parent;
            this.blockState = blockState;
            this.contextDataHash = contextMap.getElementsHashCode();

            int hash = parent.hashCode();
            hash = 31 * hash + blockState.hashCode();
            hash = 31 * hash + this.contextDataHash;
            this.precomputedHash = hash;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof BlockMeshCacheKey other)) return false;

            return this.parent == other.parent
                           && this.blockState == other.blockState
                           && this.contextDataHash == other.contextDataHash;
        }

        @Override
        public int hashCode() {
            return precomputedHash;
        }
    }
}
