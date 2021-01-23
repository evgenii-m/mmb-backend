package ru.pushkin.mmb.api.output.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.pushkin.mmb.data.enumeration.PlaylistType;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class PlaylistDto extends PlaylistShortDto {
    List<TrackDto> tracks;

    public PlaylistDto(int id, @NotNull String title, @NotNull LocalDateTime creationTime, boolean active, boolean sync,
                       @NotNull PlaylistType type, String sourceUrl, int tracksCount, List<TrackDto> tracks) {
        super(id, title, creationTime, active, sync, type, sourceUrl, tracksCount);
        this.tracks = tracks;
    }
}
