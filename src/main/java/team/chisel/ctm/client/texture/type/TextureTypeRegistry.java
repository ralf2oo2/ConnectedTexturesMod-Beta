package team.chisel.ctm.client.texture.type;

import com.google.common.collect.Maps;
import net.modificationstation.stationapi.api.StationAPI;
import net.modificationstation.stationapi.api.util.Identifier;
import team.chisel.ctm.api.event.TextureTypeRegisterEvent;
import team.chisel.ctm.api.texture.ITextureType;

import java.util.Locale;
import java.util.Map;

/**
 * Registry for all the different texture types
 */
public class TextureTypeRegistry {

    private static final Map<String, ITextureType> map = Maps.newHashMap();

    public TextureTypeRegistry() {
        TextureTypeRegisterEvent event = new TextureTypeRegisterEvent();
        StationAPI.EVENT_BUS.post(event);
    }

    public static void register(String name, ITextureType type){
        String key = name.toLowerCase(Locale.ROOT);
        if (map.containsKey(key) && map.get(key) != type){
            throw new IllegalArgumentException("Render Type with name "+key+" has already been registered!");
        }
        else if (map.get(key) != type){
            map.put(key, type);
        }
    }

    public static ITextureType getType(String name){
        String key = name.toLowerCase(Locale.ROOT);
        return map.get(key);
    }

    public static boolean isValid(String name){
        return getType(name) != null;
    }
}