package ru.pushkin.mmb.api.output.response;

import ru.pushkin.mmb.api.output.dto.TrackDto;

import java.util.List;

public class FavoriteTracksResponse extends PageableResponse<TrackDto> {

    public FavoriteTracksResponse(int page, int size, long totalSize, List<TrackDto> data) {
        super(page, size, totalSize, data);
    }
}
