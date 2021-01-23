package ru.pushkin.mmb.api.output.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.pushkin.mmb.api.output.dto.PlaylistDto;

@AllArgsConstructor
@Data
public class PlaylistResponse {
    private PlaylistDto data;
}
