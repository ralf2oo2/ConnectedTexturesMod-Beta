package team.chisel.ctm.event;

import net.mine_diver.unsafeevents.Event;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.registry.TextureTypeRegistry;

public class TextureTypeRegisterEvent extends Event {
    public void register(String name, ITextureType textureType) {
        TextureTypeRegistry.register(name, textureType);
    }
}
