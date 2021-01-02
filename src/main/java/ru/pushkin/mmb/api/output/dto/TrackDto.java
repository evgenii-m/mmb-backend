package ru.pushkin.mmb.api.output.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class TrackDto {
    private String uuid;
    private Number number;
    @NotNull
    private String artist;
    @NotNull
    private String title;
    private String album;
    @NotNull
    private long length;
    @NotNull
    private LocalDateTime date;
    private String sourceLink;
}
