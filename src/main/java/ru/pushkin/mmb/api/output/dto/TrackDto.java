package ru.pushkin.mmb.api.output.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class TrackDto {
    @NotNull
    private String uuid;
    private Number number;
    @NotNull
    private String artist;
    @NotNull
    private String title;
    private String album;
    @NotNull
    private int length;
    @NotNull
    private LocalDateTime added;
    private String sourceLink;
}
