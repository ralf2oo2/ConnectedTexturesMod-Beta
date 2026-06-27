package team.chisel.ctm.client.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.client.render.model.BakedModel;
import net.modificationstation.stationapi.api.client.render.model.BakedQuad;
import net.modificationstation.stationapi.api.client.render.model.ModelIdentifier;
import net.modificationstation.stationapi.api.client.render.model.WeightedBakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.ModelTransformation;
import net.modificationstation.stationapi.api.client.texture.Sprite;
import net.modificationstation.stationapi.api.util.collection.WeightedPicker;
import net.modificationstation.stationapi.api.util.math.Direction;
import org.lwjgl.util.vector.Vector3f;
import team.chisel.ctm.api.model.IUnbakedModelCTM;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.util.RenderContextList;
import team.chisel.ctm.client.util.BlockRenderLayer;
import team.chisel.ctm.mixin.WeightedBakedModelAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class AbstractCTMBakedModel implements BakedModel {
    private static Cache<ModelIdentifier, AbstractCTMBakedModel> itemcache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).<ModelIdentifier, AbstractCTMBakedModel>build();
    private static Cache<State, AbstractCTMBakedModel> modelcache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(5000).<State, AbstractCTMBakedModel>build();

    public static void invalidateCaches()
    {
        itemcache.invalidateAll();
        modelcache.invalidateAll();
    }

    @ParametersAreNonnullByDefault
    private class Overrides extends ItemOverrideList {

        public Overrides() {
            super();
        }

        @Override
        @SneakyThrows
        public BakedModel handleItemState(BakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            Block block = null;
            if (stack.getItem() instanceof ItemBlock) {
                block = ((ItemBlock) stack.getItem()).getBlock();
            }
            final BlockState state = block == null ? null : block.getDefaultState();
            ModelResourceLocation mrl = ModelUtil.getMesh(stack);
            if (mrl == null) {
                // this must be a missing/invalid model
                return Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
            }
            return itemcache.get(mrl, () -> createModel(state, model, null, 0));
        }
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    private static class State {
        private final @Nonnull BlockState cleanState;
        private final @Nullable Object2LongMap<ICTMTexture<?>> serializedContext;
        private final @Nonnull BakedModel parent;

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            State other = (State) obj;

            if (cleanState != other.cleanState) {
                return false;
            }
            if (parent != other.parent) {
                return false;
            }

            if (serializedContext == null) {
                if (other.serializedContext != null) {
                    return false;
                }
            } else if (!serializedContext.equals(other.serializedContext)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            // for some reason blockstates hash their properties, we only care about the identity hash
            result = prime * result + System.identityHashCode(cleanState);
            result = prime * result + (parent == null ? 0 : parent.hashCode());
            result = prime * result + (serializedContext == null ? 0 : serializedContext.hashCode());
            return result;
        }
    }

    @Getter
    private final @Nonnull IUnbakedModelCTM model;
    @Getter
    private final @Nonnull BakedModel parent;
    private final @Nonnull Overrides overrides = new Overrides();

    protected final ListMultimap<BlockRenderLayer, BakedQuad> genQuads = MultimapBuilder.enumKeys(BlockRenderLayer.class).arrayListValues().build();
    protected final Table<BlockRenderLayer, Direction, List<BakedQuad>> faceQuads = Tables.newCustomTable(Maps.newEnumMap(BlockRenderLayer.class), () -> Maps.newEnumMap(Direction.class));

    @Override
    @SneakyThrows
    public @Nonnull ImmutableList<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random random) {
        long rand = random.nextLong();

        if (CTMCoreMethods.renderingDamageModel.get()) {
            return parent.getQuads(state, side, random);
        }

        BakedModel parent = getParent(rand);

//        ProfileUtil.start("ctm_models");

        AbstractCTMBakedModel baked = this;
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        if (Minecraft.getInstance().world != null && state instanceof CTMExtendedState) {
//            ProfileUtil.start("state_creation");
            CTMExtendedState ext = (CTMExtendedState) state;
            RenderContextList ctxList = ext.getContextList(ext.getClean(), model);

            Object2LongMap<ICTMTexture<?>> serialized = ctxList.serialized();
//            ProfileUtil.endAndStart("model_creation");
            baked = modelcache.get(new State(ext.getClean(), serialized, parent), () -> createModel(state, model, ctxList, rand));
//            ProfileUtil.end();
        } else if (state != null)  {
//            ProfileUtil.start("model_creation");
            baked = modelcache.get(new State(state, null, getParent(rand)), () -> createModel(state, model, null, rand));
//            ProfileUtil.end();
        }

//        ProfileUtil.start("quad_lookup");
        List<BakedQuad> ret;
        if (side != null && layer != null) {
            ret = baked.faceQuads.get(layer, side);
        } else if (side != null) {
            ret = baked.faceQuads.column(side).values().stream().flatMap(List::stream).collect(Collectors.toList());
        } else if (layer != null) {
            ret = baked.genQuads.get(layer);
        } else {
            ret = Lists.newArrayList(baked.genQuads.values());
        }
//        ProfileUtil.end();
//
//        ProfileUtil.end();
        return ret;
    }

    /**
     * Random sensitive parent, will proxy to {@link WeightedBakedModel} if possible.
     */
    @Nonnull
    public BakedModel getParent(long rand) {
        // TODO: get random model on weightedmodel
//        if (getParent() instanceof WeightedBakedModel weightedBakedModel) {
//            Objects.requireNonNull(WeightedPicker.getAt((List<? extends WeightedPicker.Entry>) ((WeightedBakedModelAccessor)weightedBakedModel).getModels(), Math.abs((int) rand) % ((WeightedBakedModelAccessor)weightedBakedModel).getTotalWeight()));
//            return ((WeightedBakedModel)parent).getRandomModel(rand);
//        }
        return getParent();
    }

    @Override
    public @Nonnull ItemOverrideList getOverrides() {
        return overrides;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return parent.useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return parent.hasDepth();
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public @Nonnull Sprite getSprite() {
        return this.parent.getSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return parent.getTransformation();
    }

    private static @Nonnull TRSRTransformation get(float tx, float ty, float tz, float ax, float ay, float az, float s) {
        return new TRSRTransformation(
                new Vector3f(tx / 16, ty / 16, tz / 16),
                TRSRTransformation.quatFromXYZDegrees(new Vector3f(ax, ay, az)),
                new Vector3f(s, s, s),
                null);
    }

    public static final Map<ModelTransformation.Mode, TRSRTransformation> TRANSFORMS = ImmutableMap.<ModelTransformation.Mode, TRSRTransformation>builder()
                                                                                    .put(ModelTransformation.Mode.GUI,                         get(0, 0, 0, 30, 45, 0, 0.625f))
                                                                                    .put(ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND,     get(0, 2.5f, 0, 75, 45, 0, 0.375f))
                                                                                    .put(ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND,      get(0, 2.5f, 0, 75, 45, 0, 0.375f))
                                                                                    .put(ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND,     get(0, 0, 0, 0, 45, 0, 0.4f))
                                                                                    .put(ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND,      get(0, 0, 0, 0, 225, 0, 0.4f))
                                                                                    .put(ModelTransformation.Mode.GROUND,                      get(0, 2, 0, 0, 0, 0, 0.25f))
                                                                                    .put(ModelTransformation.Mode.FIXED,                       get(0, 0, 0, 0, 0, 0, 0.5f))
                                                                                    .build();

    public static final TRSRTransformation DEFAULT_TRANSFORM = get(0, 0, 0, 0, 0, 0, 1);

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return Pair.of(this, TRANSFORMS.getOrDefault(cameraTransformType, DEFAULT_TRANSFORM).getMatrix());
    }

    protected static final BlockRenderLayer[] LAYERS = BlockRenderLayer.values();

    protected abstract AbstractCTMBakedModel createModel(IBlockState state, @Nonnull IModelCTM model, RenderContextList ctx, long rand);
}
