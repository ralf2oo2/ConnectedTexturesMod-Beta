package team.chisel.ctm.test;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import net.modificationstation.stationapi.api.template.block.TemplateBlock;
import net.modificationstation.stationapi.api.template.block.TemplateBookshelfBlock;
import net.modificationstation.stationapi.api.template.block.TemplateGlassBlock;
import net.modificationstation.stationapi.api.template.block.TemplateSugarCaneBlock;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;
import org.apache.logging.log4j.Logger;

public class CTMTest {
    @Entrypoint.Namespace
    public static Namespace NAMESPACE;

    @Entrypoint.Logger
    public static Logger LOGGER;

    public static Block testBlockCtm;
    public static Block testBlockCtmv;
    public static Block testBlockCtmh;
    public static Block testBlockRandom;
    public static Block testBlockPattern;
    public static Block testBlockEdgeFull;
    public static Block testBlockEldritch;
    public static Block testBlockProxy;

    @EventListener
    public void registerBlocks(BlockRegistryEvent event) {
        testBlockCtm = new TemplateGlassBlock(NAMESPACE.id("ctm_glass"), 0, Material.GLASS, false);
        testBlockCtmv = new TemplateSugarCaneBlock(NAMESPACE.id("ctmv_sugarcane"), 0);
        testBlockCtmh = new TemplateBookshelfBlock(NAMESPACE.id("ctmh_bookshelf"), 0);
        testBlockRandom = new TemplateBlock(NAMESPACE.id("random"), Material.WOOD);
        testBlockPattern = new TemplateBlock(NAMESPACE.id("pattern"), Material.WOOD);
        testBlockEdgeFull = new TemplateBlock(NAMESPACE.id("edge_full_obsidian"), Material.WOOD);
        testBlockEldritch = new TemplateBlock(NAMESPACE.id("eldritch"), Material.WOOD);
        testBlockProxy = new TemplateBlock(NAMESPACE.id("proxy"), Material.WOOD);
    }
}
