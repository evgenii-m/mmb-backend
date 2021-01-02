package ru.pushkin.mmb.data.model.library;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("TrackData")
public class TrackData {

    @Id
    private String id;

    @Indexed(unique = true)
    private String mbid;

    @Indexed(unique = true)
    @NotNull
    private String title;

    @NotNull
    private String trackName;

    @NotNull
    private String artist;

    private String album;

    private Long length;

    private String lastFmUrl;

    @Transient
    private LocalDateTime dateTime;
}
