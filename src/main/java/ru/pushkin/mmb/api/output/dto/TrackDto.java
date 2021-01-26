package ru.pushkin.mmb.api.output.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class TrackDto {
    private int id;
    private String mbid;
    private Number number;
    private Integer position;
    @NotNull
    private String artist;
    @NotNull
    private String trackName;
    private String album;
    @NotNull
    private Long length;
    @NotNull
    private LocalDateTime date;
    private String sourceLink;
}
