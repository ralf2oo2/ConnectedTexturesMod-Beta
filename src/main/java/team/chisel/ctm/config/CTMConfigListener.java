package team.chisel.ctm.config;

import net.fabricmc.loader.api.FabricLoader;
import net.glasslauncher.mods.gcapi3.api.PreConfigSavedListener;
import net.glasslauncher.mods.gcapi3.impl.EventStorage;
import net.glasslauncher.mods.gcapi3.impl.GlassYamlFile;
import net.minecraft.client.Minecraft;
import team.chisel.ctm.client.model.CTMBakedModel;

public class CTMConfigListener implements PreConfigSavedListener{
    @Override
    public void onPreConfigSaved(int source, GlassYamlFile oldValues, GlassYamlFile newValues) {

        Minecraft minecraft = (Minecraft) FabricLoader.getInstance().getGameInstance();
        if (EventStorage.EventSource.containsOne(source, EventStorage.EventSource.USER_SAVE, EventStorage.EventSource.MOD_SAVE)) {
            if (oldValues.contains("disableCtm")) {
                if (oldValues.getBoolean("disableCtm") != newValues.getBoolean("disableCtm")) {
                    CTMBakedModel.invalidateCaches();
                    minecraft.worldRenderer.reload();
                    return;
                }
            }
            if (oldValues.contains("connectInsideCTM")) {
                if (oldValues.getBoolean("connectInsideCTM") != newValues.getBoolean("connectInsideCTM")) {
                    CTMBakedModel.invalidateCaches();
                    minecraft.worldRenderer.reload();
                    return;
                }
            }
        }
    }
}
