package team.chisel.ctm.client.util;

public enum BlockRenderLayer {
    SOLID("Solid", 0),
    CUTOUT_MIPPED("Mipped Cutout", 1),
    CUTOUT("Cutout", 1),
    TRANSLUCENT("Translucent", 1);

    private final String layerName;
    public final int layerIndex;

    BlockRenderLayer(String layerName, int layerIndex) {
        this.layerName = layerName;
        this.layerIndex = layerIndex;
    }

    public String toString() {
        return this.layerName;
    }
}
