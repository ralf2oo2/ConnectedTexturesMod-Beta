package team.chisel.ctm.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
import team.chisel.ctm.client.render.UnbakedQuad;
import team.chisel.ctm.client.state.CTMRenderContext;
import team.chisel.ctm.client.util.BakedQuadRetextured;
import team.chisel.ctm.mixin.BakedQuadAccessor;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CTMBakedModel extends ForwardingBakedModel {
    private static final Cache<BlockMeshCacheKey, CTMBakedModel> BLOCK_MESH_CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).build();

    protected ImmutableMap<Integer, ImmutableList<BakedQuad>> quads;
    protected ImmutableMap<Integer, ImmutableMap<Direction, ImmutableList<BakedQuad>>> faceQuads;

    protected ImmutableList<BakedQuad> cachedItemQuads;
    protected ImmutableMap<Direction, ImmutableList<BakedQuad>> cachedItemFaceQuads;

    private CTMBakedModel itemModel;

    @NotNull
    protected CTMModelInfo modelInfo;
    protected Sprite sprite;

    public CTMBakedModel(@NotNull final BakedModel parent, @NotNull final CTMModelInfo modelInfo) {
        this.wrapped = Objects.requireNonNull(parent, "parent is marked non-null but is null");
        this.modelInfo = Objects.requireNonNull(modelInfo, "modelInfo is marked non-null but is null");
        this.sprite = initSprite();
    }

    private CTMBakedModel(BakedModel parent, @NotNull CTMModelInfo modelInfo, BlockState blockState, @Nullable TextureContextMap contextMap, Random rand) {
        this(parent, modelInfo);

        int defaultLayer = blockState != null ? blockState.getBlock().getRenderLayer() : 0;

        List<BakedQuad> rawNullQuads = parent.getQuads(blockState, null, rand);
        List<BakedQuad> transformedNullQuads = transformQuads(rawNullQuads, null, contextMap);

        Map<Integer, ImmutableList.Builder<BakedQuad>> quadsBuilder = new HashMap<>();
        for (BakedQuad quad : transformedNullQuads) {
            int layer = getLayerForQuad(quad, defaultLayer);
            quadsBuilder.computeIfAbsent(layer, k -> ImmutableList.builder()).add(quad);
        }

        ImmutableMap.Builder<Integer, ImmutableList<BakedQuad>> finalQuads = ImmutableMap.builder();
        quadsBuilder.forEach((layer, builder) -> finalQuads.put(layer, builder.build()));
        this.quads = finalQuads.build();

        Map<Integer, Map<Direction, ImmutableList.Builder<BakedQuad>>> faceQuadsBuilder = new HashMap<>();

        for (Direction direction : Direction.values()) {
            List<BakedQuad> rawFaceQuads = parent.getQuads(blockState, direction, rand);
            if (rawFaceQuads.isEmpty()) continue;

            List<BakedQuad> transformedFaceQuads = transformQuads(rawFaceQuads, direction, contextMap);

            for (BakedQuad quad : transformedFaceQuads) {
                int layer = getLayerForQuad(quad, defaultLayer);
                faceQuadsBuilder
                        .computeIfAbsent(layer, k -> new EnumMap<>(Direction.class))
                        .computeIfAbsent(direction, k -> ImmutableList.builder())
                        .add(quad);
            }
        }

        ImmutableMap.Builder<Integer, ImmutableMap<Direction, ImmutableList<BakedQuad>>> finalFaceQuads = ImmutableMap.builder();
        faceQuadsBuilder.forEach((layer, dirMap) -> {
            Map<Direction, ImmutableList<BakedQuad>> immutableDirMap = new EnumMap<>(Direction.class);
            dirMap.forEach((dir, builder) -> immutableDirMap.put(dir, builder.build()));
            finalFaceQuads.put(layer, Maps.immutableEnumMap(immutableDirMap));
        });
        this.faceQuads = finalFaceQuads.build();

        if (blockState == null) {
            ImmutableList.Builder<BakedQuad> flatQuads = ImmutableList.builder();
            this.quads.values().forEach(flatQuads::addAll);
            this.cachedItemQuads = flatQuads.build();

            Map<Direction, ImmutableList.Builder<BakedQuad>> flatFaceBuilders = new EnumMap<>(Direction.class);
            this.faceQuads.values().forEach(dirMap -> {
                dirMap.forEach((dir, layerQuads) -> {
                    flatFaceBuilders.computeIfAbsent(dir, k -> ImmutableList.builder()).addAll(layerQuads);
                });
            });
            Map<Direction, ImmutableList<BakedQuad>> flatFaceMap = new EnumMap<>(Direction.class);
            flatFaceBuilders.forEach((dir, builder) -> flatFaceMap.put(dir, builder.build()));
            this.cachedItemFaceQuads = Maps.immutableEnumMap(flatFaceMap);
        }
    }

    private ImmutableList<BakedQuad> transformQuads(List<BakedQuad> parentQuads, Direction direction, TextureContextMap contextMap) {
        List<List<BakedQuad>> transformed = new ArrayList<>(parentQuads.size());
        boolean split = false;

        for (BakedQuad quad : parentQuads) {
            List<BakedQuad> result = transformQuad(quad, direction, contextMap);
            split |= result.size() > 1;
            transformed.add(result);
        }

        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        for (List<BakedQuad> result : transformed) {
            if (!split || result.size() != 1) {
                builder.addAll(result);
                continue;
            }

            BakedQuad quad = result.get(0);
            Sprite sprite = ((BakedQuadAccessor) quad).getSprite();
            for (UnbakedQuad quadrant : new UnbakedQuad(quad).toQuadrants()) {
                builder.add(new BakedQuad(quadrant.bake().getVertexData(), quad.getColorIndex(), quad.getFace(), sprite, quad.hasShade(), quad.getEmission()));
            }
        }
        return builder.build();
    }

    private List<BakedQuad> transformQuad(BakedQuad quad, Direction direction, TextureContextMap contextMap) {
        CTMTexture<?> tex = findTexture(quad);

        if(tex != null) {
            Sprite spriteReplacement = modelInfo.getOverrideSprite(quad.getColorIndex());
            if(spriteReplacement != null) {
                quad = new BakedQuadRetextured(quad, spriteReplacement);
            }
            TextureContext textureContext = contextMap == null ? null : contextMap.getContext(tex);
            return tex.transformQuad(quad, direction, textureContext);
        }
        return List.of(quad);
    }

    private CTMTexture<?> findTexture(BakedQuad quad) {
        Identifier spriteId = ((BakedQuadAccessor) quad).getSprite().getContents().getId();
        CTMTexture<?> tex = modelInfo.getOverrideTexture(quad.getColorIndex(), spriteId);

        if(tex == null) {
            tex = modelInfo.getTexture(spriteId);
        }
        return tex;
    }

    private int getLayerForQuad(BakedQuad quad, int defaultLayer) {
        CTMTexture<?> tex = findTexture(quad);
        if (tex != null && tex.getLayer() != null) {
            return tex.getLayer().layerIndex;
        }
        return defaultLayer;
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

    private ImmutableList<BakedQuad> getQuadsForLayer(CTMBakedModel model, Direction face, int layer) {
        if (face == null) {
            return model.quads.getOrDefault(layer, ImmutableList.of());
        }
        ImmutableMap<Direction, ImmutableList<BakedQuad>> faceMap = model.faceQuads.get(layer);
        return faceMap != null ? faceMap.getOrDefault(face, ImmutableList.of()) : ImmutableList.of();
    }

    @Override
    public ImmutableList<BakedQuad> getQuads(BlockState blockState, Direction face, Random rand) {
        int currentLayer = CTMRenderContext.getRenderLayer();

        if (blockState == null) {
            if (this.itemModel == null) {
                this.itemModel = new CTMBakedModel(this.wrapped, this.modelInfo, null, null, rand);
            }

            if (face == null) {
                return this.itemModel.cachedItemQuads;
            }
            return this.itemModel.cachedItemFaceQuads.getOrDefault(face, ImmutableList.of());
        }

        List<BakedQuad> parentQuads = this.wrapped.getQuads(blockState, face, rand);
        if (parentQuads.isEmpty()) {
            return ImmutableList.of();
        }

        CTMBakedModel bakedModel = CTMRenderContext.getBakedModel();
        if (bakedModel != null) {
            return getQuadsForLayer(bakedModel, face, currentLayer);
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

            return getQuadsForLayer(processedModel, face, currentLayer);

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
