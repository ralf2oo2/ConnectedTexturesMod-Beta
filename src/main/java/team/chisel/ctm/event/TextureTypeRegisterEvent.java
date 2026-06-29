package team.chisel.ctm.event;

import net.mine_diver.unsafeevents.Event;
import team.chisel.ctm.api.texture.TextureType;
import team.chisel.ctm.registry.TextureTypeRegistry;

public class TextureTypeRegisterEvent extends Event {
    public void register(String name, TextureType textureType) {
        TextureTypeRegistry.register(name, textureType);
    }
}
