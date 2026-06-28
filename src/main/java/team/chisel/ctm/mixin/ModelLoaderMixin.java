package team.chisel.ctm.mixin;

import net.modificationstation.stationapi.api.StationAPI;
import net.modificationstation.stationapi.api.client.color.block.BlockColors;
import net.modificationstation.stationapi.api.client.render.model.ModelLoader;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.chisel.ctm.event.ModelsAddedEvent;

import java.util.List;
import java.util.Map;

@Mixin(value = ModelLoader.class, remap = false)
public class ModelLoaderMixin {
    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> unbakedModels;

    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> modelsToBake;

    @Inject(
            method = "<init>(Lnet/modificationstation/stationapi/api/client/color/block/BlockColors;Lnet/modificationstation/stationapi/api/util/profiler/Profiler;Ljava/util/Map;Ljava/util/Map;)V",
            at = @At("TAIL")
    )
    private void onFinishAddingModels(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map<Identifier, List<ModelLoader.SourceTrackedData>> blockStates, CallbackInfo ci) {
        StationAPI.EVENT_BUS.post(ModelsAddedEvent.builder().modelLoader((ModelLoader) (Object) this).unbakedModels(unbakedModels).modelsToBake(modelsToBake).build());
    }
}
