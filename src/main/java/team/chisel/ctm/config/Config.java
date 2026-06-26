package team.chisel.ctm.config;

import net.glasslauncher.mods.gcapi3.api.ConfigEntry;
import net.glasslauncher.mods.gcapi3.api.ConfigRoot;

public class Config {
    @ConfigRoot(value = "config", visibleName = "Config")
    public static final ConfigEntries CONFIG = new ConfigEntries();
    public static class ConfigEntries {
        @ConfigEntry(name = "Disable CTM", description = "Disable connected textures entirely")
        public Boolean disableCtm = false;

        @ConfigEntry(name = "Connect inside corner", description = "Choose whether the inside corner is disconnected on a CTM block")
        public Boolean connectInsideCTM = false;
    }
}
