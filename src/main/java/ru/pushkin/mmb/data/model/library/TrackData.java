package ru.pushkin.mmb.data.model.library;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class TrackData {
    @Id
    private String id;
    private String mbid;
    @NotNull
    private String title;
    @NotNull
    private String artist;
    private String album;
    private Long length;
    private String lastFmUrl;
}
