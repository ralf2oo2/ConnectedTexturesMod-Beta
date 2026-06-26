package team.chisel.ctm.api.event;

import net.mine_diver.unsafeevents.Event;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.client.texture.type.TextureTypeRegistry;

public class TextureTypeRegisterEvent extends Event {
    public void register(String name, ITextureType textureType) {
        TextureTypeRegistry.register(name, textureType);
    }
}
