package ru.pushkin.mmb.data.model.library;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "track_data")
public class TrackData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String mbid;

    @NotNull
    @Column(unique = true)
    private String title;

    @NotNull
    @Column(name = "track_name")
    private String trackName;

    @NotNull
    private String artist;

    private String album;

    @NotNull
    private Long length;

    @Column(name = "lastfm_url")
    private String lastFmUrl;

    @Transient
    private LocalDateTime dateTime;


    public void setTitle(String artist, String trackName) {
        this.title = String.format("%s - %s", artist, trackName);
    }
}
