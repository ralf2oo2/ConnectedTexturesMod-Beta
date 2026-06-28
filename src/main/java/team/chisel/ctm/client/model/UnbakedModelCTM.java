//package team.chisel.ctm.client.model;
//
//import com.google.common.collect.HashMultimap;
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import com.google.gson.*;
//import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
//import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
//import net.modificationstation.stationapi.api.block.BlockState;
//import net.modificationstation.stationapi.api.client.render.model.BakedModel;
//import net.modificationstation.stationapi.api.client.render.model.Baker;
//import net.modificationstation.stationapi.api.client.render.model.ModelBakeSettings;
//import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
//import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
//import net.modificationstation.stationapi.api.client.texture.Sprite;
//import net.modificationstation.stationapi.api.client.texture.SpriteIdentifier;
//import net.modificationstation.stationapi.api.util.Identifier;
//import org.apache.commons.lang3.tuple.Pair;
//import team.chisel.ctm.api.texture.ICTMTexture;
//import team.chisel.ctm.api.util.ResourceUtil;
//import team.chisel.ctm.api.util.TextureInfo;
//import team.chisel.ctm.client.texture.render.TextureNormal;
//import team.chisel.ctm.client.texture.type.TextureTypeNormal;
//import team.chisel.ctm.client.util.BlockRenderLayer;
//
//import javax.annotation.Nullable;
//import java.io.IOException;
//import java.util.*;
//import java.util.function.Function;
//
//public class UnbakedModelCTM implements IUnbakedModelCTM {
//    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(IMetadataSectionCTM.class, new IMetadataSectionCTM.Serializer()).create();
//
//    private final JsonUnbakedModel modelinfo;
//    private UnbakedModel vanillamodel;
//
//    // Populated from overrides data during construction
//    private final Int2ObjectMap<JsonElement> overrides;
//    protected final Int2ObjectMap<IMetadataSectionCTM> metaOverrides = new Int2ObjectArrayMap<>();
//
//    // Populated during bake with real texture data
//    protected Int2ObjectMap<Sprite> spriteOverrides;
//    protected Map<Pair<Integer, String>, ICTMTexture<?>> textureOverrides;
//
//    private final Collection<Identifier> textureDependencies;
//
//    private transient byte layers;
//
//    private Map<String, ICTMTexture<?>> textures = new HashMap<>();
//
//    public UnbakedModelCTM(JsonUnbakedModel modelinfo, UnbakedModel vanillamodel, Int2ObjectMap<JsonElement> overrides) throws IOException {
//        this.modelinfo = modelinfo;
//        this.vanillamodel = vanillamodel;
//        this.overrides = overrides;
//
//        this.textureDependencies = new HashSet<>();
//        this.textureDependencies.addAll(vanillamodel.getTextures());
//        for (Map.Entry<Integer, JsonElement> e : this.overrides.entrySet()) {
//            IMetadataSectionCTM meta = null;
//            if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
//                Identifier rl = Identifier.of(e.getValue().getAsString());
//                meta = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(rl));
//                textureDependencies.add(rl);
//            } else if (e.getValue().isJsonObject()) {
//                JsonObject obj = e.getValue().getAsJsonObject();
//                if (!obj.has("ctm_version")) {
//                    // This model can only be version 1, TODO improve this
//                    obj.add("ctm_version", new JsonPrimitive(1));
//                }
//                meta = GSON.fromJson(obj, IMetadataSectionCTM.class);
//            }
//            if (meta != null ) {
//                metaOverrides.put(e.getKey(), meta);
//                textureDependencies.addAll(Arrays.asList(meta.getAdditionalTextures()));
//            }
//        }
//
//        this.textureDependencies.removeIf(rl -> rl.getPath().startsWith("#"));
//
//        // Validate all texture metadata
//        for (Identifier res : getTextures()) {
//            IMetadataSectionCTM meta = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(res));
//            if (meta != null) {
//                if (meta.getType().requiredTextures() != meta.getAdditionalTextures().length + 1) {
//                    throw new IOException(String.format("Texture type %s requires exactly %d textures. %d were provided.", meta.getType(), meta.getType().requiredTextures(), meta.getAdditionalTextures().length + 1));
//                }
//            }
//        }
//    }
//
//    @Override
//    public UnbakedModel getVanillaParent() {
//        return vanillamodel;
//    }
//
//    // TODO remove this reflection
//    private static final MethodHandle _asVanillaModel; static {
//        MethodHandle mh;
//        try {
//            mh = MethodHandles.lookup().unreflect(IModel.class.getMethod("asVanillaModel"));
//        } catch (IllegalAccessException | NoSuchMethodException | SecurityException e) {
//            mh = null;
//        }
//        _asVanillaModel = mh;
//    }
//
//    // @Override Soft override
//    @SuppressWarnings("unchecked")
//    public Optional<JsonUnbakedModel> asVanillaModel() {
//        return Optional.ofNullable(_asVanillaModel)
//                       .<Optional<JsonUnbakedModel>>map(mh -> {
//                           try {
//                               return (Optional<JsonUnbakedModel>) mh.invokeExact(getVanillaParent());
//                           } catch (Throwable e1) {
//                               return Optional.empty();
//                           }
//                       })
//                       .filter(Optional::isPresent)
//                       .orElse(Optional.ofNullable(modelinfo));
//    }
//
//    @Override
//    public Collection<Identifier> getModelDependencies() {
//        return Collections.emptySet();
//    }
//
//    @Override
//    public Collection<Identifier> getTextures() {
//        return textureDependencies;
//    }
//
//    @Override
//    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
//        BakedModel parent = vanillamodel.bake(baker, format, rl -> {
//            Sprite sprite = bakedTextureGetter.apply(rl);
//            IMetadataSectionCTM chiselmeta = null;
//            try {
//                chiselmeta = ResourceUtil.getMetadata(sprite);
//            } catch (IOException e) {}
//            final IMetadataSectionCTM meta = chiselmeta;
//            textures.computeIfAbsent(sprite.getName(), s -> {
//                ICTMTexture<?> tex;
//                if (meta == null) {
//                    tex = new TextureNormal(TextureTypeNormal.INSTANCE, new TextureInfo(new Sprite[][] { sprite }, Optional.empty(), null));
//                } else {
//                    tex = meta.makeTexture(sprite, bakedTextureGetter);
//                }
//                layers |= 1 << (tex.getLayer() == null ? 7 : tex.getLayer().ordinal());
//                return tex;
//            });
//            return sprite;
//        });
//        if (spriteOverrides == null) {
//            spriteOverrides = new Int2ObjectArrayMap<>();
//            // Convert all primitive values into sprites
//            for (Map.Entry<Integer, JsonElement> e : overrides.entrySet()) {
//                if (e.getValue().isJsonPrimitive() && e.getValue().getAsJsonPrimitive().isString()) {
//                    Sprite sprite = bakedTextureGetter.apply(Identifier.of(e.getValue().getAsString()));
//                    spriteOverrides.put(e.getKey(), sprite);
//                }
//            }
//        }
//        if (textureOverrides == null) {
//            textureOverrides = new HashMap<>();
//            for (Map.Entry<Integer, IMetadataSectionCTM> e : metaOverrides.entrySet()) {
//                List<BlockPartFace> matches = modelinfo.getElements().stream().flatMap(b -> b.mapFaces.values().stream()).filter(b -> b.tintIndex == e.getKey()).collect(Collectors.toList());
//                Multimap<String, BlockPartFace> bySprite = HashMultimap.create();
//                matches.forEach(part -> bySprite.put(modelinfo.textures.getOrDefault(part.texture.substring(1), part.texture), part));
//                for (val e2 : bySprite.asMap().entrySet()) {
//                    ResourceLocation texLoc = new ResourceLocation(e2.getKey());
//                    TextureAtlasSprite sprite = getOverrideSprite(e.getKey());
//                    if (sprite == null) {
//                        sprite = bakedTextureGetter.apply(texLoc);
//                    }
//                    ICTMTexture<?> tex = e.getValue().makeTexture(sprite, bakedTextureGetter);
//                    layers |= 1 << (tex.getLayer() == null ? 7 : tex.getLayer().ordinal());
//                    textureOverrides.put(Pair.of(e.getKey(), texLoc.toString()), tex);
//                }
//            }
//        }
//        return new ModelBakedCTM(this, parent);
//    }
//
//    @Override
//    public IModelState getDefaultState() {
//        return TRSRTransformation.identity();
//    }
//
//    @Override
//    public void load() {}
//
//    @Override
//    public Collection<ICTMTexture<?>> getCTMTextures() {
//        return ImmutableList.<ICTMTexture<?>>builder().addAll(textures.values()).addAll(textureOverrides.values()).build();
//    }
//
//    @Override
//    public ICTMTexture<?> getTexture(String iconName) {
//        return textures.get(iconName);
//    }
//
//    @Override
//    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
//        // sign bit is used to signify that a layer-less (vanilla) texture is present
//        return (layers < 0 && state.getBlock().getBlockLayer() == layer) || ((layers >> layer.ordinal()) & 1) == 1;
//    }
//
//    @Override
//    @Nullable
//    public Sprite getOverrideSprite(int tintIndex) {
//        return spriteOverrides.get(tintIndex);
//    }
//
//    @Override
//    @Nullable
//    public ICTMTexture<?> getOverrideTexture(int tintIndex, String sprite) {
//        return textureOverrides.get(Pair.of(tintIndex, sprite));
//    }
//
//    @Override
//    public UnbakedModel retexture(ImmutableMap<String, String> textures) {
//        try {
//            return retexture(this, textures);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ModelLoaderRegistry.getMissingModel();
//        }
//    }
//
//    private static ModelCTM retexture(ModelCTM current, ImmutableMap<String, String> textures) throws IOException {
//        IModel vanillamodel = current.getVanillaParent().retexture(textures);
//
//        // Deep copy logic taken from ModelLoader$VanillaModelWrapper
//        List<BlockPart> parts = new ArrayList<>();
//        for (BlockPart part : current.modelinfo.getElements()) {
//            parts.add(new BlockPart(part.positionFrom, part.positionTo, Maps.newHashMap(part.mapFaces), part.partRotation, part.shade));
//        }
//
//        ModelBlock newModel = new ModelBlock(current.modelinfo.getParentLocation(), parts,
//                Maps.newHashMap(current.modelinfo.textures), current.modelinfo.isAmbientOcclusion(), current.modelinfo.isGui3d(),
//                current.modelinfo.getAllTransforms(), Lists.newArrayList(current.modelinfo.getOverrides()));
//
//        newModel.name = current.modelinfo.name;
//        newModel.parent = current.modelinfo.parent;
//        ModelCTM ret = new ModelCTM(newModel, vanillamodel, new Int2ObjectArrayMap<>(current.overrides));
//
//        ret.modelinfo.textures.putAll(textures);
//        for (Entry<Integer, IMetadataSectionCTM> e : ret.metaOverrides.entrySet()) {
//            ResourceLocation[] additionals = e.getValue().getAdditionalTextures();
//            for (int i = 0; i < additionals.length; i++) {
//                ResourceLocation res = additionals[i];
//                if (res.getPath().startsWith("#")) {
//                    additionals[i] = new ResourceLocation(textures.get(res.getPath().substring(1)));
//                    ret.textureDependencies.add(additionals[i]);
//                }
//            }
//        }
//        for (int i : ret.overrides.keySet()) {
//            ret.overrides.compute(i, (idx, ele) -> {
//                if (ele.isJsonPrimitive() && ele.getAsJsonPrimitive().isString()) {
//                    ele = new JsonPrimitive(textures.get(ele.getAsString().substring(1)));
//                    ret.textureDependencies.add(new ResourceLocation(ele.getAsString()));
//                }
//                return ele;
//            });
//        }
//        return ret;
//    }
//}
