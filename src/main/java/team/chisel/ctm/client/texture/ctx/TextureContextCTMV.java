package team.chisel.ctm.client.texture.ctx;

import com.google.common.collect.ObjectArrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.util.math.BlockPos;
import net.modificationstation.stationapi.api.block.BlockState;
import net.modificationstation.stationapi.api.util.math.Direction;
import net.modificationstation.stationapi.api.world.BlockStateView;
import org.apache.commons.lang3.ArrayUtils;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.client.util.ConnectionLocations;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static team.chisel.ctm.client.util.ConnectionLocations.*;

public class TextureContextCTMV implements ITextureContext {

    private static final ConnectionLocations[] MAIN_VALUES = { UP, DOWN, NORTH, SOUTH, EAST, WEST };
    private static final ConnectionLocations[] OFFSET_VALUES = ArrayUtils.removeElements(ConnectionLocations.VALUES, ObjectArrays.concat(
            new ConnectionLocations[] { NORTH_EAST_UP, NORTH_EAST_DOWN, NORTH_WEST_UP, NORTH_WEST_DOWN, SOUTH_WEST_UP, SOUTH_WEST_DOWN, SOUTH_EAST_UP, SOUTH_EAST_DOWN, },
            MAIN_VALUES,
            ConnectionLocations.class
    ));
    private static final ConnectionLocations[] ALL_VALUES = ObjectArrays.concat(MAIN_VALUES, OFFSET_VALUES, ConnectionLocations.class);

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Connections {

        private EnumSet<Direction> connections;

        public boolean connected(Direction facing) {
            return connections.contains(facing);
        }

        public boolean connectedAnd(Direction... facings) {
            for (Direction d : facings) {
                if (!connected(d)) {
                    return false;
                }
            }
            return true;
        }

        public boolean connectedOr(Direction... facings) {
            for (Direction d : facings) {
                if (connected(d)) {
                    return true;
                }
            }
            return false;
        }

        public static Connections forPos(BlockStateView world, BlockPos pos) {
            BlockState state = world.getBlockState(pos);
            return forPos(world, state, pos);
        }

        public static Connections forData(long data, Direction offset) {
            EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
            if (offset == null) {
                for (ConnectionLocations loc : MAIN_VALUES) {
                    if ((data & loc.getMask()) != 0) {
                        connections.add(ConnectionLocations.toFacing(loc));
                    }
                }
            } else {
                for (ConnectionLocations loc : OFFSET_VALUES) {
                    if ((data & loc.getMask()) != 0) {
                        Direction facing = loc.clipOrDestroy(offset);
                        if (facing != null) {
                            connections.add(facing);
                        }
                    }
                }
            }
            return new Connections(connections);
        }

        public static Connections forPos(BlockStateView world, BlockState baseState, BlockPos pos) {
            EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
            BlockState state = world.getBlockState(pos);
            if (state == baseState) {
                for (Direction f : Direction.values()) {
                    if (world.getBlockState(pos.offset(f)) == baseState) {
                        connections.add(f);
                    }
                }
            }
            return new Connections(connections);
        }
    }

    @ToString
    public static class ConnectionData {

        @Getter
        private Connections connections;
        private Map<Direction, Connections> connectionConnections = new EnumMap<>(Direction.class);

        public ConnectionData(BlockStateView world, BlockPos pos) {
            connections = Connections.forPos(world, pos);
            BlockState state = world.getBlockState(pos);
            for (Direction d : Direction.values()) {
                connectionConnections.put(d, Connections.forPos(world, state, pos.offset(d)));
            }
        }

        public ConnectionData(long data){
            connections = Connections.forData(data, null);
            for (Direction d : Direction.values()){
                connectionConnections.put(d, Connections.forData(data, d));
            }
        }

        public Connections getConnections(Direction facing) {
            return connectionConnections.get(facing);
        }
    }

    @Getter
    private ConnectionData data;

    private long compressedData;

    public TextureContextCTMV(BlockStateView world, BlockPos pos) {
        data = new ConnectionData(world, pos);

        BlockState state = world.getBlockState(pos);
        for (ConnectionLocations loc : ALL_VALUES) {
            if (state == world.getBlockState(loc.transform(pos))){
                compressedData = compressedData | loc.getMask();
            }
        }
    }

    public TextureContextCTMV(long data){
        this.data = new ConnectionData(data);
    }

    @Override
    public long getCompressedData(){
        return this.compressedData;
    }
}
