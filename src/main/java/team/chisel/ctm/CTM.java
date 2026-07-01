package team.chisel.ctm;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.StationAPI;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.util.Namespace;
import org.apache.logging.log4j.Logger;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.client.texture.type.*;
import team.chisel.ctm.event.TextureTypeRegisterEvent;

public class CTM {
    @Entrypoint.Namespace
    public static Namespace NAMESPACE;

    @Entrypoint.Logger
    public static Logger LOGGER;

    @EventListener
    public void onInit(InitEvent event) {
        StationAPI.EVENT_BUS.post(new TextureTypeRegisterEvent());
    }


    @EventListener
    public void registerTextureTypes(TextureTypeRegisterEvent event) {
        TextureType type;
        event.register("ctm", new TextureTypeCTM());
        event.register("edges", new TextureTypeEdges());
        event.register("edges_full", new TextureTypeEdgesFull());
        event.register("eldritch", new TextureTypeEldritch());
        event.register("random", TextureTypeMap.RANDOM);
        event.register("r", TextureTypeMap.RANDOM);
        event.register("pattern", TextureTypeMap.PATTERN);
        event.register("v", TextureTypeMap.PATTERN);
        event.register("normal", TextureTypeNormal.INSTANCE);
        type = new TextureTypePillar();
        event.register("pillar", type);
        event.register("ctmv", type);
        event.register("ctm_horizontal", TextureTypePlane.HORIZONRAL);
        event.register("ctmh", TextureTypePlane.HORIZONRAL);
        event.register("ctm_vertical", TextureTypePlane.VERTICAL);
        type = new TextureTypeSCTM();
        event.register("ctm_simple", type);
        event.register("sctm", type);
        event.register("optifine_full", new TextureTypeOptifineFull());
    }
}
