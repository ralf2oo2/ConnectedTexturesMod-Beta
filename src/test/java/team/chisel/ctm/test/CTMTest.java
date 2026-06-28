package team.chisel.ctm.test;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.template.block.TemplateGlassBlock;
import net.modificationstation.stationapi.api.util.Namespace;
import org.apache.logging.log4j.Logger;

public class CTMTest {
    @Entrypoint.Namespace
    public static Namespace NAMESPACE;

    @Entrypoint.Logger
    public static Logger LOGGER;

    public static Block testBlockCtm;

    @EventListener
    public void registerBlocks(BlockRegistryEvent event) {
        testBlockCtm = new TemplateGlassBlock(NAMESPACE.id("ctm_glass"), 0, Material.GLASS, true);
    }
}
