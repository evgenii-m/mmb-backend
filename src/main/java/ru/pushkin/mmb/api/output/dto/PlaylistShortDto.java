package ru.pushkin.mmb.api.output.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.pushkin.mmb.data.enumeration.PlaylistType;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PlaylistShortDto {
    private int id;
    @NotNull
    private String title;
    private String description;
    @NotNull
    private LocalDateTime creationTime;
    private boolean active;
    private boolean sync;
    @NotNull
    private PlaylistType type;
    private String sourceUrl;
    private int tracksCount;
}
