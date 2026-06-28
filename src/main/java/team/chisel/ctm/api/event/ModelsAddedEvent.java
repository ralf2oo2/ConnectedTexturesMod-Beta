package team.chisel.ctm.api.event;

import lombok.experimental.SuperBuilder;
import net.mine_diver.unsafeevents.Event;
import net.modificationstation.stationapi.api.client.render.model.ModelLoader;
import net.modificationstation.stationapi.api.client.render.model.UnbakedModel;
import net.modificationstation.stationapi.api.resource.ResourceManager;
import net.modificationstation.stationapi.api.util.Identifier;

import java.util.Map;

@SuperBuilder
public class ModelsAddedEvent extends Event {
    public final ModelLoader modelLoader;
    public final Map<Identifier, UnbakedModel> unbakedModels;
    public final Map<Identifier, UnbakedModel> modelsToBake;
}
