package team.chisel.ctm.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;


public interface OffsetProvider {

    @NotNull
    BlockPos getOffset(@NotNull World world, @NotNull BlockPos pos);

}
