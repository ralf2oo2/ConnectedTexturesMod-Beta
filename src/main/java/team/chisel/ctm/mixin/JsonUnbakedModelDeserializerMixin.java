package team.chisel.ctm.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import net.modificationstation.stationapi.api.StationAPI;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.chisel.ctm.event.DeserializeModelJsonEvent;

import java.lang.reflect.Type;

@Mixin(JsonUnbakedModel.Deserializer.class)
public class JsonUnbakedModelDeserializerMixin {
    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/modificationstation/stationapi/api/client/render/model/json/JsonUnbakedModel;", at = @At("RETURN"))
    public void onDeserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<Object> cir) {
        JsonUnbakedModel jsonModel = (JsonUnbakedModel) cir.getReturnValue();
        StationAPI.EVENT_BUS.post(DeserializeModelJsonEvent.builder().jsonModel(jsonModel).jsonElement(jsonElement).type(type).context(jsonDeserializationContext).build());
    }
}
