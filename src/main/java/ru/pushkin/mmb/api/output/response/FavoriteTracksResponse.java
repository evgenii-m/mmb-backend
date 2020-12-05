package ru.pushkin.mmb.api.output.response;

import ru.pushkin.mmb.api.output.dto.TrackDto;

import java.util.List;

public class FavoriteTracksResponse extends PageableResponse<List<TrackDto>> {

    public FavoriteTracksResponse(int page, int size, int totalSize, List<TrackDto> data) {
        super(page, size, totalSize, data);
    }
}
