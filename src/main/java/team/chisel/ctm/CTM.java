package team.chisel.ctm;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.util.Namespace;
import org.apache.logging.log4j.Logger;
import team.chisel.ctm.api.event.TextureTypeRegisterEvent;
import team.chisel.ctm.client.texture.type.TextureTypeCTM;

public class CTM {
    @Entrypoint.Namespace
    public static Namespace NAMESPACE;

    @Entrypoint.Logger
    public static Logger LOGGER;


    @EventListener
    public void registerTextureTypes(TextureTypeRegisterEvent event) {
        event.register("ctm", new TextureTypeCTM());
    }
}
