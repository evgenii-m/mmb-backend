package ru.pushkin.mmb.api.output.response;

import ru.pushkin.mmb.api.output.dto.TrackDto;

import java.util.List;

public class ListeningHistoryResponse extends PageableResponse<TrackDto> {

    public ListeningHistoryResponse(int page, int size, long totalSize, List<TrackDto> data) {
        super(page, size, totalSize, data);
    }
}
