package team.chisel.ctm.client.util.connection;

import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.world.BlockStateView;
import org.jetbrains.annotations.NotNull;

import static net.modificationstation.stationapi.api.util.math.Direction.*;

public class PillarConnectionLogic extends SpacialConnectionLogic {
    @Override
    public void buildConnectionMap(@NotNull BlockStateView world, @NotNull BlockPos pos) {
        connectionMap = 0;

        if (!checkY(world, pos, true)) {
            if (!checkX(world, pos, true)) {
                checkZ(world, pos, true);
            }
        }
    }

    private boolean checkY(BlockStateView world, BlockPos pos, boolean changeMap) {
        boolean connectedUp = isConnected(world, pos, UP);
        boolean connectedDown = isConnected(world, pos, DOWN);
        if (connectedUp || connectedDown) {
            if (changeMap) {
                setConnected(UP, connectedUp);
                setConnected(DOWN, connectedDown);
            }
            return true;
        }
        return false;
    }

    private boolean checkX(BlockStateView world, BlockPos pos, boolean changeMap) {
        boolean connectedEast = isConnected(world, pos, EAST);
        if (connectedEast) {
            connectedEast = !checkY(world, pos.offset(EAST), false);
        }
        boolean connectedWest = isConnected(world, pos, WEST);
        if (connectedWest) {
            connectedWest = !checkY(world, pos.offset(WEST), false);
        }
        if (connectedEast || connectedWest) {
            if (changeMap) {
                setConnected(EAST, connectedEast);
                setConnected(WEST, connectedWest);
            }
            return true;
        }
        return false;
    }

    private boolean checkZ(BlockStateView world, BlockPos pos, boolean changeMap) {
        BlockPos offsetPos;
        boolean connectedSouth = isConnected(world, pos, SOUTH);
        if (connectedSouth) {
            offsetPos = pos.offset(SOUTH);
            connectedSouth = !checkX(world, offsetPos, false) && !checkY(world, offsetPos, false);
        }
        boolean connectedNorth = isConnected(world, pos, NORTH);
        if (connectedNorth) {
            offsetPos = pos.offset(NORTH);
            connectedNorth = !checkX(world, offsetPos, false) && !checkY(world, offsetPos, false);
        }
        if (connectedSouth || connectedNorth) {
            if (changeMap) {
                setConnected(SOUTH, connectedSouth);
                setConnected(NORTH, connectedNorth);
            }
            return true;
        }
        return false;
    }
}
