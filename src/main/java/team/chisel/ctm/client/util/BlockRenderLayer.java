package team.chisel.ctm.client.util;

public enum BlockRenderLayer {
    SOLID("Solid"),
    CUTOUT_MIPPED("Mipped Cutout"),
    CUTOUT("Cutout"),
    TRANSLUCENT("Translucent");

    private final String layerName;

    private BlockRenderLayer(String layerName) {
        this.layerName = layerName;
    }

    public String toString() {
        return this.layerName;
    }
}
