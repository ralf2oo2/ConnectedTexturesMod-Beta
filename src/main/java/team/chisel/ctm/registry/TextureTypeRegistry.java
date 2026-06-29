package team.chisel.ctm.registry;

import com.google.common.collect.Maps;
import team.chisel.ctm.api.texture.TextureType;

import java.util.Locale;
import java.util.Map;

/**
 * Registry for all the different texture types
 */
public class TextureTypeRegistry {

    private static final Map<String, TextureType> map = Maps.newHashMap();

    public static void register(String name, TextureType type){
        String key = name.toLowerCase(Locale.ROOT);
        if (map.containsKey(key) && map.get(key) != type){
            throw new IllegalArgumentException("Render Type with name "+key+" has already been registered!");
        }
        else if (map.get(key) != type){
            map.put(key, type);
        }
    }

    public static TextureType getType(String name){
        String key = name.toLowerCase(Locale.ROOT);
        return map.get(key);
    }

    public static boolean isValid(String name){
        return getType(name) != null;
    }
}