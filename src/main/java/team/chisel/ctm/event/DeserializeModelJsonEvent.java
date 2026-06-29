package team.chisel.ctm.event;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import lombok.experimental.SuperBuilder;
import net.mine_diver.unsafeevents.Event;
import net.modificationstation.stationapi.api.client.render.model.json.JsonUnbakedModel;

import java.lang.reflect.Type;

@SuperBuilder
public class DeserializeModelJsonEvent extends Event {
    public final JsonUnbakedModel jsonModel;
    public final JsonElement jsonElement;
    public final Type type;
    public final JsonDeserializationContext context;
}
