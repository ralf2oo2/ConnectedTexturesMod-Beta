package team.chisel.ctm.event;

import net.mine_diver.unsafeevents.Event;
import team.chisel.ctm.api.OffsetProvider;
import team.chisel.ctm.registry.OffsetProviderRegistry;

public class OffsetProviderRegisterEvent extends Event {
    public void register(OffsetProvider offsetProvider) {
        OffsetProviderRegistry.INSTANCE.registerProvider(offsetProvider);
    }
}
