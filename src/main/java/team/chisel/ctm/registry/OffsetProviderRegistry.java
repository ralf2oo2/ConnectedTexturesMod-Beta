package team.chisel.ctm.registry;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.util.math.Vec3i;
import team.chisel.ctm.api.OffsetProvider;

import java.util.ArrayList;
import java.util.List;

public enum OffsetProviderRegistry {

    INSTANCE;

    private final List<OffsetProvider> providers = new ArrayList<>();

    public void registerProvider(OffsetProvider provider) {
        this.providers.add(provider);
    }

    public BlockPos getOffset(World world, BlockPos pos) {
        BlockPos ret = BlockPos.ORIGIN;
        for (OffsetProvider p : providers) {
            BlockPos bp = p.getOffset(world, pos);
            ret = ret.add(new Vec3i(bp.x, bp.y, bp.z));
        }
        return ret;
    }

}
