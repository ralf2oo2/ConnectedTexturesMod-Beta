package team.chisel.ctm.init;

import net.mine_diver.unsafeevents.listener.EventListener;
import team.chisel.ctm.api.util.ResourceUtil;
import team.chisel.ctm.client.model.CTMBakedModel;
import team.chisel.ctm.event.ModelsLoadedEvent;

public class ModelsLoadedListener {
    @EventListener
    public void onModelsLoaded(ModelsLoadedEvent event) {
        CTMBakedModel.invalidateCaches();
        ResourceUtil.invalidateCaches();
    }
}
