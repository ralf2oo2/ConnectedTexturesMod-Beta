package team.chisel.ctm.mixin;

import net.modificationstation.stationapi.api.StationAPI;
import net.modificationstation.stationapi.api.client.render.model.BakedModelManager;
import net.modificationstation.stationapi.api.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.chisel.ctm.event.ModelsLoadedEvent;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin{
    @Inject(
            method = "upload(Lnet/modificationstation/stationapi/api/client/render/model/BakedModelManager$BakingResult;Lnet/modificationstation/stationapi/api/util/profiler/Profiler;)V",
            at = @At("TAIL")
    )
    private void onUpload(@Coerce Object bakingResult, Profiler profiler, CallbackInfo ci) {
        StationAPI.EVENT_BUS.post(new ModelsLoadedEvent());
    }
}
