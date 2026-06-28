package team.chisel.ctm;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.StationAPI;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.util.Namespace;
import org.apache.logging.log4j.Logger;
import team.chisel.ctm.client.texture.render.TextureMap;
import team.chisel.ctm.client.texture.type.TextureTypeMap;
import team.chisel.ctm.event.TextureTypeRegisterEvent;
import team.chisel.ctm.client.texture.type.TextureTypeCTM;
import team.chisel.ctm.client.texture.type.TextureTypeCTMH;
import team.chisel.ctm.client.texture.type.TextureTypeCTMV;

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
        event.register("ctm", new TextureTypeCTM());

        event.register("ctmv", new TextureTypeCTMV());
        event.register("pillar", new TextureTypeCTMV());

        event.register("ctmh", new TextureTypeCTMH());
        event.register("ctm_horizontal", new TextureTypeCTMH());

        event.register("r", TextureTypeMap.RANDOM);
        event.register("random", TextureTypeMap.RANDOM);

        event.register("v", TextureTypeMap.PATTERN);
        event.register("pattern", TextureTypeMap.PATTERN);
    }
}
