package ru.pushkin.mmb.api.output.dto;

import lombok.Builder;
import lombok.Data;
import ru.pushkin.mmb.data.enumeration.PlaylistType;

import java.time.LocalDateTime;

@Data
@Builder
public class PlaylistShortDto {
    private Integer id;
    private String title;
    private LocalDateTime creationTime;
    private boolean active;
    private boolean sync;
    private PlaylistType type;
    private String sourceUrl;
    private int tracksCount;
}
