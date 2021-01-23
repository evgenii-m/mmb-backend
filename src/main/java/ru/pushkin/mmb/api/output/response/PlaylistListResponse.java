package ru.pushkin.mmb.api.output.response;

import ru.pushkin.mmb.api.output.dto.PlaylistShortDto;

import java.util.List;

public class PlaylistListResponse extends PageableResponse<PlaylistShortDto> {

    public PlaylistListResponse(int page, int size, long totalSize, List<PlaylistShortDto> data) {
        super(page, size, totalSize, data);
    }
}
