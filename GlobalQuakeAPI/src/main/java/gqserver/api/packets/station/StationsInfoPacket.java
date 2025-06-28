package gqserver.api.packets.station;

import gqserver.api.Packet;
import gqserver.api.data.station.StationInfoData;

import java.io.Serial;
import java.util.List;
import java.util.UUID;

public record StationsInfoPacket(UUID stationsIndexing, List<StationInfoData> stationInfoDataList) implements Packet {
    @Serial
    private static final long serialVersionUID = 0L;
}
